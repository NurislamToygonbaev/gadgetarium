package gadgetarium.api;

import gadgetarium.dto.request.PersonalDataRequest;
import gadgetarium.dto.response.*;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Status;
import gadgetarium.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderApi {

    private final OrderService orderService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Инфографика", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping("/info")
    public InfoResponse getInfo(){
        return orderService.getInfo();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Инфографика за (день или месяц или год)", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping("/info-amount")
    public InfoResponseFor getInfoForPeriod(@RequestParam ForPeriod forPeriod){
        return orderService.getInfoForPeriod(forPeriod);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Все гаджеты", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping
    public OrderPagination getAllOrders(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Status status,
                        @RequestParam(value = "startDate", required = false) LocalDate startDate,
                        @RequestParam(value = "endDate", required = false) LocalDate endDate,
                        @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "size", defaultValue = "5") int size) {
        return orderService.getAllOrders(status,keyword, startDate, endDate, page, size);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Изменение статуса гаджета", description = "Авторизация: АДМИНСТРАТОР")
    @PatchMapping("/{orderId}")
    public HttpResponse changeStatus(@PathVariable Long orderId,
                                     @RequestParam Status status){
        return orderService.changeStatusOfOrder(orderId, status);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Удаление заказа по ID", description = "Авторизация: АДМИНСТРАТОР")
    @DeleteMapping("/{orderId}")
    public HttpResponse deleteOrder(@PathVariable Long orderId){
        return orderService.deleteOrder(orderId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "найти заказа по ID", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping("/by-id/{orderId}")
    public OrderResponseFindById findOrderById(@PathVariable Long orderId){
        return orderService.findOrderById(orderId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "инфо заказа по ID", description = "Авторизация: АДМИНСТРАТОР")
    @GetMapping("/order-info/{orderId}")
    public OrderInfoResponse findOrderInfo(@PathVariable Long orderId){
        return orderService.findOrderInfo(orderId);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Оформление заказа.", description = "Авторизация: ПОЛЬЗВАТЕЛЬ")
    @PostMapping
    public HttpResponse placingAnOrder(@RequestParam List<Long> subGadgetId,
                                       @RequestParam boolean deliveryType,
                                       @RequestBody @Valid PersonalDataRequest personalDataRequest){
        return orderService.placingAnOrder(subGadgetId, deliveryType, personalDataRequest);
    }

    @PreAuthorize("hasAnyAuthority('USER')")
    @Operation(summary = "Возвращение дынные.", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @GetMapping("/personal")
    public PersonalDataResponse personalData(){
        return orderService.personalDataCustomer();
    }
}
