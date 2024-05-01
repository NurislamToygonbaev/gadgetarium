package gadgetarium.services;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.dto.response.ViewedProductsResponse;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;

import java.util.List;

public interface GadgetService {
    GadgetResponse getGadgetById(Long gadgetId);

    GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget);

    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    List<ViewedProductsResponse> viewedProduct();
}
