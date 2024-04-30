package gadgetarium.api;

import gadgetarium.dto.request.CategoryNameRequest;
import gadgetarium.dto.response.ComparedGadgetsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ListComparisonResponse;
import gadgetarium.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comparison")
public class ComparisonAPI {

    private final UserService userService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Метод для добовления гаджетов в сравнении или удалении", description = "авторизация: Юзер")
    @PostMapping("/add-compare/{subGadgetId}")
    public HttpResponse addCompare(@PathVariable Long subGadgetId) {
        return userService.addCompare(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Метод показывает гаджетов в сравнении", description = "авторизация: Юзер")
    @GetMapping("/list-compare")
    public List<ListComparisonResponse> listCompare() {
        return userService.seeComparison();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Метод для просмотра различий", description = "авторизация: Юзер")
    @GetMapping("/compare")
    public ComparedGadgetsResponse compare(CategoryNameRequest categoryName,
                                           @RequestParam boolean isDifferences) {
        return userService.compare(categoryName, isDifferences);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Метод для удаление одного гаджета в сравнении", description = "авторизация: Юзер")
    @DeleteMapping("/delete/{subGadgetId}")
    public HttpResponse deleteSub(@PathVariable Long subGadgetId) {
        return userService.deleteSubGadget(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Метод для удаление всех гаджетов в сравнении", description = "авторизация: Юзер")
    @DeleteMapping("/clear")
    public HttpResponse deleteAllGadgets() {
        return userService.deleteAllGadgets();
    }

}
