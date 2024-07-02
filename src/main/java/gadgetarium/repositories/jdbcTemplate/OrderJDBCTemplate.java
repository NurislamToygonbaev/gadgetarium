package gadgetarium.repositories.jdbcTemplate;

import gadgetarium.dto.response.AllOrderHistoryResponse;
import gadgetarium.dto.response.CompareResponses;
import gadgetarium.dto.response.OrderHistoryResponse;
import gadgetarium.dto.response.OrderPagination;
import gadgetarium.enums.GadgetType;

import java.time.LocalDate;
import java.util.List;

public interface OrderJDBCTemplate {
    OrderPagination getAllOrders(String status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size);

    List<CompareResponses> comparing(GadgetType gadgetType, boolean isDifferences);

    List<AllOrderHistoryResponse> getAllHistory();

    OrderHistoryResponse getOrderHistoryById(Long orderId);
}
