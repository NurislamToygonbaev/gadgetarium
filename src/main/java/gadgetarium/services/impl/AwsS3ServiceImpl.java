package gadgetarium.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import gadgetarium.configs.s3.AmazonS3Config;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.S3Response;
import gadgetarium.services.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3ServiceImpl implements AwsS3Service {

    private final AmazonS3 s3Client;
    private final AmazonS3Config amazonS3Config;


    public String getUrl(String key) {
        return s3Client.getUrl(amazonS3Config.getBucketName(), key).toString();
    }

    @Override
    public S3Response uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return S3Response.builder()
                    .httpResponse(HttpResponse.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("No file provided")
                            .build())
                    .build();
        }
        String key = UUID.randomUUID().toString();
        File fileObj = null;

        try {
            fileObj = convertMultiPartFileToFile(file);
            s3Client.putObject(new PutObjectRequest(amazonS3Config.getBucketName(), key, fileObj));
            String url = getUrl(key);
            log.info("saved successfully with URL: {}", url);
            return S3Response.builder()
                    .data(url)
                    .httpResponse(HttpResponse.builder()
                            .status(HttpStatus.OK)
                            .message("saved successfully")
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while saving", e);
            return S3Response.builder()
                    .httpResponse(HttpResponse.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error occurred while saving")
                            .build())
                    .build();
        } finally {
            if (fileObj != null && fileObj.exists()) {
                fileObj.delete();
            }
        }
    }

    @Override
    public S3Response uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return S3Response.builder()
                    .httpResponse(HttpResponse.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("No file provided")
                            .build())
                    .build();
        }
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                return S3Response.builder()
                        .httpResponse(HttpResponse.builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .message("One or more files are empty")
                                .build())
                        .build();
            }

            String key = UUID.randomUUID().toString();
            File fileObj = null;

            try {
                fileObj = convertMultiPartFileToFile(file);
                s3Client.putObject(new PutObjectRequest(amazonS3Config.getBucketName(), key, fileObj));
                String url = getUrl(key);
                log.info("File saved successfully with URL: {}", url);
                urls.add(url);
            } catch (Exception e) {
                log.error("Error occurred while saving file", e);
                return S3Response.builder()
                        .httpResponse(HttpResponse.builder()
                                .status(HttpStatus.BAD_REQUEST)
                                .message("Error occurred while saving")
                                .build())
                        .build();
            } finally {
                if (fileObj != null && fileObj.exists()) {
                    fileObj.delete();
                }
            }
        }

        return S3Response.builder()
                .data(urls)
                .httpResponse(HttpResponse.builder()
                        .status(HttpStatus.OK)
                        .message("saved successfully")
                        .build())
                .build();
    }

    @Override
    public byte[] downloadFile(String key) {
        log.info("Downloading file with key: {}", key);
        S3Object s3Object = null;
        S3ObjectInputStream inputStream = null;

        try {
            s3Object = s3Client.getObject(amazonS3Config.getBucketName(), key);
            inputStream = s3Object.getObjectContent();
            byte[] content = IOUtils.toByteArray(inputStream);
            log.info("File downloaded successfully with key: {}", key);
            return content;
        } catch (IOException e) {
            log.error("Error occurred while downloading file with key: {}", key, e);
            return new byte[0];
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing input stream for file with key: {}", key, e);
                }
            }
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing S3Object for file with key: {}", key, e);
                }
            }
        }
    }

    @Override
    public HttpResponse deleteFile(String fileName) {
        log.info("Deleting file: {}", fileName);

        try {
            s3Client.deleteObject(amazonS3Config.getBucketName(), fileName);
            log.info("File {} deleted successfully", fileName);
            return HttpResponse.builder()
                    .status(HttpStatus.OK)
                    .message(fileName + " успешно удалено из Amazon S3")
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while deleting file: {}", fileName, e);
            return HttpResponse.builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error occurred while deleting file: " + fileName)
                    .build();
        }
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
