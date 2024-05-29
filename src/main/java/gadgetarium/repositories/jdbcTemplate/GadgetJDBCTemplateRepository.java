package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.response.GadgetPaginationForMain;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.Sort;

import java.math.BigDecimal;
import java.util.List;

public interface GadgetJDBCTemplateRepository {

    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    PaginationSHowMoreGadget allGadgetsForEvery(Long catId, Sort sort, Discount discount, List<Memory> memory, List<Ram> ram, BigDecimal costFrom, BigDecimal costUpTo, List<String> colour, List<String> brand, int page, int size);

    GadgetPaginationForMain mainPageDiscounts(int page, int size);

    GadgetPaginationForMain mainPageNews(int page, int size);

    GadgetPaginationForMain mainPageRecommend(int page, int size);

    List<GadgetResponse> globalSearch(String request);
}
