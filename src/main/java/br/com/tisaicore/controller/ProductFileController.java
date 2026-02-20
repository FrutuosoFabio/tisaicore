package br.com.tisaicore.controller;

import br.com.tisaicore.dto.response.ProductImageResponse;
import br.com.tisaicore.entity.Product;
import br.com.tisaicore.entity.ProductImage;
import br.com.tisaicore.entity.SisFile;
import br.com.tisaicore.exception.ResourceNotFoundException;
import br.com.tisaicore.repository.ProductImageRepository;
import br.com.tisaicore.repository.ProductRepository;
import br.com.tisaicore.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@Tag(name = "Product Images", description = "Gerenciamento de imagens de produtos")
public class ProductFileController {

    private final FileService fileService;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductFileController(FileService fileService,
                                  ProductRepository productRepository,
                                  ProductImageRepository productImageRepository) {
        this.fileService = fileService;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de imagem para um produto")
    public ResponseEntity<ProductImageResponse> upload(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "displayOrder", defaultValue = "0") Integer displayOrder) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        SisFile sisFile = fileService.upload(file);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setSisFile(sisFile);
        productImage.setDisplayOrder(displayOrder);
        productImageRepository.save(productImage);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductImageResponse.from(productImage, fileService.getUrl(sisFile)));
    }

    @GetMapping
    @Operation(summary = "Listar imagens de um produto")
    public ResponseEntity<List<ProductImageResponse>> list(@PathVariable Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }

        List<ProductImageResponse> images = productImageRepository
                .findByProductIdAndActiveTrueOrderByDisplayOrderAsc(productId)
                .stream()
                .map(img -> ProductImageResponse.from(img, fileService.getUrl(img.getSisFile())))
                .toList();

        return ResponseEntity.ok(images);
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Deletar imagem de um produto")
    public ResponseEntity<Void> delete(
            @PathVariable Long productId,
            @PathVariable Long imageId) throws IOException {

        ProductImage productImage = productImageRepository.findById(imageId)
                .filter(img -> img.getProduct().getId().equals(productId))
                .filter(ProductImage::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));

        fileService.delete(productImage.getSisFile());

        productImage.setActive(false);
        productImageRepository.save(productImage);

        return ResponseEntity.noContent().build();
    }
}
