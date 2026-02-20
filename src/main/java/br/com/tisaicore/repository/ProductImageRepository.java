package br.com.tisaicore.repository;

import br.com.tisaicore.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdAndActiveTrueOrderByDisplayOrderAsc(Long productId);
}
