package dev.cleanslice.platform.product.infrastructure.messaging;

import dev.cleanslice.platform.common.events.ProductCreatedEvent;
import dev.cleanslice.platform.product.domain.Product;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProductEventPublisherTest {

    @Test
    void shouldAddTypeHeaderWhenPublishing() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        ProductEventPublisher publisher = new ProductEventPublisher(kafkaTemplate);

        Product product = new Product(UUID.randomUUID(), "name", "desc");
        publisher.publishProductCreated(product);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> sent = captor.getValue();
        assertThat(sent.topic()).isEqualTo("products.events.v1");
        assertThat(new String(sent.headers().lastHeader("__TypeId__").value(), StandardCharsets.UTF_8))
                .isEqualTo(ProductCreatedEvent.class.getName());
    }
}
