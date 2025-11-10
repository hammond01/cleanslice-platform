package dev.cleanslice.platform.product.application;

import dev.cleanslice.platform.product.domain.Media;
import dev.cleanslice.platform.product.adapters.out.jpa.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@SuppressWarnings("null")
public class AttachMediaUseCase {

    private final ProductRepository productRepository;

    public AttachMediaUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID productId, UUID fileId, String altText, Integer sortOrder, Boolean isPrimary) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // If this is primary, unset other primary media
        if (Boolean.TRUE.equals(isPrimary)) {
            product.getMediaList().forEach(m -> m.setIsPrimary(false));
        }

        var media = new Media(fileId, altText, sortOrder, isPrimary);
        product.addMedia(media);

        productRepository.save(product);
    }
}
