package gadgetarium.api;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.services.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/basket")
@RequiredArgsConstructor
public class BasketApi {

    private final BasketService basketService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Добавить в корзину", description = "авторизация: USER")
    @PostMapping("/add-to-basket/{gadgetId}")
    public HttpResponse addToBasket(@PathVariable Long gadgetId){
       return basketService.addToBasket(gadgetId);
    }
}
