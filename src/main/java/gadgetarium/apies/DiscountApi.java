package gadgetarium.apies;

import gadgetarium.dto.request.DiscountRequest;
import gadgetarium.dto.response.DiscountResponse;
import gadgetarium.services.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/discount")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DiscountApi {

    private final DiscountService discountService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Создать скидку", description = "Авторизация: АДМИНСТРАТОР")
    @PostMapping
    public DiscountResponse create(@RequestBody @Valid DiscountRequest discountRequest){
        return discountService.create(discountRequest);
    }

}
