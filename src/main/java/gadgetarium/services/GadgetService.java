package gadgetarium.services;

import gadgetarium.dto.request.AddProductRequest;
import gadgetarium.dto.request.ProductDocRequest;
import gadgetarium.dto.request.ProductPriceRequest;
import gadgetarium.dto.request.ProductsIdsRequest;
import gadgetarium.dto.response.AddProductsResponse;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.exceptions.IOException;

import java.util.List;

public interface GadgetService {
    GadgetResponse getGadgetById(Long gadgetId);

    GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget);

    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    HttpResponse addGadget(Long sunCategoryId, Long brandId, AddProductRequest addProductRequest);


    List<AddProductsResponse> getNewProducts();

    HttpResponse addPrice(ProductsIdsRequest productsIds);

    HttpResponse setPriceOneProduct(ProductPriceRequest productPriceRequest);

    HttpResponse addDocument(ProductDocRequest productDocRequest) throws IOException;
}
