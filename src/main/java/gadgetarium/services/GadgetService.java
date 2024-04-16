package gadgetarium.services;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;

public interface GadgetService {
    GadgetResponse getGadgetById(Long id);

    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);
}
