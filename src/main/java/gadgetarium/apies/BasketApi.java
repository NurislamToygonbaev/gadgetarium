package gadgetarium.apies;

import gadgetarium.dto.response.GetBasketAmounts;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.GetAllBasketResponse;
import gadgetarium.dto.response.SumOrderWithGadgetResponse;
import gadgetarium.services.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BasketApi {

    private final BasketService basketService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Все гаджеты в корзине", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping
    public List<GetAllBasketResponse> gelAllBasket() {
        return basketService.gelAllBasket();
    }

    @Cacheable("AllAmountInBasketCache")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Цены из корзины ", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/all-amount-in-basket")
    public GetBasketAmounts allAmounts(@RequestParam List<Long> ids) {
        return basketService.allAmounts(ids);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Добавить в корзину", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PatchMapping("/{subGadgetId}")
    public HttpResponse addToBasket(@PathVariable Long subGadgetId,
                                    @RequestParam(value = "quantity",required = false, defaultValue = "1") int quantity) {
        return basketService.addToBasket(subGadgetId, quantity);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Удалить с корзины", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @DeleteMapping("/remove-count/{gadgetId}")
    public HttpResponse removeFromBasket(@PathVariable Long gadgetId) {
        return basketService.removeFromBasket(gadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Удалить гаджет из корзины", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @DeleteMapping("/{gadgetId}")
    public HttpResponse deleteFromBasket(@PathVariable Long gadgetId) {
        return basketService.deleteFromBasket(gadgetId);
    }

    @CacheEvict("AllAmountInBasketCache")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Удалить из корзины по выбранным гаджетом", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @DeleteMapping("/delete-all")
    public HttpResponse deleteALlFromBasket(@RequestParam List<Long> ids) {
        return basketService.deleteALlFromBasket(ids);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Сумма заказа с гаджетом", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/order-amounts")
    public SumOrderWithGadgetResponse sumOrderWithGadgets(@RequestParam List<Long> ids) {
        return basketService.sumOrderWithGadgets(ids);
    }
}
