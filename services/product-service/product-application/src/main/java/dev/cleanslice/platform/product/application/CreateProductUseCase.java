package dev.cleanslice.platform.product.application;

import dev.cleanslice.platform.product.domain.Product;
import dev.cleanslice.platform.product.domain.Variant;
import dev.cleanslice.platform.product.adapters.out.jpa.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@SuppressWarnings("null")
public class CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product execute(UUID ownerId, String name, String description) {
        var product = new Product(ownerId, name, description);
        
        // Create default variant
        var defaultVariant = new Variant("Default", null, 0.0, 0);
        product.addVariant(defaultVariant);
        
        return productRepository.save(product);
    }
}
