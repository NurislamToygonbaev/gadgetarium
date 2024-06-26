package gadgetarium.apies;

import gadgetarium.dto.response.AllFavoritesResponse;
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
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FavoritesApi {

    private final UserService userService;

    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Метод для добовления гаджетов в избранное или удалении", description = "Авторизация: ПОЛЬЗОВАТЕЛЬ")
    @PatchMapping("/{subGadgetId}")
    public HttpResponse addToFavorites(@PathVariable Long subGadgetId) {
        return userService.addToFavorites(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод добовляет несколько гаджетов в избранный")
    @PostMapping("/add-all")
    public HttpResponse addAllGadgetsToFavorites(@RequestParam List<Long> subGadgetIds){
        return userService.addAllGadgetsToFavorites(subGadgetIds);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод показывает гаджетов в избранный")
    @GetMapping("/favorites")
    public List<ListComparisonResponse> listFavorites() {
        return userService.seeFavorites();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод для просмотра всех гаджетов в избранный")
    @GetMapping
    public List<AllFavoritesResponse> getAllFavorites(){
        return userService.getAllFavorites();
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод для удаление одного гаджета в избранный")
    @DeleteMapping("/{subGadgetId}")
    public HttpResponse deleteById(@PathVariable Long subGadgetId){
        return userService.deleteById(subGadgetId);
    }

    @PreAuthorize("hasAuthority('USER')")
    @Operation(description = "Авторизация: ПОЛЬЗОВАТЕЛЬ", summary = "Метод удаляет всех гаджетов в избранный")
    @DeleteMapping("/clear")
    public HttpResponse clearFavorites(){
        return userService.clearFavorites();
    }
}
