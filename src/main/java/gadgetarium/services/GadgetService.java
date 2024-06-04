package gadgetarium.services;

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

import java.util.List;

import java.math.BigDecimal;

public interface GadgetService {
    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    PaginationSHowMoreGadget allGadgetsForEvery(Long catId, Sort sort, Discount discount, List<Memory> memory, List<Ram> ram, BigDecimal costFrom, BigDecimal costUpTo, List<String> colour, List<String> brand, int page, int size);

    GadgetResponse getGadgetById(Long gadgetId, String color, Memory memory, int quantity);

    List<ViewedProductsResponse> viewedProduct();
    HttpResponse addGadget(Long sunCategoryId, Long brandId, AddProductRequest addProductRequest);

    List<AddProductsResponse> getNewProducts();

    HttpResponse addPrice(List<Long> ids, BigDecimal price, int quantity);

    HttpResponse setPriceOneProduct(Long id, BigDecimal price);

    HttpResponse addDocument(Long gadgetId, ProductDocRequest productDocRequest) throws IOException;

    GadgetPaginationForMain mainPageDiscounts(int page, int size);

    GadgetPaginationForMain mainPageNews(int page, int size);

    GadgetPaginationForMain mainPageRecommend(int page, int size);

    GadgetDescriptionResponse getDescriptionGadget(Long id);

    GadgetCharacteristicsResponse getCharacteristicsGadget(Long id);

    List<GadgetReviewsResponse> getReviewsGadget(Long id);

    GadgetDeliveryPriceResponse getDeliveryPriceGadget(Long id);

    HttpResponse updateGadget(Long subGadgetId, GadgetNewDataRequest gadgetNewDataRequest);

    HttpResponse deleteGadget(Long subGadgetId);

    List<CatResponse> getCategories();

    List<CatResponse> getSubCategories(Long catId);

    HttpResponse setQuantityOneProduct(Long id, int quantity);

    List<String> getAllColours(Long gadgetId);

    List<Memory> getAllMemories(Long gadgetId);
    List<DetailsResponse> gadgetDetails();

    List<GadgetsResponse> globalSearch(String request);
}
