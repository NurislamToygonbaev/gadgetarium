package gadgetarium.services;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.Sort;

import java.math.BigDecimal;

public interface GadgetService {
    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    PaginationSHowMoreGadget allGadgetsForEvery(Sort sort, Discount discount, Memory memory, Ram ram, BigDecimal costFrom, BigDecimal costUpTo, String colour, String brand, int page, int size);

    GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget);

    GadgetResponse getGadgetById(Long gadgetId);
}
