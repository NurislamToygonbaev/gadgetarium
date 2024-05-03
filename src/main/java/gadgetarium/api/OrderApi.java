package gadgetarium.api;

import gadgetarium.dto.response.*;
import gadgetarium.enums.ForPeriod;
import gadgetarium.enums.Status;
import gadgetarium.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApi {

    private final OrderService orderService;

    @GetMapping("/info")
    @Operation(description = "Инфографика")
    @PreAuthorize("hasAuthority('ADMIN')")
    public InfoResponse getInfo(){
        return orderService.getInfo();
    }

    @GetMapping("/info-withRequest")
    @Operation(description = "Инфографика за (день или месяц или год)")
    @PreAuthorize("hasAuthority('ADMIN')")
    public InfoResponseFor getInfoForPeriod(@RequestParam ForPeriod forPeriod){
        return orderService.getInfoForPeriod(forPeriod);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "все гаджеты", description = "авторизация: АДМИН")
    @GetMapping("/get-all")
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
    @Operation(summary = "Изменение статуса гаджета", description = "авторизация: АДМИН")
    @PatchMapping("/change-status/{orderId}")
    public HttpResponse changeStatus(@PathVariable Long orderId,
                                     @RequestParam Status status){
        return orderService.changeStatusOfOrder(orderId, status);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "удаление заказа по ID", description = "авторизация: АДМИН")
    @DeleteMapping("/delete-order/{orderId}")
    public HttpResponse deleteOrder(@PathVariable Long orderId){
        return orderService.deleteOrder(orderId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "найти заказа по ID", description = "авторизация: АДМИН")
    @GetMapping("/find-by-id/{orderId}")
    public OrderResponseFindById findOrderById(@PathVariable Long orderId){
        return orderService.findOrderById(orderId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "инфо заказа по ID", description = "авторизация: АДМИН")
    @GetMapping("/order_info/{orderId}")
    public OrderInfoResponse findOrderInfo(@PathVariable Long orderId){
        return orderService.findOrderInfo(orderId);
    }
}
