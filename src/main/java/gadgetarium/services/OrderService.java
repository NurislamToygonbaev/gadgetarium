package gadgetarium.services;

import gadgetarium.dto.request.ChangePasswordRequest;
import gadgetarium.dto.request.CurrentUserProfileRequest;
import gadgetarium.dto.request.UserImageRequest;
import gadgetarium.dto.response.*;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Status;

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

    List<AllOrderHistoryResponse> getAllOrdersHistory();

    OrderHistoryResponse getOrderHistoryById(Long orderId);

    CurrentUserProfileResponse editProfile(CurrentUserProfileRequest currentUserProfileRequest);

    UserImageResponse addPhotoAndEdit(UserImageRequest userImageRequest);

    HttpResponse changePassword(ChangePasswordRequest changePasswordRequest);
}
