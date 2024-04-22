package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BrandService {
    HttpResponse saveBrand(MultipartFile file, String brandName);
}
