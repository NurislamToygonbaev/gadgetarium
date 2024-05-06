package gadgetarium.dto.response;

import lombok.Builder;

@Builder
public record GadgetResponseInOrder(
        Long id,
        String image,
        String nameOfGadget,
        String memory,
        String colour,
        Long article,
        int quantity
) {
}
