package gadgetarium.services;

import gadgetarium.dto.request.DiscountRequest;
import gadgetarium.dto.response.DiscountResponse;

import java.util.List;

public interface DiscountService {

    DiscountResponse create(List<Long> gadgetId, DiscountRequest discountRequest);
}
