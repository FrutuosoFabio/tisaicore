package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.SalesReportFilter;
import br.com.tisaicore.dto.response.SalesReportResponse;
import br.com.tisaicore.dto.response.SalesReportResponse.BrandBreakdown;
import br.com.tisaicore.dto.response.SalesReportResponse.CategoryBreakdown;
import br.com.tisaicore.dto.response.SalesReportResponse.DailyRevenue;
import br.com.tisaicore.dto.response.SalesReportResponse.ExpiringBatch;
import br.com.tisaicore.dto.response.SalesReportResponse.Kpis;
import br.com.tisaicore.dto.response.SalesReportResponse.OrderRow;
import br.com.tisaicore.dto.response.SalesReportResponse.StatusBreakdown;
import br.com.tisaicore.dto.response.SalesReportResponse.TopCustomer;
import br.com.tisaicore.dto.response.SalesReportResponse.TopProduct;
import br.com.tisaicore.entity.Batch;
import br.com.tisaicore.entity.Order;
import br.com.tisaicore.entity.OrderItem;
import br.com.tisaicore.entity.OrderStatus;
import br.com.tisaicore.repository.BatchRepository;
import br.com.tisaicore.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportService {

    private static final int TOP_N = 10;
    private static final int EXPIRATION_LOOKAHEAD_DAYS = 90;
    private static final Set<OrderStatus> PENDING_STATUSES =
            EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);

    private final OrderRepository orderRepository;
    private final BatchRepository batchRepository;
    private final SalesReportPdfService pdfService;

    public ReportService(OrderRepository orderRepository,
                         BatchRepository batchRepository,
                         SalesReportPdfService pdfService) {
        this.orderRepository = orderRepository;
        this.batchRepository = batchRepository;
        this.pdfService = pdfService;
    }

    @Transactional(readOnly = true)
    public SalesReportResponse buildSalesReport(SalesReportFilter filter) {
        List<Order> orders = orderRepository.findForReport(
                filter.getStartDateTime(),
                filter.getEndDateTime(),
                filter.getStatus(),
                filter.getCompanyId(),
                filter.getUserId(),
                filter.getProductId(),
                filter.getCategoryId(),
                filter.getBrandId()
        );

        Kpis kpis = computeKpis(orders);
        List<DailyRevenue> daily = computeDailySeries(orders, filter.getStartDate(), filter.getEndDate());
        List<TopProduct> topProducts = computeTopProducts(orders);
        List<TopCustomer> topCustomers = computeTopCustomers(orders);
        List<StatusBreakdown> statusBreakdown = computeStatusBreakdown(orders);
        List<CategoryBreakdown> categoryBreakdown = computeCategoryBreakdown(orders);
        List<BrandBreakdown> brandBreakdown = computeBrandBreakdown(orders);
        List<ExpiringBatch> expiringBatches = computeExpiringBatches();
        List<OrderRow> orderRows = computeOrderRows(orders);

        return new SalesReportResponse(
                filter.getStartDate(),
                filter.getEndDate(),
                kpis,
                daily,
                topProducts,
                topCustomers,
                statusBreakdown,
                categoryBreakdown,
                brandBreakdown,
                expiringBatches,
                orderRows
        );
    }

    @Transactional(readOnly = true)
    public byte[] buildSalesReportPdf(SalesReportFilter filter) {
        SalesReportResponse report = buildSalesReport(filter);
        return pdfService.render(report);
    }

    private Kpis computeKpis(List<Order> orders) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal cancelledRevenue = BigDecimal.ZERO;
        long totalOrders = orders.size();
        long totalItems = 0;
        long cancelledOrders = 0;
        long pendingOrders = 0;
        long deliveredOrders = 0;
        long activeOrdersForTicket = 0;

        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) {
                cancelledOrders++;
                cancelledRevenue = cancelledRevenue.add(sumOriginalAmount(o));
                continue;
            }
            if (PENDING_STATUSES.contains(o.getStatus())) pendingOrders++;
            if (o.getStatus() == OrderStatus.DELIVERED) deliveredOrders++;

            BigDecimal orderRevenue = BigDecimal.ZERO;
            for (OrderItem item : o.getItems()) {
                if (item.isCancelled()) {
                    cancelledRevenue = cancelledRevenue.add(item.getTotalPrice());
                    continue;
                }
                totalItems += item.getQuantity();
                orderRevenue = orderRevenue.add(item.getTotalPrice());
            }
            totalRevenue = totalRevenue.add(orderRevenue);
            if (orderRevenue.signum() > 0) activeOrdersForTicket++;
        }

        BigDecimal avgTicket = activeOrdersForTicket == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(activeOrdersForTicket), 2, RoundingMode.HALF_UP);

        return new Kpis(
                totalRevenue.setScale(2, RoundingMode.HALF_UP),
                avgTicket,
                totalOrders,
                totalItems,
                cancelledOrders,
                cancelledRevenue.setScale(2, RoundingMode.HALF_UP),
                pendingOrders,
                deliveredOrders
        );
    }

    private BigDecimal sumOriginalAmount(Order o) {
        return o.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<DailyRevenue> computeDailySeries(List<Order> orders, LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> revenueByDay = new LinkedHashMap<>();
        Map<LocalDate, Long> ordersByDay = new LinkedHashMap<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            revenueByDay.put(d, BigDecimal.ZERO);
            ordersByDay.put(d, 0L);
        }

        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            LocalDate day = o.getCreatedAt().toLocalDate();
            if (!revenueByDay.containsKey(day)) continue;
            BigDecimal active = o.getItems().stream()
                    .filter(i -> !i.isCancelled())
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenueByDay.merge(day, active, BigDecimal::add);
            ordersByDay.merge(day, 1L, Long::sum);
        }

        List<DailyRevenue> series = new ArrayList<>(revenueByDay.size());
        revenueByDay.forEach((d, rev) ->
                series.add(new DailyRevenue(d, rev.setScale(2, RoundingMode.HALF_UP), ordersByDay.get(d))));
        return series;
    }

    private List<TopProduct> computeTopProducts(List<Order> orders) {
        Map<Long, long[]> qty = new LinkedHashMap<>();
        Map<Long, BigDecimal> rev = new LinkedHashMap<>();
        Map<Long, String> name = new LinkedHashMap<>();

        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            for (OrderItem item : o.getItems()) {
                if (item.isCancelled()) continue;
                Long pid = item.getProduct().getId();
                qty.computeIfAbsent(pid, k -> new long[]{0})[0] += item.getQuantity();
                rev.merge(pid, item.getTotalPrice(), BigDecimal::add);
                name.putIfAbsent(pid, item.getProduct().getName());
            }
        }

        return rev.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(TOP_N)
                .map(e -> new TopProduct(
                        e.getKey(),
                        name.get(e.getKey()),
                        qty.get(e.getKey())[0],
                        e.getValue().setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<TopCustomer> computeTopCustomers(List<Order> orders) {
        Map<Long, long[]> count = new LinkedHashMap<>();
        Map<Long, BigDecimal> rev = new LinkedHashMap<>();
        Map<Long, String> name = new LinkedHashMap<>();

        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            Long cid = o.getCompany().getId();
            BigDecimal active = o.getItems().stream()
                    .filter(i -> !i.isCancelled())
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            count.computeIfAbsent(cid, k -> new long[]{0})[0]++;
            rev.merge(cid, active, BigDecimal::add);
            name.putIfAbsent(cid, o.getCompany().getTradeName());
        }

        return rev.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(TOP_N)
                .map(e -> new TopCustomer(
                        e.getKey(),
                        name.get(e.getKey()),
                        count.get(e.getKey())[0],
                        e.getValue().setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<StatusBreakdown> computeStatusBreakdown(List<Order> orders) {
        Map<OrderStatus, long[]> count = new LinkedHashMap<>();
        Map<OrderStatus, BigDecimal> rev = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            count.put(s, new long[]{0});
            rev.put(s, BigDecimal.ZERO);
        }

        for (Order o : orders) {
            count.get(o.getStatus())[0]++;
            rev.merge(o.getStatus(), o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO, BigDecimal::add);
        }

        List<StatusBreakdown> result = new ArrayList<>();
        for (OrderStatus s : OrderStatus.values()) {
            result.add(new StatusBreakdown(s, count.get(s)[0], rev.get(s).setScale(2, RoundingMode.HALF_UP)));
        }
        return result;
    }

    private List<CategoryBreakdown> computeCategoryBreakdown(List<Order> orders) {
        Map<Long, long[]> qty = new LinkedHashMap<>();
        Map<Long, BigDecimal> rev = new LinkedHashMap<>();
        Map<Long, String> name = new LinkedHashMap<>();

        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            for (OrderItem item : o.getItems()) {
                if (item.isCancelled()) continue;
                var category = item.getProduct().getCategory();
                Long key = category != null ? category.getId() : 0L;
                String label = category != null ? category.getName() : "Sem categoria";
                qty.computeIfAbsent(key, k -> new long[]{0})[0] += item.getQuantity();
                rev.merge(key, item.getTotalPrice(), BigDecimal::add);
                name.putIfAbsent(key, label);
            }
        }

        return rev.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(e -> new CategoryBreakdown(
                        e.getKey() == 0 ? null : e.getKey(),
                        name.get(e.getKey()),
                        qty.get(e.getKey())[0],
                        e.getValue().setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<BrandBreakdown> computeBrandBreakdown(List<Order> orders) {
        Map<Long, long[]> qty = new LinkedHashMap<>();
        Map<Long, BigDecimal> rev = new LinkedHashMap<>();
        Map<Long, String> name = new LinkedHashMap<>();

        for (Order o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            for (OrderItem item : o.getItems()) {
                if (item.isCancelled()) continue;
                var brand = item.getProduct().getBrand();
                Long key = brand != null ? brand.getId() : 0L;
                String label = brand != null ? brand.getName() : "Sem marca";
                qty.computeIfAbsent(key, k -> new long[]{0})[0] += item.getQuantity();
                rev.merge(key, item.getTotalPrice(), BigDecimal::add);
                name.putIfAbsent(key, label);
            }
        }

        return rev.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .map(e -> new BrandBreakdown(
                        e.getKey() == 0 ? null : e.getKey(),
                        name.get(e.getKey()),
                        qty.get(e.getKey())[0],
                        e.getValue().setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }

    private List<ExpiringBatch> computeExpiringBatches() {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(EXPIRATION_LOOKAHEAD_DAYS);
        List<Batch> batches = batchRepository.findByExpirationDateBetweenAndActiveTrue(today, horizon);
        return batches.stream()
                .filter(b -> b.getCurrentQuantity() != null && b.getCurrentQuantity() > 0)
                .sorted(Comparator.comparing(Batch::getExpirationDate))
                .map(b -> new ExpiringBatch(
                        b.getId(),
                        b.getCode(),
                        b.getProduct() != null ? b.getProduct().getName() : "—",
                        b.getExpirationDate(),
                        b.getCurrentQuantity(),
                        ChronoUnit.DAYS.between(today, b.getExpirationDate())))
                .toList();
    }

    private List<OrderRow> computeOrderRows(List<Order> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .map(o -> new OrderRow(
                        o.getId(),
                        o.getCreatedAt(),
                        o.getCompany() != null ? o.getCompany().getTradeName() : "—",
                        o.getUser() != null ? o.getUser().getName() : "—",
                        o.getStatus(),
                        o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO,
                        (int) o.getItems().stream().filter(i -> !i.isCancelled()).count()))
                .toList();
    }
}
