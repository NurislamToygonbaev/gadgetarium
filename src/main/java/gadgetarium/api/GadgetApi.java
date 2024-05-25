package gadgetarium.api;

import gadgetarium.dto.request.*;
import gadgetarium.dto.response.AddProductsResponse;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.dto.response.*;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.Sort;
import gadgetarium.exceptions.IOException;
import gadgetarium.services.GadgetService;
import gadgetarium.validation.price.PriceValidation;
import gadgetarium.validation.quantity.QuantityValidation;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gadget")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GadgetApi {

    private final GadgetService gadgetService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Все Гаджеты ", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping
    public ResultPaginationGadget allGadgets(@RequestParam(required = false) Sort sort,
                                             @RequestParam(required = false) Discount discount,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "size", defaultValue = "7") int size) {
        return gadgetService.getAll(sort, discount, page, size);
    }

    @Operation(summary = "Все гаджеты с фильтрацией", description = "Авторизация: ВСЕ")
    @GetMapping("/{catId}/filter")
    public PaginationSHowMoreGadget allGadgetsForEvery(@PathVariable Long catId,
                                                       @RequestParam(required = false) Sort sort,
                                                       @RequestParam(required = false) Discount discount,
                                                       @RequestParam(required = false) List<String> brand,
                                                       @RequestParam(required = false) List<String> colour,
                                                       @RequestParam(required = false) BigDecimal costFrom,
                                                       @RequestParam(required = false) BigDecimal costUpTo,
                                                       @RequestParam(required = false) List<Memory> memory,
                                                       @RequestParam(required = false) List<Ram> ram,
                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                       @RequestParam(value = "size", defaultValue = "12") int size) {
        return gadgetService.allGadgetsForEvery(catId, sort, discount, memory, ram, costFrom, costUpTo, colour, brand, page, size);
    }

    @Operation(summary = "Получение гаджета по ID.", description = "Авторизация: ВСЕ")
    @GetMapping("/by-id/{gadgetId}")
    public GadgetResponse getGadget(@PathVariable Long gadgetId) {
        return gadgetService.getGadgetById(gadgetId);
    }

    @Operation(summary = "Полученный гаджет, выбор по цвету.", description = "Авторизация: ВСЕ")
    @GetMapping("/{gadgetId}/colour")
    public GadgetResponse getGadgetByColour(@PathVariable Long gadgetId,
                                            @RequestParam String colour) {
        return gadgetService.getGadgetSelectColour(gadgetId, colour);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Просмотренные гаджеты.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/viewed-products")
    public List<ViewedProductsResponse> viewedProduct() {
        return gadgetService.viewedProduct();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = " Добавление продукта ", description = "Авторизация: АДМИНСТРАТОР")
    @PostMapping("/{subCategoryId}/{brandId}")
    public HttpResponse addGadget(@PathVariable Long subCategoryId,
                                  @PathVariable Long brandId,
                                  @RequestBody @Valid AddProductRequest addProductRequest) {
        return gadgetService.addGadget(subCategoryId, brandId, addProductRequest);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Возвращение добавленных товаров.", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping("/get-new")
    public List<AddProductsResponse> getNewProducts() {
        return gadgetService.getNewProducts();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Установить цены и количество на добавленные товары.", description = "Авторизация: АДМИНСТРАТОР")
    @PatchMapping("/set-all-price")
    public HttpResponse addPrice(@RequestParam @PriceValidation BigDecimal price,
                                 @RequestParam @QuantityValidation int quantity,
                                 @RequestParam List<Long> ids) {
        return gadgetService.addPrice(ids, price, quantity);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Установить цены по одному.", description = "Авторизация: АДМИНСТРАТОР")
    @PatchMapping("/{id}/set-price")
    public HttpResponse addPrice(@RequestParam @PriceValidation BigDecimal price,
                                 @PathVariable Long id) {
        return gadgetService.setPriceOneProduct(id, price);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Установить количество по одному.", description = "Авторизация: АДМИНСТРАТОР")
    @PatchMapping("/{id}/set-quantity")
    public HttpResponse addQuantity(@RequestParam @QuantityValidation int quantity,
                                    @PathVariable Long id) {
        return gadgetService.setQuantityOneProduct(id, quantity);
    }

    @Operation(summary = " Все категории", description = "Авторизация: ВСЕ")
    @GetMapping("/categories")
    public List<CatResponse> getCategories() {
        return gadgetService.getCategories();
    }

    @Operation(summary = " Все подкатегории", description = "Авторизация ВСЕ")
    @GetMapping("/{catId}/sub-categories")
    public List<CatResponse> getSubCategories(@PathVariable Long catId) {
        return gadgetService.getSubCategories(catId);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Добавление документа на товары ", description = "Авторизация ADMIN")
    @PatchMapping("/set-document")
    public HttpResponse addDocument(@RequestBody ProductDocRequest productDocRequest) throws IOException {
        return gadgetService.addDocument(productDocRequest);
    }

    @Operation(summary = "Все Гаджеты по акции", description = "Авторизация: ВСЕ")
    @GetMapping("/discounts")
    public GadgetPaginationForMain mainPageDiscounts(@RequestParam(value = "page", defaultValue = "1") int page,
                                                     @RequestParam(value = "size", defaultValue = "5") int size) {
        return gadgetService.mainPageDiscounts(page, size);
    }

    @Operation(summary = "Новинки", description = "Авторизация: ВСЕ")
    @GetMapping("/new")
    public GadgetPaginationForMain mainPageNews(@RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "size", defaultValue = "5") int size) {
        return gadgetService.mainPageNews(page, size);
    }

    @Operation(summary = "Рекомендуемые", description = "Авторизация: ВСЕ")
    @GetMapping("/recommend")
    public GadgetPaginationForMain mainPageRecommend(@RequestParam(value = "page", defaultValue = "1") int page,
                                                     @RequestParam(value = "size", defaultValue = "5") int size) {
        return gadgetService.mainPageRecommend(page, size);
    }

    @Operation(summary = "Посмотреть описание гаджета", description = "Авторизация: ВСЕ")
    @GetMapping("/description/{id}")
    public GadgetDescriptionResponse getDescriptionGadget(@PathVariable Long id) {
        return gadgetService.getDescriptionGadget(id);
    }

    @Operation(summary = "Посмотреть характеристики гаджета", description = "Авторизация: ВСЕ")
    @GetMapping("/characteristics/{id}")
    public GadgetCharacteristicsResponse getCharacteristicsGadget(@PathVariable Long id) {
        return gadgetService.getCharacteristicsGadget(id);
    }

    @Operation(summary = "Посмотреть отзывы гаджета", description = "Авторизация: ВСЕ")
    @GetMapping("/reviews/{id}")
    public List<GadgetReviewsResponse> getReviewsGadget(@PathVariable Long id) {
        return gadgetService.getReviewsGadget(id);
    }

    @Operation(summary = "Информация про доставка и оплата", description = "Авторизация: ВСЕ")
    @GetMapping("/delivery/{id}")
    public GadgetDeliveryPriceResponse getDeliveryPriceGadget(@PathVariable Long id) {
        return gadgetService.getDeliveryPriceGadget(id);
    }

    @Operation(summary = "Метод для скачивание PDF", description = "Авторизация: ВСЕ")
    @GetMapping("/doc/{key}/{id}")
    public ResponseEntity<ByteArrayResource> downloadPDF(@PathVariable String key,
                                                         @PathVariable Long id) {
        byte[] data = gadgetService.downloadFile(key, id);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + key + "\"")
                .body(resource);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Обновление гаджета по ID", description = "Авторизация ADMIN")
    @PutMapping("/{subGadgetId}")
    public HttpResponse updateGadget(@PathVariable Long subGadgetId,
                                     @RequestBody @Valid GadgetNewDataRequest gadgetNewDataRequest) {
        return gadgetService.updateGadget(subGadgetId, gadgetNewDataRequest);
    }


    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Удаление гаджета по ID", description = "Авторизация ADMIN")
    @DeleteMapping("/{gadgetID}")
    public HttpResponse deleteGadget(@PathVariable Long gadgetID) {
        return gadgetService.deleteGadget(gadgetID);
    }
}




