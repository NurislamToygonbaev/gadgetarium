package gadgetarium.api;

import gadgetarium.dto.request.*;
import gadgetarium.dto.response.AddProductsResponse;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.exceptions.IOException;
import gadgetarium.services.GadgetService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gadget")
@Slf4j
public class GadgetAPI {

    private final GadgetService gadgetService;

    @Secured("ADMIN")
    @Operation(description = "Получение всех гаджетов!")
    @GetMapping("/get-all")
    public ResultPaginationGadget allGadgets(@RequestParam Sort sort,
                                             @RequestParam Discount discount,
                                             @RequestParam int page,
                                             @RequestParam int size) {
        return gadgetService.getAll(sort, discount, page, size);
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

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Выборка: Категерии, подкатегории, бренды. Добавление продукты.", description = "Авторизация ADMIN")
    @PostMapping("/add-gadget/{subCategoryId}/{brandId}")
    public HttpResponse addGadget(@PathVariable Long subCategoryId,
                                  @PathVariable Long brandId,
                                  @RequestBody AddProductRequest addProductRequest) {
        return gadgetService.addGadget(subCategoryId, brandId, addProductRequest);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Возвращение добавленных товаров.", description = "Авторизация ADMIN")
    @GetMapping("/get-new-products")
    public List<AddProductsResponse> getNewProducts() {
        return gadgetService.getNewProducts();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Установить цены на добавленные товары.", description = "Авторизация ADMIN")
    @PostMapping("/set-all-price")
    public HttpResponse addPrice(@RequestBody ProductsIdsRequest productsIds) {
        return gadgetService.addPrice(productsIds);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Установить цены по одному.", description = "Авторизация ADMIN")
    @PostMapping("/set-price")
    public HttpResponse addPrice(@RequestBody ProductPriceRequest productPriceRequest) {
        return gadgetService.setPriceOneProduct(productPriceRequest);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Добавление документа на товары ", description = "Авторизация ADMIN")
    @PostMapping("/set-document")
    public HttpResponse addDocument(ProductDocRequest productDocRequest) throws IOException {
        return gadgetService.addDocument(productDocRequest);
    }
}
