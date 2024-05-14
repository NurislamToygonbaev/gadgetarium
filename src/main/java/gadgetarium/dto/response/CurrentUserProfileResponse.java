package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record CurrentUserProfileResponse(
        String userName,
        String lastName,
        String email,
        String phoneNumber,
        String address
) {}
