package gadgetarium.api;

import gadgetarium.dto.response.AllFavoritesResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ListComparisonResponse;
import gadgetarium.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoritesAPI {

    private final UserService userService;

    @Operation(summary = "Метод для добовления гаджетов в избранное или удалении", description = "Авторизация: ВСЕ")
    @PostMapping("/add-favorites/{subGadgetId}")
    public HttpResponse addToFavorites(@PathVariable Long subGadgetId) {
        return userService.addToFavorites(subGadgetId);
    }

    @Operation(description = "Авторизация: ВСЕ", summary = "Метод добовляет несколько гаджетов в избранный")
    @PostMapping("add-all-favorites")
    public HttpResponse addAllGadgetsToFavorites(@RequestParam @Valid @Min(1) List<Long> subGadgetId){
        return userService.addAllGadgetsToFavorites(subGadgetId);
    }

    @Operation(description = "Авторизация: ВСЕ", summary = "Метод показывает гаджетов в избранный")
    @GetMapping("/list-favorites")
    public List<ListComparisonResponse> listFavorites() {
        return userService.seeFavorites();
    }

    @Operation(description = "Авторизация: ВСЕ", summary = "Метод для просмотра всех гаджетов в избранный")
    @GetMapping("get-all-favorites")
    public List<AllFavoritesResponse> getAllFavorites(){
        return userService.getAllFavorites();
    }
    @Operation(description = "Авторизация: ВСЕ", summary = "Метод для удаление одного гаджета в избранный")
    @DeleteMapping("/delete-by-id/{subGadgetId}")
    public HttpResponse deleteById(@PathVariable Long subGadgetId){
        return userService.deleteById(subGadgetId);
    }

    @Operation(description = "Авторизация: ВСЕ", summary = "Метод показывает гаджетов в избранный")
    @DeleteMapping("/clear-favorites")
    public HttpResponse clearFavorites(){
        return userService.clearFavorites();
    }
}
