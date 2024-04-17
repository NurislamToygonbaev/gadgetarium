package gadgetarium.api;

import gadgetarium.dto.response.ComparedGadgetsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ListComparisonResponse;
import gadgetarium.services.UserService;
import gadgetarium.validation.email.StringValidation;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comparison")
public class ComparisonAPI {

    private final UserService userService;

    @Operation(description = "Метод для добовления гаджетов в сравнении")
    @PostMapping("/add-сompare/{subGadgetId}")
    public HttpResponse addCompare(@PathVariable Long subGadgetId) {
        return userService.addCompare(subGadgetId);
    }

    @Operation(description = "Метод показывает гаджетов в сравнении")
    @GetMapping("/list-сompare")
    public List<ListComparisonResponse> listCompare() {
        return userService.seeComparison();
    }
    @Operation(description = "Метод для просмотра различии определенного гаджета")
    @GetMapping("/compare")
    public ComparedGadgetsResponse compare(@RequestParam @Valid @StringValidation String categoryName,
                                           @RequestParam boolean differences) {
        return userService.compare(categoryName, differences);
    }

    @Operation(description = "Метод для удаление одного гаджета в сравнении")
    @DeleteMapping("/delete/{subGadgetId}")
    public HttpResponse deleteSub(@PathVariable Long subGadgetId) {
        return userService.deleteSubGadget(subGadgetId);
    }

    @Operation(description = "Метод для удаление всех гаджетов в сравнении")
    @DeleteMapping("/clear")
    public HttpResponse deleteAllGadgets() {
        return userService.deleteAllGadgets();
    }

}
