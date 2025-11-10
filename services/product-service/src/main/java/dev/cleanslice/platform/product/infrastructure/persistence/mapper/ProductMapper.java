package dev.cleanslice.platform.product.infrastructure.persistence.mapper;

import dev.cleanslice.platform.product.domain.Product;
import dev.cleanslice.platform.product.domain.Variant;
import dev.cleanslice.platform.product.domain.Media;
import dev.cleanslice.platform.product.infrastructure.persistence.entity.ProductEntity;
import dev.cleanslice.platform.product.infrastructure.persistence.entity.VariantEntity;
import dev.cleanslice.platform.product.infrastructure.persistence.entity.MediaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert between domain models and JPA entities.
 * This is the KEY to keeping domain clean - we convert at infrastructure boundary.
 */
@Component
public class ProductMapper {

    /**
     * Convert domain Product to JPA ProductEntity.
     */
    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }

        ProductEntity entity = ProductEntity.builder()
                .id(product.getId())
                .ownerId(product.getOwnerId())
                .name(product.getName())
                .description(product.getDescription())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .variants(new ArrayList<>())
                .mediaList(new ArrayList<>())
                .build();

        // Map variants
        List<VariantEntity> variantEntities = product.getVariants().stream()
                .map(variant -> toVariantEntity(variant, entity))
                .collect(Collectors.toList());
        entity.setVariants(variantEntities);

        // Map media
        List<MediaEntity> mediaEntities = product.getMediaList().stream()
                .map(media -> toMediaEntity(media, entity))
                .collect(Collectors.toList());
        entity.setMediaList(mediaEntities);

        return entity;
    }

    /**
     * Convert JPA ProductEntity to domain Product.
     */
    public Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }

        // Convert variants
        List<Variant> variants = entity.getVariants().stream()
                .map(this::toVariantDomain)
                .collect(Collectors.toList());

        // Convert media
        List<Media> mediaList = entity.getMediaList().stream()
                .map(this::toMediaDomain)
                .collect(Collectors.toList());

        return new Product(
                entity.getId(),
                entity.getOwnerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                variants,
                mediaList,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private VariantEntity toVariantEntity(Variant variant, ProductEntity product) {
        return VariantEntity.builder()
                .id(variant.getId())
                .product(product)
                .name(variant.getName())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stock(variant.getStock())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }

    private Variant toVariantDomain(VariantEntity entity) {
        return new Variant(
                entity.getId(),
                entity.getName(),
                entity.getSku(),
                entity.getPrice(),
                entity.getStock(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private MediaEntity toMediaEntity(Media media, ProductEntity product) {
        return MediaEntity.builder()
                .id(media.getId())
                .product(product)
                .fileId(media.getFileId())
                .altText(media.getAltText())
                .sortOrder(media.getSortOrder())
                .isPrimary(media.isPrimary())
                .createdAt(media.getCreatedAt())
                .build();
    }

    private Media toMediaDomain(MediaEntity entity) {
        return new Media(
                entity.getId(),
                entity.getFileId(),
                entity.getAltText(),
                entity.getSortOrder(),
                entity.getIsPrimary(),
                entity.getCreatedAt()
        );
    }
}
