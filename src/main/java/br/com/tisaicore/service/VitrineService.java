package br.com.tisaicore.service;

import br.com.tisaicore.dto.response.VitrineAdminResponse;
import br.com.tisaicore.dto.response.VitrinePublicResponse;
import br.com.tisaicore.entity.Product;
import br.com.tisaicore.exception.ResourceNotFoundException;
import br.com.tisaicore.repository.ProductImageRepository;
import br.com.tisaicore.repository.ProductRepository;
import br.com.tisaicore.service.file.FileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VitrineService {

    private static final int MAX_VITRINE_SIZE = 12;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileService fileService;

    public VitrineService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          FileService fileService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.fileService = fileService;
    }

    @Transactional
    public VitrineAdminResponse addProduct(Long productId) {
        Product product = findProduct(productId);

        if (product.isVitrine()) {
            throw new IllegalArgumentException("Produto já está na vitrine.");
        }

        if (productRepository.countByVitrineTrue() >= MAX_VITRINE_SIZE) {
            throw new IllegalArgumentException(
                    "A vitrine já está cheia. Máximo de " + MAX_VITRINE_SIZE + " produtos permitidos.");
        }

        product.setVitrine(true);
        productRepository.save(product);
        return VitrineAdminResponse.from(product, resolveImageUrl(productId));
    }

    @Transactional
    public void removeProduct(Long productId) {
        Product product = findProduct(productId);

        if (!product.isVitrine()) {
            throw new ResourceNotFoundException("Produto não está na vitrine", productId);
        }

        product.setVitrine(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<VitrineAdminResponse> listAdmin() {
        return productRepository.findAllByVitrineTrueOrderByIdAsc()
                .stream()
                .map(p -> VitrineAdminResponse.from(p, resolveImageUrl(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VitrinePublicResponse> listPublic() {
        return productRepository.findAllByVitrineTrueOrderByIdAsc()
                .stream()
                .filter(Product::isActive)
                .map(p -> VitrinePublicResponse.from(p, resolveImageUrl(p.getId())))
                .toList();
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private String resolveImageUrl(Long productId) {
        return productImageRepository
                .findFirstByProductIdAndActiveTrueOrderByDisplayOrderAsc(productId)
                .map(img -> fileService.getUrl(img.getSisFile()))
                .orElse(null);
    }
}
