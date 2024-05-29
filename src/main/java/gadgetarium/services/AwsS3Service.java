package gadgetarium.services;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.S3Response;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AwsS3Service {

    S3Response uploadFile(MultipartFile file);
    S3Response uploadFiles(List<MultipartFile> files);
    HttpResponse deleteFile(String fileName);
    byte[] downloadFile(String key);
}
