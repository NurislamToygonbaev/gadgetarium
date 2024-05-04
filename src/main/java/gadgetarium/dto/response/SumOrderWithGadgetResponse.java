package gadgetarium.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SumOrderWithGadgetResponse(
        GetBasketAmounts basketAmounts,
        List<GadgetResponseInOrder> gadgetResponse
) {
}
