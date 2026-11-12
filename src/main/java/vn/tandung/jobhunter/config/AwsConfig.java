package vn.tandung.jobhunter.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration 
public class AwsConfig {

    @Value ("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value ("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value ("${cloud.aws.region.static}")
    private String region;

    // 🌟 THÊM BIẾN NÀY: Nhận Endpoint động từ file properties (.env)
    @Value("${cloud.aws.s3.endpoint:}")
    private String endpoint;


    private software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
    getCredentials(){
        boolean HasAccessKey = accessKey != null && !accessKey.isBlank(); 
        boolean HasSecretKey = secretKey != null && !secretKey.isBlank(); 

        if(HasAccessKey && HasSecretKey){
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }

        return DefaultCredentialsProvider.create();
    }

    @Bean 
    public S3Client s3Client(){
        S3ClientBuilder builder = S3Client.builder()
                // Luôn dùng us-east-1 làm mã phân vùng ký số chuẩn cho S3-Compatible
                .region(Region.of("us-east-1")) 
                .credentialsProvider(getCredentials());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
            
            // Ép cấu hình Path-style truy cập dạng endpoint/bucket-name để chặn lỗi 404
            builder.serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .chunkedEncodingEnabled(false)
                    .build());
        }

        return builder.build();
    }

}
