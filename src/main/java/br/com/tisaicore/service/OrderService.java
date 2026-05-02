package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.AssignBatchRequest;
import br.com.tisaicore.dto.request.CreateOrderRequest;
import br.com.tisaicore.dto.response.OrderResponse;
import br.com.tisaicore.entity.*;
import br.com.tisaicore.exception.InsufficientStockException;
import br.com.tisaicore.exception.ResourceNotFoundException;
import br.com.tisaicore.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final CompanyService companyService;
    private final UserService userService;
    private final BatchService batchService;

    public OrderService(OrderRepository orderRepository,
                        ProductService productService,
                        CompanyService companyService,
                        UserService userService,
                        BatchService batchService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.companyService = companyService;
        this.userService = userService;
        this.batchService = batchService;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request, Long userId) {
        Company company = companyService.findEntityById(request.companyId());
        User user = userService.findEntityById(userId);

        Order order = new Order();
        order.setCompany(company);
        order.setUser(user);
        order.setNotes(request.notes());

        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
            Product product = productService.findEntityById(itemReq.productId());

            if (product.getStockQuantity() < itemReq.quantity()) {
                throw new InsufficientStockException(
                        product.getName(), product.getStockQuantity(), itemReq.quantity());
            }

            product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(java.math.BigDecimal.valueOf(itemReq.quantity())));

            order.addItem(item);
        }

        order.recalculateTotal();

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAllByActiveTrue(pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível atualizar um pedido cancelado");
        }

        if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.isCancelled()) {
                    continue;
                }
                returnItemToStock(item);
                item.setCancelled(true);
                item.setCancelReason("Pedido cancelado");
                item.setCancelledAt(LocalDateTime.now());
            }
            order.recalculateTotal();
        }

        order.setStatus(status);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public void softDelete(Long id, String reason) {
        Order order = orderRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        for (OrderItem item : order.getItems()) {
            if (item.isCancelled()) {
                continue;
            }
            returnItemToStock(item);
        }

        order.setActive(false);
        order.setDeletedAt(LocalDateTime.now());
        order.setDeletedReason(reason);
        orderRepository.save(order);
    }

    private void returnItemToStock(OrderItem item) {
        Product product = item.getProduct();
        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());

        if (item.getBatch() != null) {
            Batch batch = item.getBatch();
            batch.setCurrentQuantity(batch.getCurrentQuantity() + item.getQuantity());
        }
    }

    @Transactional
    public OrderResponse assignBatches(Long orderId, AssignBatchRequest request) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível atribuir lotes a um pedido cancelado");
        }

        for (AssignBatchRequest.ItemBatch itemBatch : request.items()) {
            OrderItem orderItem = order.getItems().stream()
                    .filter(i -> i.getId().equals(itemBatch.orderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Order item not found: " + itemBatch.orderItemId()));

            Batch batch = batchService.findEntityById(itemBatch.batchId());

            if (!batch.getProduct().getId().equals(orderItem.getProduct().getId())) {
                throw new IllegalArgumentException(
                        "Lote " + batch.getCode() + " não pertence ao produto " + orderItem.getProduct().getName());
            }

            // Se já tinha lote atribuído, devolve a quantidade ao lote anterior
            if (orderItem.getBatch() != null) {
                Batch previousBatch = orderItem.getBatch();
                previousBatch.setCurrentQuantity(previousBatch.getCurrentQuantity() + orderItem.getQuantity());
            }

            // Desconta do novo lote
            if (batch.getCurrentQuantity() < orderItem.getQuantity()) {
                throw new InsufficientStockException(
                        "Batch " + batch.getCode(), batch.getCurrentQuantity(), orderItem.getQuantity());
            }
            batch.setCurrentQuantity(batch.getCurrentQuantity() - orderItem.getQuantity());

            orderItem.setBatch(batch);
        }

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrderItem(Long orderId, Long itemId, String reason) {
        Order order = orderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível cancelar itens de um pedido já cancelado");
        }

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem", itemId));

        if (item.isCancelled()) {
            throw new IllegalArgumentException("Este item já foi cancelado");
        }

        returnItemToStock(item);

        item.setCancelled(true);
        item.setCancelReason(reason);
        item.setCancelledAt(LocalDateTime.now());

        order.recalculateTotal();

        boolean allCancelled = order.getItems().stream().allMatch(OrderItem::isCancelled);
        if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        return OrderResponse.from(orderRepository.save(order));
    }
}
