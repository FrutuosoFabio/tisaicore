package br.com.tisaicore.dto.response;

import br.com.tisaicore.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SalesReportResponse(
        LocalDate periodStart,
        LocalDate periodEnd,
        Kpis kpis,
        List<DailyRevenue> dailySeries,
        List<TopProduct> topProducts,
        List<TopCustomer> topCustomers,
        List<StatusBreakdown> statusBreakdown,
        List<CategoryBreakdown> categoryBreakdown,
        List<BrandBreakdown> brandBreakdown,
        List<ExpiringBatch> expiringBatches,
        List<OrderRow> orders
) {

    public record Kpis(
            BigDecimal totalRevenue,
            BigDecimal averageTicket,
            long totalOrders,
            long totalItems,
            long cancelledOrders,
            BigDecimal cancelledRevenue,
            long pendingOrders,
            long deliveredOrders
    ) {}

    public record DailyRevenue(
            LocalDate date,
            BigDecimal revenue,
            long orders
    ) {}

    public record TopProduct(
            Long productId,
            String name,
            long quantity,
            BigDecimal revenue
    ) {}

    public record TopCustomer(
            Long companyId,
            String name,
            long orders,
            BigDecimal revenue
    ) {}

    public record StatusBreakdown(
            OrderStatus status,
            long count,
            BigDecimal revenue
    ) {}

    public record CategoryBreakdown(
            Long categoryId,
            String name,
            long quantity,
            BigDecimal revenue
    ) {}

    public record BrandBreakdown(
            Long brandId,
            String name,
            long quantity,
            BigDecimal revenue
    ) {}

    public record ExpiringBatch(
            Long batchId,
            String code,
            String productName,
            LocalDate expirationDate,
            int currentQuantity,
            long daysUntilExpiration
    ) {}

    public record OrderRow(
            Long orderId,
            LocalDateTime createdAt,
            String customer,
            String seller,
            OrderStatus status,
            BigDecimal totalAmount,
            int itemCount
    ) {}
}
