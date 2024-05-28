package gadgetarium.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import gadgetarium.configs.s3.AmazonS3Config;
import gadgetarium.dto.response.BrandResponse;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.Brand;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.repositories.BrandRepository;
import gadgetarium.services.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepo;
    private final AmazonS3 amazonS3;
    private final AmazonS3Config amazonS3Config;

    public String getUrl(String key) {
        return amazonS3.getUrl(amazonS3Config.getBucketName(), key).toString();
    }

    private void checkBrandName(String name) {
        boolean exists = brandRepo.existsByBrandName(name);
        if (exists) {
            throw new AlreadyExistsException("Brand with name: " + name + " already exists");
        }
    }

    @Override
    public HttpResponse saveBrand(MultipartFile file, String brandName) {
        if (file == null || file.isEmpty()) {
            return HttpResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("No file provided")
                    .build();
        }
        checkBrandName(brandName);
        String key = UUID.randomUUID().toString();
        File fileObj = convertMultiPartFileToFile(file);
        if (fileObj == null) {
            return HttpResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error converting file")
                    .build();
        }

        try {
            amazonS3.putObject(new PutObjectRequest(amazonS3Config.getBucketName(), key, fileObj));
            String url = getUrl(key);
            Brand brand = new Brand();
            brand.setBrandName(brandName);
            brand.setLogo(url);
            brandRepo.save(brand);
            log.info("Brand saved successfully with URL: {}", url);
            return HttpResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Brand saved successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while saving brand", e);
            return HttpResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error occurred while saving brand")
                    .build();
        } finally {
            fileObj.delete();
        }
    }

    @Override
    public List<BrandResponse> getAllBrands() {
        return brandRepo.getAllBrands();
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
            return null;
        }
        return convertedFile;
    }
}
