package gadgetarium.services;

import gadgetarium.dto.response.BrandResponse;
import gadgetarium.dto.response.HttpResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BrandService {
    HttpResponse saveBrand(MultipartFile file, String brandName);

    List<BrandResponse> getAllBrands();
}
