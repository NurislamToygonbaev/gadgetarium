package gadgetarium.dto.response;

import gadgetarium.enums.Role;
import lombok.Builder;

@Builder
public record SignResponse(
        Long id,
        String email,
        Role role,
        String phoneNumber,
        String token,
        HttpResponse response
) {
}
