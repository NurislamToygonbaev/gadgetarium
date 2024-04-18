package gadgetarium.api;

import gadgetarium.dto.request.BannerRequest;
import gadgetarium.dto.response.BannerResponse;
import gadgetarium.services.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
@Slf4j
public class BannerApi {

    private final BannerService bannerService;

    @Operation(description = "Загрузить баннер")
    @PostMapping("/create")
    public BannerResponse createBanner(@RequestBody @Valid BannerRequest bannerRequest){
        return bannerService.create(bannerRequest);
    }
}