package gadgetarium.api;

import gadgetarium.dto.request.DiscountRequest;
import gadgetarium.dto.response.DiscountResponse;
import gadgetarium.services.DiscountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@RestController
@RequestMapping("/api/discount")
@RequiredArgsConstructor
@Slf4j
public class DiscountApi {

    private final DiscountService discountService;

    @Secured("ADMIN")
    @PostMapping("/create")
    public DiscountResponse create(@RequestParam @NotNull List<Long> subGadgetsId, @RequestBody @Valid DiscountRequest discountRequest){
        return discountService.create(subGadgetsId, discountRequest);
    }
}
