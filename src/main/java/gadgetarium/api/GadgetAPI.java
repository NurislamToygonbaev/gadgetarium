package gadgetarium.api;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.Sort;
import gadgetarium.services.GadgetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gadget")
@Slf4j
public class GadgetAPI {

    private final GadgetService gadgetService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "get all gadgets", description = "авторизация: АДМИН")
    @GetMapping("/get-all")
    public ResultPaginationGadget allGadgets(@RequestParam(required = false) Sort sort,
                                             @RequestParam(required = false) Discount discount,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "size", defaultValue = "7") int size) {
        return gadgetService.getAll(sort, discount, page, size);
    }

    @Operation(summary = "все гаджеты с фильтрацией", description = "авторизация: ВСЕ")
    @GetMapping("/all-gadgets")
    public PaginationSHowMoreGadget allGadgetsForEvery(@RequestParam(required = false) Sort sort,
                                                       @RequestParam(required = false) Discount discount,
                                                       @RequestParam(required = false) Memory memory,
                                                       @RequestParam(required = false) Ram ram,
                                                       @RequestParam(required = false) BigDecimal costFrom,
                                                       @RequestParam(required = false) BigDecimal costUpTo,
                                                       @RequestParam(required = false) String colour,
                                                       @RequestParam(required = false) String brand,
                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                       @RequestParam(value = "size", defaultValue = "12") int size) {
        return gadgetService.allGadgetsForEvery(sort, discount, memory, ram, costFrom, costUpTo, colour, brand, page, size);
    }

    @Secured("ADMIN")
    @Operation(description = "Получение гаджета по ID")
    @GetMapping("/get-gadget/{gadgetId}")
    public GadgetResponse getGadget(@PathVariable Long gadgetId) {
        return gadgetService.getGadgetById(gadgetId);
    }

    @Secured("ADMIN")
    @Operation(description = "Полученный гаджет, выбор по цвету")
    @GetMapping("/select-colour")
    public GadgetResponse getGadgetByColour(@RequestParam String colour,
                                            @RequestParam String nameOfGadget) {
        return gadgetService.getGadgetSelectColour(colour, nameOfGadget);
    }
}
