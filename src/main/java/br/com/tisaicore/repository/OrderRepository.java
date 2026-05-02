package br.com.tisaicore.repository;

import br.com.tisaicore.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndActiveTrue(Long id);

    Page<Order> findAllByActiveTrue(Pageable pageable);
}
