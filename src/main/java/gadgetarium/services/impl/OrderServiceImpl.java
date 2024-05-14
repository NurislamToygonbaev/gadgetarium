package gadgetarium.services.impl;

import gadgetarium.dto.request.ChangePasswordRequest;
import gadgetarium.dto.request.CurrentUserProfileRequest;
import gadgetarium.dto.request.UserImageRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.Order;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.repositories.jdbcTemplate.OrderJDBCTemplate;
import gadgetarium.services.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderJDBCTemplate orderJDBCTemplate;
    private final CurrentUser currentUser;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OrderPagination getAllOrders(Status status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size) {
        return orderJDBCTemplate.getAllOrders(status, keyword, startDate, endDate, page, size);
    }

    @Override
    @Transactional
    public HttpResponse changeStatusOfOrder(Long orderId, Status status) {
        Order order = orderRepo.getOrderById(orderId);
        order.setStatus(status);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success changed")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse deleteOrder(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        orderRepo.delete(order);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success deleted")
                .build();
    }

    @Override
    public InfoResponse getInfo() {
        BigDecimal buyPrice = orderRepo.getBuyPrice();
        int buyCount = orderRepo.getBuyCount();
        BigDecimal orderPrice = orderRepo.getOrderPrice();
        int orderCount = orderRepo.getOrderCount();
        return InfoResponse.builder()
                .buyPrice(buyPrice)
                .buyCount(buyCount)
                .orderPrice(orderPrice)
                .orderCount(orderCount)
                .build();


    }

    @Override
    public InfoResponseFor getInfoForPeriod(ForPeriod forPeriod) {
        InfoResponseFor infoResponseFor = new InfoResponseFor();
        if (forPeriod.equals(ForPeriod.FOR_DAY)) {
            infoResponseFor.setCurrentPeriod(orderRepo.forCurrentDay());
            infoResponseFor.setPreviousPeriod(orderRepo.forPreviousDay());
        } else if (forPeriod.equals(ForPeriod.FOR_MONTH)) {
            infoResponseFor.setCurrentPeriod(orderRepo.forCurrentMonth());
            infoResponseFor.setPreviousPeriod(orderRepo.forPreviousMonth());
        } else if (forPeriod.equals(ForPeriod.FOR_YEAR)) {
            infoResponseFor.setCurrentPeriod(orderRepo.forCurrentYear());
            infoResponseFor.setPreviousPeriod(orderRepo.forPreviousYear());
        }
        return infoResponseFor;
    }

    @Override
    public OrderResponseFindById findOrderById(Long orderId) {
        orderRepo.getOrderById(orderId);
        return orderJDBCTemplate.findOrderById(orderId);
    }

    @Override
    public OrderInfoResponse findOrderInfo(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        return OrderInfoResponse.builder()
                .id(order.getId())
                .number(order.getNumber())
                .status(order.getStatus().name())
                .phoneNumber(order.getUser().getPhoneNumber())
                .address(order.getUser().getAddress())
                .build();
    }

    @Override
    public List<AllOrderHistoryResponse> getAllOrdersHistory() {
        return orderRepo.getAllHistory(currentUser.get().getId());
    }

    @Override
    public OrderHistoryResponse getOrderHistoryById(Long orderId) throws NotFoundException {
        Optional<Order> optionalOrder = currentUser.get().getOrders().stream()
                .filter(order -> Objects.equals(order.getId(), orderId))
                .findFirst();

        Order foundOrder = optionalOrder.orElseThrow(() -> new NotFoundException("Order not found"));

        User user = foundOrder.getUser();
        return OrderHistoryResponse.builder()
                .number(foundOrder.getNumber())
                .privateGadgetResponse( mapGadgets(foundOrder.getGadgets()))
                .status(foundOrder.getStatus())
                .clientFullName(user.getFirstName() + " " + user.getLastName())
                .userName(user.getFirstName())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .discount(foundOrder.getDiscountPrice())
                .currentPrice(foundOrder.getTotalPrice())
                .createdAt(foundOrder.getCreatedAt())
                .payment(foundOrder.getPayment())
                .lastName(user.getLastName())
                .build();
    }

    @Override
    @Transactional
    public CurrentUserProfileResponse editProfile(CurrentUserProfileRequest currentUserProfileRequest) {
        User user = currentUser.get();
        user.setFirstName(currentUserProfileRequest.userName());
        user.setLastName(currentUserProfileRequest.lastName());
        user.setEmail(currentUserProfileRequest.email());
        user.setPhoneNumber(currentUserProfileRequest.phoneNumber());
        user.setAddress(currentUserProfileRequest.address());
        return CurrentUserProfileResponse.builder()
                .userName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .build();
    }

    @Override
    @Transactional
    public UserImageResponse addPhotoAndEdit(UserImageRequest userImageRequest) {
        User user = currentUser.get();
        user.setImage(userImageRequest.image());
        return UserImageResponse.builder().image(user.getImage()).build();
    }

    @Override
    @Transactional
    public HttpResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = currentUser.get();
        String oldPassword = user.getPassword();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), oldPassword)) {
            return HttpResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .message("Incorrect old password")
                    .build();
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmationPassword())) {
            return HttpResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("New password and confirmation password do not match")
                    .build();
        }

        String newPasswordEncoded = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        user.setPassword(newPasswordEncoded);

        log.info("Password successfully changed for user: {}", user.getUsername());

        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Password successfully changed")
                .build();
    }

    private List<PrivateGadgetResponse> mapGadgets(List<Gadget> gadgets) {
        List<PrivateGadgetResponse> gadgetResponses = new ArrayList<>();

        for (Gadget gadget : gadgets) {
            SubGadget subGadget = gadget.getSubGadget();
            List<String> images = subGadget != null && !subGadget.getImages().isEmpty() ?
                    Collections.singletonList(subGadget.getImages().getFirst()) :
                    Collections.emptyList();
            PrivateGadgetResponse privateGadgetResponse = PrivateGadgetResponse.builder()
                    .id(gadget.getId())
                    .gadgetImage(images.isEmpty() ? null : Collections.singletonList(images.getFirst()))
                    .nameOfGadget(subGadget != null ? subGadget.getNameOfGadget() : null)
                    .subCategoryName(gadget.getSubCategory().getSubCategoryName())
                    .rating(subGadget != null ? subGadget.getRating() : null)
                    .countRating(0)
                    .currentPrice(subGadget != null ? subGadget.getCurrentPrice() : null)
                    .build();
            gadgetResponses.add(privateGadgetResponse);

        }
        return gadgetResponses;
    }
}
