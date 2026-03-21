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
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Não é possível atualizar um pedido cancelado");
        }

        if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }
        }

        order.setStatus(status);
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse assignBatches(Long orderId, AssignBatchRequest request) {
        Order order = orderRepository.findById(orderId)
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
}
