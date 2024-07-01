package gadgetarium.dto.response;

import lombok.Builder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

@Builder
public record DownloadPdfResponse(
        ResponseEntity<ByteArrayResource> response
) {
}
