package gadgetarium.api;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.OrderPagination;
import gadgetarium.enums.Status;
import gadgetarium.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApi {

    private final OrderService orderService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "get all orders", description = "авторизация: АДМИН")
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
    @Operation(summary = "change status of order", description = "авторизация: АДМИН")
    @PatchMapping("/change-status/{orderId}")
    public HttpResponse changeStatus(@PathVariable Long orderId,
                                     @RequestParam Status status){
        return orderService.changeStatusOfOrder(orderId, status);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "delete order by ID", description = "авторизация: АДМИН")
    @DeleteMapping("/delete-order/{orderId}")
    public HttpResponse deleteOrder(@PathVariable Long orderId){
        return orderService.deleteOrder(orderId);
    }

}
