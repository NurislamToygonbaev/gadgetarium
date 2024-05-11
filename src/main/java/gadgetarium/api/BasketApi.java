package gadgetarium.api;

import gadgetarium.dto.request.BasketIdsRequest;
import gadgetarium.dto.response.GetBasketAmounts;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.GetAllBasketResponse;
import gadgetarium.dto.response.SumOrderWithGadgetResponse;
import gadgetarium.services.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
public class BasketApi {

    private final BasketService basketService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "все гаджеты в корзине", description = "авторизация: USER")
    @GetMapping("/get-all-basket")
    public List<GetAllBasketResponse> gelAllBasket() {
        return basketService.gelAllBasket();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "цены из корзины ", description = "авторизация: USER")
    @GetMapping("/all-amount-in-basket")
    public GetBasketAmounts allAmounts(BasketIdsRequest basketIdsRequest) {
        return basketService.allAmounts(basketIdsRequest);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Добавить в корзину", description = "авторизация: USER")
    @PostMapping("/add-to-basket/{gadgetId}")
    public HttpResponse addToBasket(@PathVariable Long gadgetId,
                                    @RequestParam(value = "quantity",required = false, defaultValue = "1") int quantity) {
        return basketService.addToBasket(gadgetId, quantity);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "удалить с корзины", description = "авторизация: USER")
    @DeleteMapping("/remove-count-from-basket/{gadgetId}")
    public HttpResponse removeFromBasket(@PathVariable Long gadgetId) {
        return basketService.removeFromBasket(gadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "удалить гаджет из корзины", description = "авторизация: USER")
    @DeleteMapping("/delete-from-basket/{gadgetId}")
    public HttpResponse deleteFromBasket(@PathVariable Long gadgetId) {
        return basketService.deleteFromBasket(gadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "удалить из корзины по выбранным гаджетом", description = "авторизация: USER")
    @DeleteMapping("/delete-all-from-basket")
    public HttpResponse deleteALlFromBasket(BasketIdsRequest basketIdsRequest) {
        return basketService.deleteALlFromBasket(basketIdsRequest);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "сумма заказа с гаджетом", description = "авторизация: USER")
    @GetMapping("/all-gadget-with-amounts-in-order")
    public SumOrderWithGadgetResponse sumOrderWithGadgets(BasketIdsRequest basketIdsRequest) {
        return basketService.sumOrderWithGadgets(basketIdsRequest);
    }
}
