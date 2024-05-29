package gadgetarium.services;

import gadgetarium.dto.response.GetAllBasketResponse;
import gadgetarium.dto.response.GetBasketAmounts;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.SumOrderWithGadgetResponse;

import java.util.List;

public interface BasketService {

    HttpResponse addToBasket(Long subGadgetId, int quantity);

    HttpResponse removeFromBasket(Long gadgetId);

    HttpResponse deleteFromBasket(Long gadgetId);

    List<GetAllBasketResponse> gelAllBasket();

    GetBasketAmounts allAmounts(List<Long> ids);

    SumOrderWithGadgetResponse sumOrderWithGadgets(List<Long> ids);

    HttpResponse deleteALlFromBasket(List<Long> ids);
}
