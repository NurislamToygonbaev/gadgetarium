package gadgetarium.services.impl;

import gadgetarium.dto.request.PersonalDataRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.Order;
import gadgetarium.entities.User;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Status;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.OrderRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.repositories.jdbcTemplate.OrderJDBCTemplate;
import gadgetarium.services.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderJDBCTemplate orderJDBCTemplate;
    private final GadgetRepository gadgetRepository;
    private final CurrentUser currentUser;
    private final UserRepository userRepo;

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
    @Transactional
    public HttpResponse placingAnOrder(List<Long> gadgetIds, boolean orderType, PersonalDataRequest personalDataRequest, BigDecimal orderSumma, BigDecimal discountSumma) {
        User currentUserr = currentUser.get();
        List<Gadget> gadgets = new ArrayList<>();
        Order order = new Order();
        long orderNumber = ThreadLocalRandom.current().nextLong(100000, 1000000);

        for (Long gadgetId : gadgetIds) {
            Gadget gadgetById = gadgetRepository.getGadgetById(gadgetId);
            gadgets.add(gadgetById);
        }

        currentUserr.setFirstName(personalDataRequest.firstName());
        currentUserr.setLastName(personalDataRequest.lastName());
        currentUserr.setEmail(personalDataRequest.email());
        currentUserr.setPhoneNumber(personalDataRequest.phoneNumber());

        if (orderType){
            order.setTypeOrder(true);
            order.setTotalPrice(orderSumma);
        }else {
            order.setTypeOrder(false);
            currentUserr.setAddress(personalDataRequest.deliveryAddress());

            if (BigDecimal.valueOf(10000).compareTo(orderSumma) >= 0){
                order.setTotalPrice(orderSumma.add(BigDecimal.valueOf(200)));
            }else {
                order.setTotalPrice(orderSumma);
            }
        }

        order.setStatus(Status.PENDING);
        order.setDiscountPrice(discountSumma);
        order.setNumber(orderNumber);
        order.setGadgets(gadgets);
        order.setUser(currentUserr);
        currentUserr.getOrders().add(order);
        orderRepo.save(order);

        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Order saved!")
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
                currentUserr.getPhoneNumber()
        );
    }
}
