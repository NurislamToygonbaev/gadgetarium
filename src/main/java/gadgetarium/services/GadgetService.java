package gadgetarium.services;

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

import java.util.List;

import java.math.BigDecimal;

public interface GadgetService {
    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    PaginationSHowMoreGadget allGadgetsForEvery(Sort sort, Discount discount, Memory memory, Ram ram, BigDecimal costFrom, BigDecimal costUpTo, String colour, String brand, int page, int size);

    GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget);

    GadgetResponse getGadgetById(Long gadgetId);

    List<ViewedProductsResponse> viewedProduct();
    HttpResponse addGadget(Long sunCategoryId, Long brandId, AddProductRequest addProductRequest);

    List<AddProductsResponse> getNewProducts();

    HttpResponse addPrice(ProductsIdsRequest productsIds);

    HttpResponse setPriceOneProduct(ProductPriceRequest productPriceRequest);

    HttpResponse addDocument(ProductDocRequest productDocRequest) throws IOException;

    GadgetPaginationForMain mainPageDiscounts(int page, int size);

    GadgetPaginationForMain mainPageNews(int page, int size);

    GadgetPaginationForMain mainPageRecommend(int page, int size);

    GadgetDescriptionResponse getDescriptionGadget(Long id);

    GadgetCharacteristicsResponse getCharacteristicsGadget(Long id);

    List<GadgetReviewsResponse> getReviewsGadget(Long id);

    GadgetDeliveryPriceResponse getDeliveryPriceGadget(Long id);

    byte[] downloadFile(String key, Long gadgetId);

    List<CatResponse> getCategories();

    List<CatResponse> getSubCategories(Long catId);
}
