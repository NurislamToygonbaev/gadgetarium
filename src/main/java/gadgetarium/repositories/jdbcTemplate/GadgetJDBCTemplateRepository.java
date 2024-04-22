package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;

public interface GadgetJDBCTemplateRepository {
    ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size);
}
