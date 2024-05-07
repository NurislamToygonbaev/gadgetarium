package gadgetarium.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
@Builder
public record GadgetReviewsResponse(
        Long id,
        String image,
        String fullName,
        LocalDateTime dateTime,
        double rating,
        String description,
        String responseAdmin
){}
