package gadgetarium.dto.response;

import gadgetarium.enums.Role;
import lombok.Builder;

@Builder
public record CurrentUserProfileResponse(
        String userName,
        String lastName,
        String email,
        String phoneNumber,
        String address,
        Role role,
        Long id,
        String token,
        HttpResponse httpResponse
) {}
