package gadgetarium.api;

import gadgetarium.dto.response.AllFavoritesResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ListComparisonResponse;
import gadgetarium.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoritesAPI {

    private final UserService userService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Метод для добовления гаджетов в избранное или удалении", description = "Авторизация: User")
    @PostMapping("/add-favorites/{subGadgetId}")
    public HttpResponse addToFavorites(@PathVariable Long subGadgetId) {
        return userService.addToFavorites(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: User", summary = "Метод добовляет несколько гаджетов в избранный")
    @PostMapping("add-all-favorites")
    public HttpResponse addAllGadgetsToFavorites(@RequestParam List<Long> subGadgetId){
        return userService.addAllGadgetsToFavorites(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: User", summary = "Метод показывает гаджетов в избранный")
    @GetMapping("/list-favorites")
    public List<ListComparisonResponse> listFavorites() {
        return userService.seeFavorites();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: User", summary = "Метод для просмотра всех гаджетов в избранный")
    @GetMapping("get-all-favorites")
    public List<AllFavoritesResponse> getAllFavorites(){
        return userService.getAllFavorites();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: User", summary = "Метод для удаление одного гаджета в избранный")
    @DeleteMapping("/delete-by-id/{subGadgetId}")
    public HttpResponse deleteById(@PathVariable Long subGadgetId){
        return userService.deleteById(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: User", summary = "Метод удаляет всех гаджетов в избранный")
    @DeleteMapping("/clear-favorites")
    public HttpResponse clearFavorites(){
        return userService.clearFavorites();
    }
}
