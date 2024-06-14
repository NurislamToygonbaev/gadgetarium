package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record UserProfileResponse(
        String firsName,
        String lastName,
        String email,
        String phoneNumber,
        String address,
        String image
){
}
