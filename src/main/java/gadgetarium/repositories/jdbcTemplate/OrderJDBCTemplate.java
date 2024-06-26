package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.response.CompareResponses;
import gadgetarium.dto.response.OrderPagination;
import gadgetarium.dto.response.OrderResponseFindById;
import gadgetarium.enums.GadgetType;
import gadgetarium.enums.Status;

import java.time.LocalDate;
import java.util.List;

public interface OrderJDBCTemplate {
    OrderPagination getAllOrders(Status status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size);

    List<CompareResponses> comparing(GadgetType gadgetType, boolean isDifferences);
}
