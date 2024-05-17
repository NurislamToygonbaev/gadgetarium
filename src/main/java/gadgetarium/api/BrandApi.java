package gadgetarium.api;

import gadgetarium.dto.response.BrandResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.services.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandApi {

    private final BrandService brandService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Сохранение бренда", description = "Авторизация: АДМИНСТРАТОР")
    @PostMapping
    public HttpResponse saveBrand(@RequestParam(value = "file") MultipartFile file,
                                  @RequestParam String brandName) {
        return brandService.saveBrand(file, brandName);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "все бренды", description = "авторизация: АДМИН")
    @GetMapping
    public List<BrandResponse> getAllBrands() {
        return brandService.getAllBrands();
    }

}
