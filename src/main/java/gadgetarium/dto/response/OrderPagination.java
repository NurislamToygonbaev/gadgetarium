package gadgetarium.dto.response;

import gadgetarium.enums.Status;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record OrderPagination(
        String searchWord,
        String status,
        int waiting,
        int progress,
        int onTheWay,
        int delivered,
        int canceled,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size,
        List<OrderResponse> orderResponses
) {
}
