package gadgetarium.services.impl;

import gadgetarium.configs.jwt.JwtService;
import gadgetarium.dto.request.ChangePasswordRequest;
import gadgetarium.dto.request.CurrentUserProfileRequest;
import gadgetarium.dto.request.PersonalDataRequest;
import gadgetarium.dto.request.UserImageRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.Order;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Payment;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.UserRepository;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderJDBCTemplate orderJDBCTemplate;
    private final JwtService jwtService;
    private final SubGadgetRepository subGadgetRepo;
    private final CurrentUser currentUser;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    private Status status;

    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(10000);
    private static final BigDecimal DELIVERY_CHARGE = BigDecimal.valueOf(200);

    @Override
    public OrderPagination getAllOrders(String status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size) {
        return orderJDBCTemplate.getAllOrders(status, keyword, startDate, endDate, page, size);
    }

    @Override
    @Transactional
    public HttpResponse changeStatusOfOrder(Long orderId, String russianStatus) {
        Status status = Status.fromRussian(russianStatus);
        if (status == null) {
            throw new BadRequestException("Invalid status: " + russianStatus);
        }

        Order order = orderRepo.getOrderById(orderId);
        order.setStatus(status);
        orderRepo.save(order);

        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Status successfully changed")
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
        BigDecimal repoBuyPrice = orderRepo.getBuyPrice();
        int buyCount = orderRepo.getBuyCount();
        BigDecimal repoOrderPrice = orderRepo.getOrderPrice();
        int orderCount = orderRepo.getOrderCount();

        return InfoResponse.builder()
                .buyPrice(repoBuyPrice)
                .buyCount(buyCount)
                .orderPrice(repoOrderPrice)
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
        Optional<Order> orderOptional = orderRepo.getOrderWithStatus(orderId);
        Order order = orderOptional.orElseThrow(() ->
                new BadRequestException("order with ID: " + orderId + " not found"));

        BigDecimal price = BigDecimal.ZERO;
        int totalGadgets = 0;

        List<Object[]> objects = orderRepo.getGadgetsFields(orderId);

        List<String> gadgetNameList = new ArrayList<>();
        List<String> memoryList = new ArrayList<>();
        List<String> colourList = new ArrayList<>();
        List<Integer> percentList = new ArrayList<>();

        for (Object[] response : objects) {
            gadgetNameList.add((String) response[0]);
            memoryList.add((String) response[1]);
            colourList.add((String) response[2]);


            int percentValue = response[3] != null ? (Integer) response[3] : 0;
            percentList.add(percentValue);

            price = price.add((BigDecimal) response[4]);

            int countOfGadget = ((Long) response[5]).intValue();
            totalGadgets += countOfGadget;
        }

        return OrderResponseFindById.builder()
                .id(order.getId())
                .fullName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .number(order.getNumber())
                .nameOfGadget(gadgetNameList)
                .memory(memoryList)
                .colour(colourList)
                .count(totalGadgets)
                .price(price)
                .percent(percentList)
                .discountPrice(order.getDiscountPrice())
                .totalPrice(order.getTotalPrice())
                .build();
    }



    @Override
    public OrderInfoResponse findOrderInfo(Long orderId) {
        Order order = orderRepo.getOrderById(orderId);
        return OrderInfoResponse.builder()
                .id(order.getId())
                .number(order.getNumber())
                .status(order.getStatus())
                .phoneNumber(order.getUser().getPhoneNumber())
                .address(order.getUser().getAddress())
                .build();
    }

    @Override
    @Transactional
    public HttpResponse placingAnOrder(List<Long> subGadgetId, boolean orderType, PersonalDataRequest personalDataRequest) {
        if (subGadgetId == null || personalDataRequest == null) {
            throw new IllegalArgumentException("SubGadget ID list and personal data request must not be null");
        }

        User user = currentUser.get();
        String userEmail = personalDataRequest.email();

        boolean emailExists = userRepo.existsByEmail(userEmail);
        if (emailExists && !userEmail.equals(user.getEmail())) {
            throw new AlreadyExistsException("User with email: " + userEmail + " already exists.");
        }

        Order order = new Order();
        orderRepo.save(order);
        long orderNumber = ThreadLocalRandom.current().nextLong(100000, 1000000);

        user.setFirstName(personalDataRequest.firstName());
        user.setLastName(personalDataRequest.lastName());
        user.setEmail(userEmail);
        user.setPhoneNumber(personalDataRequest.phoneNumber());
        BigDecimal price = personalDataRequest.price();

        if (orderType) {
            order.setTypeOrder(true);
            order.setTotalPrice(personalDataRequest.price());
        } else {
            order.setTypeOrder(false);
            user.setAddress(personalDataRequest.deliveryAddress());

            if (FREE_DELIVERY_THRESHOLD.compareTo(price) >= 0) {
                price = price.add(DELIVERY_CHARGE);
            }
            order.setTotalPrice(price);
        }
        if (personalDataRequest.discountPrice() != null){
            order.setDiscountPrice(personalDataRequest.discountPrice());
        }
        order.setNumber(orderNumber);

        order.setUser(user);
        user.addOrder(order);

        for (Long aLong : subGadgetId) {
            SubGadget subGadget = subGadgetRepo.getByID(aLong);
            order.addSubGadget(subGadget);
        }

        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Order saved with ID: " + order.getId())
                .build();
    }


    @Override
    public List<AllOrderHistoryResponse> getAllOrdersHistory() {
        return orderJDBCTemplate.getAllHistory();
    }

    @Override
    public OrderHistoryResponse getOrderHistoryById(Long orderId) throws NotFoundException {
       return orderJDBCTemplate.getOrderHistoryById(orderId);
    }

    @Override
    public PersonalDataResponse personalDataCustomer() {
        User currentUserr = currentUser.get();

        return new PersonalDataResponse(
                currentUserr.getFirstName(),
                currentUserr.getLastName(),
                currentUserr.getEmail(),
                currentUserr.getPhoneNumber(),
                currentUserr.getAddress()
        );
    }


    @Transactional
    public CurrentUserProfileResponse editProfile(CurrentUserProfileRequest currentUserProfileRequest) {
        User user = currentUser.get();
        boolean email = userRepo.existsByEmail(currentUserProfileRequest.email());
        if (email && !currentUserProfileRequest.email().equals(currentUser.get().getEmail())) {
            throw new AlreadyExistsException("User with email: " + currentUserProfileRequest.email() + " already exists.");
        }

        user.setFirstName(currentUserProfileRequest.userName());
        user.setLastName(currentUserProfileRequest.lastName());
        user.setEmail(currentUserProfileRequest.email());
        user.setPhoneNumber(currentUserProfileRequest.phoneNumber());
        user.setAddress(currentUserProfileRequest.address());
        userRepo.save(user);
        return CurrentUserProfileResponse.builder()
                .userName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .id(user.getId())
                .token(jwtService.createToken(user))
                .httpResponse(HttpResponse.builder()
                        .status(HttpStatus.OK)
                        .message("successfully updated")
                        .build())
                .build();
    }

    @Override
    @Transactional
    public UserImageResponse addPhotoAndEdit(UserImageRequest userImageRequest) {
        User user = currentUser.get();
        user.setImage(userImageRequest.image());
        userRepo.save(user);
        return UserImageResponse.builder()
                .image(user.getImage())
                .build();
    }

    @Override
    @Transactional
    public HttpResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = currentUser.get();
        String oldPassword = user.getPassword();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), oldPassword)) {
            throw new BadRequestException("Incorrect old password");
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmationPassword())) {
            throw new BadRequestException("New password and confirmation password do not match");
        }

        String newPasswordEncoded = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        user.setPassword(newPasswordEncoded);
        userRepo.save(user);

        log.info("Password successfully changed for user: {}", user.getUsername());

        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Password successfully changed")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse clearOrders() {
        User user = currentUser.get();
        orderRepo.deleteAll(user.getOrders());
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("cleared")
                .build();
    }

    @Override
    public UserProfileResponse findUserProfile() {
        User user = currentUser.get();
        return UserProfileResponse.builder()
                .firsName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .image(user.getImage())
                .build();
    }

}
