package gadgetarium.api;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.dto.response.ViewedProductsResponse;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.services.GadgetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gadget")
@Slf4j
public class GadgetAPI {

    private final GadgetService gadgetService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "все Гаджеты ", description = "авторизация: АДМИН")
    @GetMapping("/get-all")
    public ResultPaginationGadget allGadgets(@RequestParam(required = false) Sort sort,
                                             @RequestParam(required = false) Discount discount,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "size", defaultValue = "7") int size) {
        return gadgetService.getAll(sort, discount, page, size);
    }

    @PreAuthorize("hasAnyAuthority({'ADMIN', 'USER'})")
    @Operation(summary = "Получение гаджета по ID.", description = "авторизация: АДМИН,ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/get-gadget/{gadgetId}")
    public GadgetResponse getGadget(@PathVariable Long gadgetId) {
        return gadgetService.getGadgetById(gadgetId);
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Полученный гаджет, выбор по цвету.", description = "авторизация: АДМИН")
    @GetMapping("/select-colour")
    public GadgetResponse getGadgetByColour(@RequestParam String colour,
                                            @RequestParam String nameOfGadget) {
        return gadgetService.getGadgetSelectColour(colour, nameOfGadget);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Просмотренные гаджеты.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/viewed-products")
    public List<ViewedProductsResponse> viewedProduct() {
        return gadgetService.viewedProduct();
    }
}




