package gadgetarium.api;

import gadgetarium.dto.request.AddProductRequest;
import gadgetarium.dto.request.ProductDocRequest;
import gadgetarium.dto.request.ProductPriceRequest;
import gadgetarium.dto.request.ProductsIdsRequest;
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
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    @Operation(summary = "det-all-categories.", description = "Авторизация ADMIN")
    @GetMapping("/get-categories")
    public List<CatResponse> getCategories() {
        return gadgetService.getCategories();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "det-sub-categories.", description = "Авторизация ADMIN")
    @GetMapping("/{catId}/get-sub-categories")
    public List<CatResponse> getSubCategories(@PathVariable Long catId) {
        return gadgetService.getSubCategories(catId);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @Operation(summary = "Добавление документа на товары ", description = "Авторизация ADMIN")
    @PostMapping("/set-document")
    public HttpResponse addDocument(ProductDocRequest productDocRequest) throws IOException {
        return gadgetService.addDocument(productDocRequest);
    }

    @Operation(summary = "все Гаджеты по акции", description = "авторизация: все")
    @GetMapping("/all-gadgets-with-discounts")
    public GadgetPaginationForMain mainPageDiscounts(@RequestParam(value = "page", defaultValue = "1") int page,
                                                     @RequestParam(value = "size", defaultValue = "5") int size) {
        return gadgetService.mainPageDiscounts(page, size);
    }

    @Operation(summary = "новинки", description = "авторизация: все")
    @GetMapping("/all-new-gadgets")
    public GadgetPaginationForMain mainPageNews(@RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "size", defaultValue = "5") int size) {
        return gadgetService.mainPageNews(page, size);
    }

    @Operation(summary = "рекомендуемые", description = "авторизация: все")
    @GetMapping("/all-gadgets-recommend")
    public GadgetPaginationForMain mainPageRecommend(@RequestParam(value = "page", defaultValue = "1") int page,
                                                     @RequestParam(value = "size", defaultValue = "5") int size) {
        return gadgetService.mainPageRecommend(page, size);
    }

    @Operation(summary = "Посмотреть описание гаджета", description = "авторизация: все")
    @GetMapping("/see-gadget-description/{id}")
    public GadgetDescriptionResponse getDescriptionGadget(@PathVariable Long id) {
        return gadgetService.getDescriptionGadget(id);
    }

    @Operation(summary = "Посмотреть характеристики гаджета", description = "авторизация: все")
    @GetMapping("/see-gadget-characteristics/{id}")
    public GadgetCharacteristicsResponse getCharacteristicsGadget(@PathVariable Long id) {
        return gadgetService.getCharacteristicsGadget(id);
    }

    @Operation(summary = "Посмотреть отзывы гаджета", description = "авторизация: все")
    @GetMapping("/see-gadget-reviews/{id}")
    public List<GadgetReviewsResponse> getReviewsGadget(@PathVariable Long id) {
        return gadgetService.getReviewsGadget(id);
    }

    @Operation(summary = "Информация про доставка и оплата", description = "авторизация: все")
    @GetMapping("/see-gadget-delivery/{id}")
    public GadgetDeliveryPriceResponse getDeliveryPriceGadget(@PathVariable Long id) {
        return gadgetService.getDeliveryPriceGadget(id);
    }

    @Operation(summary = "Метод для скачивание PDF", description = "авторизация: все")
    @GetMapping("/download-doc/{key}/{id}")
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

}




