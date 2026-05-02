package br.com.tisaicore.repository;

import br.com.tisaicore.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByName(String name);

    Optional<Brand> findByIdAndActiveTrue(Long id);
    Page<Brand> findAllByActiveTrue(Pageable pageable);
}
