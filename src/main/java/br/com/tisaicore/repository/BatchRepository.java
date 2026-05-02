package br.com.tisaicore.repository;

import br.com.tisaicore.entity.Batch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    Optional<Batch> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Batch> findByIdAndActiveTrue(Long id);

    Page<Batch> findAllByActiveTrue(Pageable pageable);

    Page<Batch> findByProductIdAndActiveTrue(Long productId, Pageable pageable);

    List<Batch> findByProductIdAndActiveTrue(Long productId);

    List<Batch> findByExpirationDateBeforeAndActiveTrue(LocalDate date);

    List<Batch> findByExpirationDateBetweenAndActiveTrue(LocalDate start, LocalDate end);
}
