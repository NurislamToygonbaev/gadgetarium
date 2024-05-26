package gadgetarium.api;

import gadgetarium.dto.request.CategoryNameRequest;
import gadgetarium.dto.response.ComparedGadgetsResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ListComparisonResponse;
import gadgetarium.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comparison")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ComparisonApi {

    private final UserService userService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод для добовления гаджетов в сравнении или удалении")
    @PatchMapping("/{subGadgetId}")
    public HttpResponse addCompare(@PathVariable Long subGadgetId) {
        return userService.addCompare(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод показывает гаджетов в сравнении")
    @GetMapping
    public List<ListComparisonResponse> listCompare() {
        return userService.seeComparison();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод для просмотра различий")
    @GetMapping("/compare")
    public ComparedGadgetsResponse compare(CategoryNameRequest categoryName,
                                           @RequestParam boolean isDifferences) {
        return userService.compare(categoryName, isDifferences);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод для удаление одного гаджета в сравнении")
    @DeleteMapping("/{subGadgetId}")
    public HttpResponse deleteSub(@PathVariable Long subGadgetId) {
        return userService.deleteSubGadget(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод для удаление всех гаджетов в сравнении")
    @DeleteMapping("/clear")
    public HttpResponse deleteAllGadgets() {
        return userService.deleteAllGadgets();
    }


}
