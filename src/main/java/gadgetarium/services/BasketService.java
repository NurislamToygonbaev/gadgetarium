package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;

public interface BasketService {
    HttpResponse addToBasket(Long gadgetId);
}
