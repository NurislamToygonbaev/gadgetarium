package gadgetarium.api;

import gadgetarium.dto.response.AllOrderHistoryResponse;
import gadgetarium.dto.response.OrderHistoryResponse;
import gadgetarium.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-area")
public class PersonalAreaAPI {

    private final OrderService orderService;

    @GetMapping("/view-all-history")
    public List<AllOrderHistoryResponse> getAllOrdersHistory(){
        return orderService.getAllOrdersHistory();
    }

    @GetMapping("/view-order/{orderId}")
    public OrderHistoryResponse getOrderHistoryById(@PathVariable Long orderId){
        return orderService.getOrderHistoryById(orderId);
    }
}
