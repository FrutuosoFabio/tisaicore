package br.com.tisaicore.repository;

import br.com.tisaicore.entity.Order;
import br.com.tisaicore.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndActiveTrue(Long id);

    Page<Order> findAllByActiveTrue(Pageable pageable);

    /**
     * Orders matching the report filter, eagerly loaded with items/product/category/brand/company/user.
     * The service performs all aggregations in memory, which keeps the SQL simple and the report code
     * uniform across drivers.
     */
    @Query("""
        SELECT DISTINCT o
        FROM Order o
        LEFT JOIN FETCH o.company
        LEFT JOIN FETCH o.user
        LEFT JOIN FETCH o.items i
        LEFT JOIN FETCH i.product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.brand
        WHERE o.active = true
          AND o.createdAt BETWEEN :start AND :end
          AND (:status IS NULL OR o.status = :status)
          AND (:companyId IS NULL OR o.company.id = :companyId)
          AND (:userId IS NULL OR o.user.id = :userId)
          AND (:productId IS NULL OR EXISTS (
                SELECT 1 FROM OrderItem ii WHERE ii.order = o AND ii.product.id = :productId
          ))
          AND (:categoryId IS NULL OR EXISTS (
                SELECT 1 FROM OrderItem ii WHERE ii.order = o AND ii.product.category.id = :categoryId
          ))
          AND (:brandId IS NULL OR EXISTS (
                SELECT 1 FROM OrderItem ii WHERE ii.order = o AND ii.product.brand.id = :brandId
          ))
        ORDER BY o.createdAt ASC
    """)
    List<Order> findForReport(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") OrderStatus status,
            @Param("companyId") Long companyId,
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId
    );
}
