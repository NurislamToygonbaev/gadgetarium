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
import gadgetarium.enums.Status;
import gadgetarium.exceptions.AlreadyExistsException;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(10000);
    private static final BigDecimal DELIVERY_CHARGE = BigDecimal.valueOf(200);

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
        Order order = orderRepo.getOrderById(orderId);
        BigDecimal price = order.getTotalPrice();
        int countOfGadget = orderRepo.countOfGadgets(orderId);

        List<Object[]> objects = orderRepo.getGadgetsFields(orderId);

        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(countOfGadget));

        BigDecimal totalDiscount = BigDecimal.ZERO;
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

            BigDecimal percent = BigDecimal.valueOf(percentValue);
            BigDecimal gadgetPrice = price.multiply(percent.divide(BigDecimal.valueOf(100)));
            BigDecimal gadgetDiscount = gadgetPrice.multiply(BigDecimal.valueOf(countOfGadget));
            totalDiscount = totalDiscount.add(gadgetDiscount);
        }
        BigDecimal discountPrice = totalPrice.subtract(totalDiscount);

        return OrderResponseFindById.builder()
                .id(order.getId())
                .fullName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .number(order.getNumber())
                .nameOfGadget(gadgetNameList)
                .memory(memoryList)
                .colour(colourList)
                .count(countOfGadget)
                .price(price)
                .percent(percentList)
                .discountPrice(discountPrice)
                .totalPrice(totalPrice)
                .build();
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

        BigDecimal totalSum = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        List<SubGadget> subGadgets = new ArrayList<>();
        for (Long gadgetId : subGadgetId) {
            SubGadget subGadget = subGadgetRepo.getByID(gadgetId);
            BigDecimal price = subGadget.getPrice();
            BigDecimal discount = BigDecimal.ZERO;

            if (subGadget.getGadget().getDiscount() != null) {
                int percent = subGadget.getGadget().getDiscount().getPercent();
                discount = price.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            totalSum = totalSum.add(price);
            totalDiscount = totalDiscount.add(discount);
            subGadgets.add(subGadget);
        }

        BigDecimal totalPriceWithDiscount = totalSum.subtract(totalDiscount);
        user.setFirstName(personalDataRequest.firstName());
        user.setLastName(personalDataRequest.lastName());
        user.setEmail(userEmail);
        user.setPhoneNumber(personalDataRequest.phoneNumber());

        if (orderType) {
            order.setTypeOrder(true);
            order.setTotalPrice(totalPriceWithDiscount);
        } else {
            order.setTypeOrder(false);
            user.setAddress(personalDataRequest.deliveryAddress());

            if (FREE_DELIVERY_THRESHOLD.compareTo(totalPriceWithDiscount) >= 0) {
                totalPriceWithDiscount = totalPriceWithDiscount.add(DELIVERY_CHARGE);
            }
            order.setTotalPrice(totalPriceWithDiscount);
        }

        order.setDiscountPrice(totalDiscount);
        order.setNumber(orderNumber);

        order.setUser(user);
        user.addOrder(order);
        order.getSubGadgets().addAll(subGadgets);

        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Order saved with ID: " + order.getId())
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
                .privateGadgetResponse(mapGadgets(foundOrder.getSubGadgets()))
                .status(foundOrder.getStatus())
                .clientFullName(user.getFirstName() + " " + user.getLastName())
                .userName(user.getFirstName())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .discount(foundOrder.getDiscountPrice())
                .currentPrice(foundOrder.getTotalPrice())
                .createdAt(String.valueOf(foundOrder.getCreatedAt()))
                .payment(foundOrder.getPayment())
                .lastName(user.getLastName())
                .build();
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

    private List<PrivateGadgetResponse> mapGadgets(List<SubGadget> gadgets) {
        List<PrivateGadgetResponse> gadgetResponses = new ArrayList<>();

        for (SubGadget subGadget : gadgets) {
            if (subGadget == null) continue;
            List<String> images = !subGadget.getImages().isEmpty() ?
                    Collections.singletonList(subGadget.getImages().getFirst()) :
                    Collections.emptyList();


            PrivateGadgetResponse privateGadgetResponse = PrivateGadgetResponse.builder()
                    .id(subGadget.getId())
                    .gadgetImage(images.isEmpty() ? null : Collections.singletonList(images.getFirst()))
                    .nameOfGadget(subGadget.getGadget().getNameOfGadget())
                    .subCategoryName(subGadget.getGadget().getSubCategory().getSubCategoryName())
                    .rating(subGadget.getGadget().getRating())
                    .countRating(0)
                    .currentPrice(subGadget.getPrice())
                    .build();
            gadgetResponses.add(privateGadgetResponse);

        }
        return gadgetResponses;
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
}
