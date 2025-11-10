package dev.cleanslice.platform.product.adapters.in.rest;

import dev.cleanslice.platform.product.application.AttachMediaUseCase;
import dev.cleanslice.platform.product.application.CreateProductUseCase;
import dev.cleanslice.platform.product.domain.Product;
import dev.cleanslice.platform.product.adapters.out.jpa.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@SuppressWarnings("null")
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management API")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final AttachMediaUseCase attachMediaUseCase;
    private final ProductRepository productRepository;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            AttachMediaUseCase attachMediaUseCase,
            ProductRepository productRepository) {
        this.createProductUseCase = createProductUseCase;
        this.attachMediaUseCase = attachMediaUseCase;
        this.productRepository = productRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<Product> createProduct(
            @RequestBody CreateProductRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        var product = createProductUseCase.execute(userId, request.name(), request.description());
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getProduct(@PathVariable UUID id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{id}/media")
    @Operation(summary = "Attach media to product")
    public ResponseEntity<Void> attachMedia(
            @PathVariable UUID id,
            @RequestBody AttachMediaRequest request) {
        
        attachMediaUseCase.execute(
                id,
                request.fileId(),
                request.altText(),
                request.sortOrder(),
                request.isPrimary()
        );
        
        return ResponseEntity.ok().build();
    }

    // DTOs
    public record CreateProductRequest(String name, String description) {}
    
    public record AttachMediaRequest(
            UUID fileId,
            String altText,
            Integer sortOrder,
            Boolean isPrimary
    ) {}
}
