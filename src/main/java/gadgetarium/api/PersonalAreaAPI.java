package gadgetarium.api;

import gadgetarium.dto.response.AllOrderHistoryResponse;
import gadgetarium.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-area")
public class PersonalAreaAPI {

    private final OrderService orderService;

    @GetMapping("/view-all-history")
    public AllOrderHistoryResponse getAllOrdersHistory(){
        return orderService.getAllOrdersHistory();
    }
}
