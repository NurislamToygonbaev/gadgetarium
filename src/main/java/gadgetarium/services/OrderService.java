package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.InfoResponse;
import gadgetarium.dto.response.InfoResponseFor;
import gadgetarium.dto.response.OrderPagination;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Status;

import java.time.LocalDate;

public interface OrderService {
    OrderPagination getAllOrders(Status status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size);

    HttpResponse changeStatusOfOrder(Long orderId, Status status);

    HttpResponse deleteOrder(Long orderId);

    InfoResponse getInfo();

    InfoResponseFor getInfoForPeriod(ForPeriod forPeriod);
}
