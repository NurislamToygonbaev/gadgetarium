package gadgetarium.api;

import gadgetarium.dto.request.BasketIdsRequest;
import gadgetarium.dto.response.GetBasketAmounts;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.GetAllBasketResponse;
import gadgetarium.dto.response.SumOrderWithGadgetResponse;
import gadgetarium.services.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 100000L)
public class BasketApi {

    private final BasketService basketService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Все гаджеты в корзине", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/get-all-basket")
    public List<GetAllBasketResponse> gelAllBasket() {
        return basketService.gelAllBasket();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Цены из корзины ", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/all-amount-in-basket")
    public GetBasketAmounts allAmounts(BasketIdsRequest basketIdsRequest) {
        return basketService.allAmounts(basketIdsRequest);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Добавить в корзину", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PostMapping("/add-to-basket/{gadgetId}")
    public HttpResponse addToBasket(@PathVariable Long gadgetId,
                                    @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        return basketService.addToBasket(gadgetId, quantity);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Удалить с корзины", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @DeleteMapping("/remove-count-from-basket/{gadgetId}")
    public HttpResponse removeFromBasket(@PathVariable Long gadgetId) {
        return basketService.removeFromBasket(gadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Удалить гаджет из корзины", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @DeleteMapping("/delete-from-basket/{gadgetId}")
    public HttpResponse deleteFromBasket(@PathVariable Long gadgetId) {
        return basketService.deleteFromBasket(gadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Удалить из корзины по выбранным гаджетом", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @DeleteMapping("/delete-all-from-basket")
    public HttpResponse deleteALlFromBasket(BasketIdsRequest basketIdsRequest) {
        return basketService.deleteALlFromBasket(basketIdsRequest);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Сумма заказа с гаджетом", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/all-gadget-with-amounts-in-order")
    public SumOrderWithGadgetResponse sumOrderWithGadgets(BasketIdsRequest basketIdsRequest) {
        return basketService.sumOrderWithGadgets(basketIdsRequest);
    }
}
