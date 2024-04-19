package gadgetarium.api;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.services.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandApi {

    private final BrandService brandService;

    @PostMapping("/save")
    public HttpResponse saveBrand(@RequestParam(value = "file") MultipartFile file,
                                  @RequestParam String brandName){
        return brandService.saveBrand(file, brandName);
    }
}
