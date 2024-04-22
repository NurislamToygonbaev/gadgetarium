package gadgetarium.services;

import gadgetarium.dto.request.DiscountRequest;
import gadgetarium.dto.response.DiscountResponse;
import gadgetarium.entities.SubGadget;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {

    DiscountResponse create(List<Long> subGadgetsId, DiscountRequest discountRequest);
    public BigDecimal checkCurrentPrice(SubGadget subGadget);
}
