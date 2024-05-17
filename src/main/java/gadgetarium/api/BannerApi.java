package gadgetarium.api;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;
import gadgetarium.services.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
