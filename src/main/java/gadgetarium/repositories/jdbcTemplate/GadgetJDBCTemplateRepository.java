package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.request.SelectCategoryRequest;
import gadgetarium.dto.response.ComparedGadgetsResponse;
import gadgetarium.dto.response.ResultPaginationGadget;

public interface GadgetJDBCTemplateRepository {
    ResultPaginationGadget getAll(PaginationRequest request);

}
