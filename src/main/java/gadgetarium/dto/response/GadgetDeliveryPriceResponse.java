package gadgetarium.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record GadgetDeliveryPriceResponse(
        BigDecimal deliveryPrice

) {
}
