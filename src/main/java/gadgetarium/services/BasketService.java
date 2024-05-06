package gadgetarium.services;

import gadgetarium.dto.request.BasketIdsRequest;
import gadgetarium.dto.response.GetAllBasketResponse;
import gadgetarium.dto.response.GetBasketAmounts;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SumOrderWithGadgetResponse;

import java.util.List;

public interface BasketService {

    HttpResponse addToBasket(Long gadgetId, int quantity);

    HttpResponse removeFromBasket(Long gadgetId);

    HttpResponse deleteFromBasket(Long gadgetId);

    List<GetAllBasketResponse> gelAllBasket();

    GetBasketAmounts allAmounts(BasketIdsRequest basketIdsRequest);

    SumOrderWithGadgetResponse sumOrderWithGadgets(BasketIdsRequest basketIdsRequest);

    HttpResponse deleteALlFromBasket(BasketIdsRequest basketIdsRequest);
}
