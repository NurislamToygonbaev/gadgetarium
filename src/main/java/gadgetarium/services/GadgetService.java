package gadgetarium.services;

import gadgetarium.dto.request.*;
import gadgetarium.dto.response.AddProductsResponse;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.dto.response.*;
import gadgetarium.enums.*;
import gadgetarium.exceptions.IOException;

import java.time.LocalDate;
import java.util.List;

import java.math.BigDecimal;

public interface GadgetService {
    ResultPaginationGadget getAll(GetType getType, String keyword, LocalDate startDate, LocalDate endDate, Sort sort, Discount discount, int page, int size);

    PaginationSHowMoreGadget allGadgetsForEvery(Long catId, Sort sort, Discount discount, List<String> memory, List<String> ram, BigDecimal costFrom, BigDecimal costUpTo, List<String> colour, List<String> brand, int page, int size);

    GadgetResponse getGadgetById(Long gadgetId, String color, String memory, int quantity);

    List<ViewedProductsResponse> viewedProduct();
    AddGadgetResponse addGadget(Long sunCategoryId, Long brandId, AddProductRequest addProductRequest);

    List<AddProductsResponse> getNewProducts(List<Long> ids);

    HttpResponse addPrice(List<Long> ids, BigDecimal price, int quantity);

    HttpResponse addDocument(Long gadgetId, ProductDocRequest productDocRequest) throws IOException;

    GadgetPaginationForMain mainPageDiscounts(int page, int size);

    GadgetPaginationForMain mainPageNews(int page, int size);

    GadgetPaginationForMain mainPageRecommend(int page, int size);

    GadgetDescriptionResponse getDescriptionGadget(Long id);

    GadgetCharacteristicsResponse getCharacteristicsGadget(Long id);

    List<GadgetReviewsResponse> getReviewsGadget(Long id, int page, int size);

    GadgetDeliveryPriceResponse getDeliveryPriceGadget(Long id);

    HttpResponse updateGadget(Long subGadgetId, GadgetNewDataRequest gadgetNewDataRequest);

    HttpResponse deleteGadget(Long subGadgetId);

    List<CatResponse> getCategories();

    List<CatResponse> getSubCategories(Long catId);

    List<String> getAllColours(Long gadgetId);

    List<String> getAllMemories(Long gadgetId, String color);
    List<DetailsResponse> gadgetDetails(Long gadgetId);

    List<GadgetsResponse> globalSearch(String request);

    HttpResponse updateGadgetImages(Long subGadgetId, GadgetImagesRequest gadgetImagesRequest);

    HttpResponse addPriceAndQuantity(List<SetPriceAndQuantityRequest> request);
}
