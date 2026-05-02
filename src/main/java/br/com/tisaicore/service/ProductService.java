package br.com.tisaicore.service;

import br.com.tisaicore.dto.request.CreateProductRequest;
import br.com.tisaicore.dto.response.ProductResponse;
import br.com.tisaicore.entity.Brand;
import br.com.tisaicore.entity.Category;
import br.com.tisaicore.entity.Product;
import br.com.tisaicore.exception.ResourceNotFoundException;
import br.com.tisaicore.repository.BrandRepository;
import br.com.tisaicore.repository.CategoryRepository;
import br.com.tisaicore.repository.ProductImageRepository;
import br.com.tisaicore.repository.ProductRepository;
import br.com.tisaicore.service.file.FileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final FileService fileService;

    public ProductService(ProductRepository productRepository,
                          BrandRepository brandRepository,
                          CategoryRepository categoryRepository,
                          ProductImageRepository productImageRepository,
                          FileService fileService) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.fileService = fileService;
    }

    private String normalizeSku(String sku) {
        if (sku == null || sku.isBlank()) return null;
        return sku.trim();
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        String sku = normalizeSku(request.sku());
        if (sku != null && productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("SKU já cadastrado: " + sku);
        }

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setSku(sku);
        product.setPrice(request.price());

        if (request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", request.brandId()));
            product.setBrand(brand);
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
            product.setCategory(category);
        }

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable, boolean withImage) {
        return productRepository.findAllByActiveTrue(pageable).map(product -> {
            String imageUrl = withImage ? resolveImageUrl(product.getId()) : null;
            return ProductResponse.from(product, imageUrl);
        });
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id, boolean withImage) {
        Product product = findEntityById(id);
        String imageUrl = withImage ? resolveImageUrl(id) : null;
        return ProductResponse.from(product, imageUrl);
    }

    private String resolveImageUrl(Long productId) {
        return productImageRepository
                .findFirstByProductIdAndActiveTrueOrderByDisplayOrderAsc(productId)
                .map(img -> fileService.getUrl(img.getSisFile()))
                .orElse(null);
    }

    @Transactional
    public ProductResponse update(Long id, CreateProductRequest request) {
        Product product = findEntityById(id);
        String sku = normalizeSku(request.sku());
        if (sku != null && !sku.equals(product.getSku()) && productRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("SKU já cadastrado: " + sku);
        }
        product.setName(request.name());
        product.setDescription(request.description());
        product.setSku(sku);
        product.setPrice(request.price());

        if (request.brandId() != null) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", request.brandId()));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.categoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateStock(Long id, Integer quantity) {
        Product product = findEntityById(id);
        product.setStockQuantity(quantity);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findEntityById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    public Product findEntityById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
