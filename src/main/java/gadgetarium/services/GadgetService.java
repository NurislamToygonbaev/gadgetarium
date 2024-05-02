package gadgetarium.services;

import gadgetarium.dto.request.AddProductRequest;
import gadgetarium.dto.request.ProductDocRequest;
import gadgetarium.dto.request.ProductPriceRequest;
import gadgetarium.dto.request.ProductsIdsRequest;
import gadgetarium.dto.response.*;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.exceptions.IOException;

import java.util.List;

public interface GadgetService {
    GadgetResponse getGadgetById(Long gadgetId);

    GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget);

    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    List<ViewedProductsResponse> viewedProduct();
    HttpResponse addGadget(Long sunCategoryId, Long brandId, AddProductRequest addProductRequest);

    List<AddProductsResponse> getNewProducts();

    HttpResponse addPrice(ProductsIdsRequest productsIds);

    HttpResponse setPriceOneProduct(ProductPriceRequest productPriceRequest);

    HttpResponse addDocument(ProductDocRequest productDocRequest) throws IOException;

    GadgetPaginationForMain mainPageDiscounts(int page, int size);

    GadgetPaginationForMain mainPageNews(int page, int size);

    GadgetPaginationForMain mainPageRecommend(int page, int size);
}
