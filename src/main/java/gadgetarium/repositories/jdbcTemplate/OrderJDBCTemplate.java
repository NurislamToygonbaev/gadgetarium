package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.response.OrderPagination;
import gadgetarium.dto.response.OrderResponseFindById;
import gadgetarium.enums.Status;

import java.time.LocalDate;

public interface OrderJDBCTemplate {
    OrderPagination getAllOrders(Status status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size);

    OrderResponseFindById findOrderById(Long orderId);
}
