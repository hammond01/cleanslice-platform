package dev.cleanslice.platform.files.application.usecase;

import dev.cleanslice.platform.files.application.port.FileVersionRepositoryPort;
import dev.cleanslice.platform.files.domain.FileVersion;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetFileVersionUseCaseTest {

    @Test
    void shouldReturnFileVersionWhenFound() {
        var repo = mock(FileVersionRepositoryPort.class);
        var id = UUID.randomUUID();
        var fileId = UUID.randomUUID();
        var version = new FileVersion(id, fileId, 1, "test.txt", "text/plain", 100, fileId.toString(), java.time.Instant.now(), UUID.randomUUID());
        when(repo.findById(id)).thenReturn(Optional.of(version));

        var usecase = new GetFileVersionUseCase(repo);
        var result = usecase.execute(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getVersionNumber()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("test.txt");
    }

    @Test
    void shouldThrowWhenNotFound() {
        var repo = mock(FileVersionRepositoryPort.class);
        var id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        var usecase = new GetFileVersionUseCase(repo);
        assertThatThrownBy(() -> usecase.execute(id)).isInstanceOf(IllegalArgumentException.class);
    }
}
