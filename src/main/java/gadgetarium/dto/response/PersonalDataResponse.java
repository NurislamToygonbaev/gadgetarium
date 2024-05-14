package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record PersonalDataResponse(
        String firsName,
        String lastName,
        String email,
        String phoneNumber,
        String address
) {
}
