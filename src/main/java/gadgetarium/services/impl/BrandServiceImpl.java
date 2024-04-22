package gadgetarium.services.impl;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.Brand;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.repositories.BrandRepository;
import gadgetarium.services.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepo;
    private final AmazonS3 amazonS3;
    @Value("${application.bucket.name}")
    private String bucketName;


    public String getUrl(String key){
        return amazonS3.getUrl(bucketName,key).toString();
    }

    private void checkBrandName(String name){
        boolean exists = brandRepo.existsByBrandName(name);
        if (exists){
            throw new AlreadyExistsException("Brand with name: "+name+" already exists");
        }
    }

    @Override
    public HttpResponse saveBrand(MultipartFile file, String brandName) {
        checkBrandName(brandName);
        String key = UUID.randomUUID().toString();
        File fileObj = convertMultiPartFileToFile(file);
        amazonS3.putObject(new PutObjectRequest(bucketName, key, fileObj));
        String url = getUrl(key);
        Brand brand = new Brand();
        brand.setBrandName(brandName);
        brand.setLogo(url);
        brandRepo.save(brand);
        log.info(getUrl(key));
        fileObj.delete();
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success saved")
                .build();
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return convertedFile;
    }
}
