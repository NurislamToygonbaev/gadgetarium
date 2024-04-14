package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.response.ResultPaginationGadget;

public interface GadgetJDBCTemplateRepository {
    ResultPaginationGadget getAll(PaginationRequest request);
}
