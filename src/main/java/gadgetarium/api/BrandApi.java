package gadgetarium.api;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.services.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 100000L)
public class BrandApi {

    private final BrandService brandService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Сохранение бренда", description = "Авторизация: АДМИНСТРАТОР")
    @PostMapping("/save")
    public HttpResponse saveBrand(@RequestParam(value = "file") MultipartFile file,
                                  @RequestParam String brandName){
        return brandService.saveBrand(file, brandName);
    }

}
