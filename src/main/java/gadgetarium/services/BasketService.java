package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;

public interface BasketService {

    HttpResponse addToBasket(Long gadgetId, int quantity);

    HttpResponse removeFromBasket(Long gadgetId, int quantity);

    HttpResponse deleteFromBasket(Long gadgetId);
}
