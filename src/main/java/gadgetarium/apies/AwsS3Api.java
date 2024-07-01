package gadgetarium.apies;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.S3Response;
import gadgetarium.services.AwsS3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/s3")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AwsS3Api {

    private final AwsS3Service awsS3Service;

    @Operation(summary = "upload файла в s3", description = "Авторизация: Все")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public S3Response uploadFile(@RequestParam("file") MultipartFile file) {
        return awsS3Service.uploadFile(file);
    }

    @Operation(summary = "upload много файлов в s3", description = "Авторизация: Все")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public S3Response uploadFile(@RequestParam("files") List<MultipartFile> files) {
        return awsS3Service.uploadFiles(files);
    }


    @Operation(summary = "download файл из s3", description = "Авторизация: Все")
    @GetMapping
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam("key") String key) {
        byte[] data = awsS3Service.downloadFile(key);
        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(data.length);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @Operation(summary = "delete файл из s3", description = "Авторизация: Все")
    @DeleteMapping
    public HttpResponse deleteFile(@RequestParam("key") String key) {
        return awsS3Service.deleteFile(key);
    }

}
