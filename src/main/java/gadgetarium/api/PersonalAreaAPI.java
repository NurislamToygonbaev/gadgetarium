package gadgetarium.api;

import gadgetarium.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/personal-area")
public class PersonalAreaAPI {

    private final OrderService orderService;
}
