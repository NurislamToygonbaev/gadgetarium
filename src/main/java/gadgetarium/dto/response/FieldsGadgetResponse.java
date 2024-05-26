package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record FieldsGadgetResponse(
        String nameOfGadget,
        String memory,
        String color,
        int percent
) {
}
