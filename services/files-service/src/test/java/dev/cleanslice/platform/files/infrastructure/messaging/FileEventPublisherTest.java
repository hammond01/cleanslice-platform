package dev.cleanslice.platform.files.infrastructure.messaging;

import dev.cleanslice.platform.common.events.FileUploadedEvent;
import dev.cleanslice.platform.files.domain.FileEntry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FileEventPublisherTest {

    @Test
    void shouldAddTypeHeaderWhenPublishingFileUploaded() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        FileEventPublisher publisher = new FileEventPublisher(kafkaTemplate);

        FileEntry fileEntry = FileEntry.create(UUID.randomUUID(), "file.txt", "text/plain", 1234L);
        publisher.publishFileUploaded(fileEntry);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> sent = captor.getValue();
        assertThat(sent.topic()).isEqualTo("files.events.v1");
        assertThat(new String(sent.headers().lastHeader("__TypeId__").value(), StandardCharsets.UTF_8))
                .isEqualTo(FileUploadedEvent.class.getName());
    }
}
