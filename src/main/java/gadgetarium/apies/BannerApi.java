package gadgetarium.apies;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;
import gadgetarium.dto.response.GetAllBannerResponse;
import gadgetarium.services.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class BannerApi {

    private final BannerService bannerService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Загрузить баннер", description = "Авторизация: АДМИНСТРАТОР")
    @PostMapping
    public BannerResponse createBanner(@RequestBody @Valid BannerRequest bannerRequest){
        return bannerService.create(bannerRequest);
    }

    @Operation(summary = "все баннеры", description = "Авторизация: ВСЕ")
    @GetMapping
    public List<GetAllBannerResponse> getAll(){
        return bannerService.getAll();
    }

    @Operation(summary = "<Баннер по ID", description = "Авторизация: ВСЕ")
    @GetMapping("/{bannerId}")
    public GetAllBannerResponse getById(@PathVariable Long bannerId){
        return bannerService.getById(bannerId);
    }
}
