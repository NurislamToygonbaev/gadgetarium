package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.response.DetailsResponse;
import gadgetarium.dto.response.GadgetPaginationForMain;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.dto.response.*;
import gadgetarium.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GadgetJDBCTemplateRepository {

    ResultPaginationGadget getAll(GetType getType, String keyword, LocalDate startDate, LocalDate endDate, Sort sort, Discount discount, int page, int size);

    PaginationSHowMoreGadget allGadgetsForEvery(Long catId, Sort sort, Discount discount, List<Memory> memory, List<Ram> ram, BigDecimal costFrom, BigDecimal costUpTo, List<String> colour, List<String> brand, int page, int size);

    GadgetPaginationForMain mainPageDiscounts(int page, int size);

    GadgetPaginationForMain mainPageNews(int page, int size);

    GadgetPaginationForMain mainPageRecommend(int page, int size);

    List<DetailsResponse> gadgetDetails(Long gadgetId);

    List<GadgetsResponse> globalSearch(String request);

    GadgetResponse getGadgetById(Long gadgetId, String color, Memory memory, int quantity);

    List<GadgetReviewsResponse> getReviewsGadget(Long id, int page, int size);
}
