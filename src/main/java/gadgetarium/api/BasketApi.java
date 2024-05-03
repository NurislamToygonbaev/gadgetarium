package gadgetarium.api;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderAmountResponse;
import gadgetarium.services.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
public class BasketApi {

    private final BasketService basketService;

    //    @PreAuthorize("hasAuthority('USER')")
//    @Operation(summary = "сумма заказа в корзине", description = "авторизация: USER")
//    @GetMapping("/order-amount}")
//    public OrderAmountResponse orderAmount(){
//        return basketService.orderAmount();
//    }

//    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Добавить в корзину", description = "авторизация: USER")
    @PostMapping("/add-to-basket/{gadgetId}")
    public HttpResponse addToBasket(@PathVariable Long gadgetId,
                                    @RequestParam(value = "quantity", defaultValue = "1") int quantity){
       return basketService.addToBasket(gadgetId, quantity);
    }

    //    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "удалить с корзины", description = "авторизация: USER")
    @PostMapping("/remove-from-basket/{gadgetId}")
    public HttpResponse removeFromBasket(@PathVariable Long gadgetId,
                                    @RequestParam(value = "quantity", defaultValue = "1") int quantity){
        return basketService.removeFromBasket(gadgetId, quantity);
    }

    //    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "удалить все из корзины", description = "авторизация: USER")
    @PostMapping("/remove-from-basket/{gadgetId}")
    public HttpResponse deleteFromBasket(@PathVariable Long gadgetId){
        return basketService.deleteFromBasket(gadgetId);
    }
}
