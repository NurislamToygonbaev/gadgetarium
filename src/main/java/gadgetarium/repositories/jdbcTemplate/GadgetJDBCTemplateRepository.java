package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.request.FeedbackTypeRequest;
import gadgetarium.dto.response.AllFeedbackResponse;
import gadgetarium.dto.response.GadgetPaginationForMain;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;

public interface GadgetJDBCTemplateRepository {

    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);

    GadgetPaginationForMain mainPageDiscounts(int page, int size);

    GadgetPaginationForMain mainPageNews(int page, int size);

    GadgetPaginationForMain mainPageRecommend(int page, int size);
}
