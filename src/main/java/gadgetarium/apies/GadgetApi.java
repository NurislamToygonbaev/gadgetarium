package gadgetarium.apies;

import gadgetarium.dto.request.*;
import gadgetarium.dto.response.*;
import gadgetarium.enums.*;
import gadgetarium.exceptions.IOException;
import gadgetarium.services.GadgetService;
import gadgetarium.validations.price.PriceValidation;
import gadgetarium.validations.quantity.QuantityValidation;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public ResultPaginationGadget allGadgets(@RequestParam(required = false, defaultValue = "ALL_PRODUCTS")GetType getType,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) LocalDate startDate,
                                             @RequestParam(required = false) LocalDate endDate,
                                             @RequestParam(required = false) Sort sort,
                                             @RequestParam(required = false) Discount discount,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "size", defaultValue = "7") int size) {
        return gadgetService.getAll(getType, keyword, startDate, endDate, sort, discount, page, size);
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
    public GadgetResponse getGadget(@PathVariable Long gadgetId,
                                    @RequestParam(required = false) String color,
                                    @RequestParam(required = false) Memory memory,
                                    @RequestParam(required = false, defaultValue = "0") int quantity) {
        return gadgetService.getGadgetById(gadgetId, color, memory, quantity);
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
    public AddGadgetResponse addGadget(@PathVariable Long subCategoryId,
                                  @PathVariable Long brandId,
                                  @RequestBody @Valid AddProductRequest addProductRequest) {
        return gadgetService.addGadget(subCategoryId, brandId, addProductRequest);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Возвращение добавленных товаров.", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping("/get-new")
    public List<AddProductsResponse> getNewProducts(@RequestParam List<Long> ids) {
        return gadgetService.getNewProducts(ids);
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
    @Operation(summary = "Установить цены и количество на добавленные товары по одному", description = "Авторизация: АДМИНСТРАТОР")
    @PatchMapping("/price-quantity")
    public HttpResponse addPriceAndQuantity(@RequestBody @Valid List<SetPriceAndQuantityRequest> request){
        return gadgetService.addPriceAndQuantity(request);
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
    @PatchMapping("/set-document/{gadgetId}")
    public HttpResponse addDocument(@PathVariable Long gadgetId,
                                    @RequestBody ProductDocRequest productDocRequest) throws IOException {
        return gadgetService.addDocument(gadgetId, productDocRequest);
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
    public List<GadgetReviewsResponse> getReviewsGadget(@PathVariable Long id,
                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                        @RequestParam(value = "size", defaultValue = "3") int size) {
        return gadgetService.getReviewsGadget(id, page, size);
    }

    @Operation(summary = "Информация про доставка и оплата", description = "Авторизация: ВСЕ")
    @GetMapping("/delivery/{id}")
    public GadgetDeliveryPriceResponse getDeliveryPriceGadget(@PathVariable Long id) {
        return gadgetService.getDeliveryPriceGadget(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Обновление гаджета по ID", description = "Авторизация ADMIN")
    @PatchMapping("/update/{subGadgetId}")
    public HttpResponse updateGadget(@PathVariable Long subGadgetId,
                                     @RequestBody @Valid GadgetNewDataRequest gadgetNewDataRequest) {
        return gadgetService.updateGadget(subGadgetId, gadgetNewDataRequest);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Обновление фотографии гаджета по ID", description = "Авторизация ADMIN")
    @PatchMapping("/update-image/{subGadgetId}")
    public HttpResponse updateGadgetImages(@PathVariable Long subGadgetId,
                                     @RequestBody @Valid GadgetImagesRequest gadgetImagesRequest) {
        return gadgetService.updateGadgetImages(subGadgetId, gadgetImagesRequest);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Удаление гаджета по ID", description = "Авторизация ADMIN")
    @DeleteMapping("/{subGadgetId}")
    public HttpResponse deleteGadget(@PathVariable Long subGadgetId) {
        return gadgetService.deleteGadget(subGadgetId);
    }

    @Operation(summary = "Цвета гаджета", description = "Авторизация ВСЕ")
    @GetMapping("/{gadgetId}")
    public List<String> getAllColours(@PathVariable Long gadgetId) {
        return gadgetService.getAllColours(gadgetId);
    }

    @Operation(summary = "Памяти гаджета с цветом", description = "Авторизация ВСЕ")
    @GetMapping("/memories/{gadgetId}")
    public List<Memory> getAllMemories(@PathVariable Long gadgetId,
                                       @RequestParam String color){
        return gadgetService.getAllMemories(gadgetId, color);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Детали товаров", description = "Авторизация ADMIN")
    @GetMapping("/details")
    public List<DetailsResponse> gadgetDetails(){
        return gadgetService.gadgetDetails();
    }

    @Operation(summary = "Поиск всех гаджетов", description = "Авторизация: ВСЕ")
    @GetMapping("/global-search")
    public List<GadgetsResponse> globalSearch(@RequestParam String request){
        return gadgetService.globalSearch(request);
    }
}




