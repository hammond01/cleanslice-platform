package dev.cleanslice.platform.product.infrastructure.rest;

import dev.cleanslice.platform.product.application.port.ProductRepositoryPort;
import dev.cleanslice.platform.product.application.usecase.AttachMediaUseCase;
import dev.cleanslice.platform.product.application.usecase.CreateProductUseCase;
import dev.cleanslice.platform.product.application.usecase.PublishProductUseCase;
import dev.cleanslice.platform.product.domain.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API controller for Product Service.
 * Infrastructure layer - delegates to use cases in application layer.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management API")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final PublishProductUseCase publishProductUseCase;
    private final AttachMediaUseCase attachMediaUseCase;
    private final ProductRepositoryPort productRepository;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            PublishProductUseCase publishProductUseCase,
            AttachMediaUseCase attachMediaUseCase,
            ProductRepositoryPort productRepository) {
        this.createProductUseCase = createProductUseCase;
        this.publishProductUseCase = publishProductUseCase;
        this.attachMediaUseCase = attachMediaUseCase;
        this.productRepository = productRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<Product> createProduct(
            @RequestBody CreateProductRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        Product product = createProductUseCase.execute(userId, request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getProduct(@PathVariable UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get all products by owner")
    public ResponseEntity<List<Product>> getProductsByOwner(@PathVariable UUID ownerId) {
        List<Product> products = productRepository.findByOwnerId(ownerId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/published")
    @Operation(summary = "Get all published products")
    public ResponseEntity<List<Product>> getPublishedProducts() {
        List<Product> products = productRepository.findPublishedProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a product")
    public ResponseEntity<Product> publishProduct(@PathVariable UUID id) {
        Product product = publishProductUseCase.execute(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/media")
    @Operation(summary = "Attach media to product")
    public ResponseEntity<Product> attachMedia(
            @PathVariable UUID id,
            @RequestBody AttachMediaRequest request) {
        
        Product product = attachMediaUseCase.execute(
                id,
                request.fileId(),
                request.altText(),
                request.isPrimary() != null ? request.isPrimary() : false
        );
        
        return ResponseEntity.ok(product);
    }

    // DTOs (Request/Response objects)
    public record CreateProductRequest(String name, String description) {}
    
    public record AttachMediaRequest(
            UUID fileId,
            String altText,
            Boolean isPrimary
    ) {}
}
