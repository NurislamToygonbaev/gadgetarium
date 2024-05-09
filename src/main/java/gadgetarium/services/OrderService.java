package gadgetarium.services;

import gadgetarium.dto.request.PersonalDataRequest;
import gadgetarium.dto.response.*;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    OrderPagination getAllOrders(Status status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size);

    HttpResponse changeStatusOfOrder(Long orderId, Status status);

    HttpResponse deleteOrder(Long orderId);

    InfoResponse getInfo();

    InfoResponseFor getInfoForPeriod(ForPeriod forPeriod);

    OrderResponseFindById findOrderById(Long orderId);

    OrderInfoResponse findOrderInfo(Long orderId);

    HttpResponse placingAnOrder(List<Long> gadgetIds, boolean deliveryType, PersonalDataRequest personalDataRequest, BigDecimal orderSumma, BigDecimal discountSumma);

    PersonalDataResponse personalDataCustomer();
}
