package vn.tandung.jobhunter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import vn.tandung.jobhunter.domain.VideoMedia;
import vn.tandung.jobhunter.repository.VideoMediaRepository;
import vn.tandung.jobhunter.util.constant.VideoStatusEnum;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import java.nio.file.Paths;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class FileService {

    private final S3Client s3Client;
    private final VideoMediaRepository videoMediaRepository; // 1. Tiêm thêm Repository phục vụ lưu DB

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.cloudfront.domain}") 
    private String cloudflareDomain; 

    @Value("${cloud.aws.cloudflare.secret-key}")
    private String cloudflareSecretKey;

    @Value("${cloud.aws.cloudflare.hls-token-ttl-minutes:180}")
    private int hlsTokenTtlMinutes;


    // Cập nhật Constructor để Spring tự động Inject cả 2 BEAN
    public FileService(S3Client s3Client, VideoMediaRepository videoMediaRepository) {
        this.s3Client = s3Client;
        this.videoMediaRepository = videoMediaRepository;
    }


    // Tìm kiếm thông tin video theo ID bảng

    public VideoMedia getVideoMediaById(Long id) {
        return this.videoMediaRepository.findById(id).orElse(null);
    }

    // Tìm kiếm video liên kết theo loại thực thể (USER/COMPANY/JOB) và ID của thực thể đó
    public VideoMedia getVideoMediaByReference(String referenceType, Long referenceId) {
        return this.videoMediaRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId).orElse(null);
    }

    public CreateMultipartUploadResponse initiateMultipartUpload(String key, String contentType) {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        return s3Client.createMultipartUpload(request);
    }

    public CompletedPart uploadPart(String key, String uploadId, int partNumber, InputStream inputStream, long contentLength) throws IOException {
        UploadPartRequest request = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartResponse response = s3Client.uploadPart(request, RequestBody.fromInputStream(inputStream, contentLength));
        
        return CompletedPart.builder()
                .partNumber(partNumber)
                .eTag(response.eTag())
                .build();
    }

    public CompleteMultipartUploadResponse completeMultipartUpload(String key, String uploadId, List<CompletedPart> completedParts) {
        completedParts.sort(Comparator.comparing(CompletedPart::partNumber));

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build();

        return s3Client.completeMultipartUpload(request);
    }

    public void abortMultipartUpload(String key, String uploadId) {
        AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .build();
        s3Client.abortMultipartUpload(request);
    }


   
    // Link tải file: ký theo đúng file, hạn 15 phút
    public String generatePresignedUrl(String key) {
        return generateCloudflareSecureUrl(key, 15, false);
    }


        // Hàm mở kết nối mạng đọc file trực tiếp từ S3 về làm vòi dẫn
    public InputStream getS3ObjectStream(String key) {
        return this.s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
    }


    // 2. THÊM MỚI HÀM KHỞI TẠO BẢN GHI VIDEO TRONG DATABASE (ĐANG ĐỢI XỬ LÝ)
    public VideoMedia saveInitialVideoMedia(String originalS3Key, String hlsPlayUrl, Long referenceId, String referenceType) {
        VideoMedia video = new VideoMedia();
        video.setOriginalS3Key(originalS3Key);
        video.setHlsPlayUrl(hlsPlayUrl);
        video.setReferenceId(referenceId);
        video.setReferenceType(referenceType);
        video.setStatus(VideoStatusEnum.PENDING); // Bắt đầu ở trạng thái hàng đợi nhận lệnh băm
        return videoMediaRepository.save(video);
    }

    // 3. CẬP NHẬT HÀM XỬ LÝ CHẠY NGẦM BẤT ĐỒNG BỘ (Bổ sung tham số videoId và logic cập nhật DB)
    @Async("videoExecutor")
    public void downloadConvertAndUploadHlsAsync(String originalS3Key, String outputS3Folder, Long videoId) {
        
        // Cập nhật trạng thái video sang PROCESSING khi bắt đầu tiến trình luồng ngầm
        videoMediaRepository.findById(videoId).ifPresent(v -> {
            v.setStatus(VideoStatusEnum.PROCESSING);
            videoMediaRepository.save(v);
        });

        Path tempDir = null;
        ResponseInputStream<GetObjectResponse> s3InputStream = null;
        OutputStream ffmpegOutputStream = null;
        Process process = null;

        try {
            tempDir = Files.createTempDirectory("hls_async_");
            String outputM3u8Name = "index.m3u8";
            Path outputM3u8Path = tempDir.resolve(outputM3u8Name);

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", 
                    "-i", "pipe:0", 
                    "-c:v", "libx264",
                    "-profile:v", "main", "-level", "4.0",
                    "-crf", "26",
                    "-maxrate", "3000k",
                    "-bufsize", "6000k",
                    "-c:a", "aac", "-b:a", "128k",
                    "-force_key_frames", "expr:gte(t,n_forced*4)", 
                    "-start_number", "0", 
                    "-hls_time", "4", 
                    "-hls_flags", "split_by_time", 
                    "-hls_list_size", "0", 
                    "-f", "hls", 
                    outputM3u8Path.toString()
            );

            process = pb.start();

            s3InputStream = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucketName).key(originalS3Key).build()
            );
            ffmpegOutputStream = process.getOutputStream();

            byte[] buffer = new byte[8192]; 
            int bytesRead;
            while ((bytesRead = s3InputStream.read(buffer)) != -1) {
                ffmpegOutputStream.write(buffer, 0, bytesRead);
            }

            ffmpegOutputStream.close();

            boolean finished = process.waitFor(15, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("FFmpeg process timed out after 15 minutes");
            }

            File[] files = tempDir.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".m3u8") || f.getName().endsWith(".ts")) {
                        String s3Key = outputS3Folder + "/" + f.getName();
                        String contentType = f.getName().endsWith(".m3u8") ? "application/x-mpegURL" : "video/MP2T";
                        s3Client.putObject(
                                PutObjectRequest.builder().bucket(bucketName).key(s3Key).contentType(contentType).build(),
                                RequestBody.fromFile(f)
                        );
                    }
                }
            }

            // CHỐT HẠ THÀNH CÔNG: Cập nhật trạng thái video thành COMPLETED
            videoMediaRepository.findById(videoId).ifPresent(v -> {
                v.setStatus(VideoStatusEnum.COMPLETED);
                videoMediaRepository.save(v);
            });

        } catch (Exception e) {
            System.err.println("Lỗi xử lý băm video HLS ngầm: " + e.getMessage());
            if (process != null) process.destroyForcibly();
            e.printStackTrace();

            // CHỐT HẠ THẤT BẠI: Cập nhật trạng thái thành FAILED và lưu log lỗi hệ thống vào DB
            videoMediaRepository.findById(videoId).ifPresent(v -> {
                v.setStatus(VideoStatusEnum.FAILED);
                v.setFailureReason(e.getMessage() != null ? e.getMessage() : "Unknown FFmpeg Error");
                videoMediaRepository.save(v);
            });

        } finally {
            try {
                if (s3InputStream != null) s3InputStream.close();
                if (ffmpegOutputStream != null) ffmpegOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (tempDir != null) {
                try {
                    Files.walk(tempDir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                } catch (IOException e) {
                    System.err.println("Không thể xóa thư mục tạm: " + e.getMessage());
                }
            }
        }
    }

    // Link HLS: ký theo thư mục video, hạn dài hơn độ dài video
    public String generateCloudFrontSignedUrl(String hlsPlayUrl) {
        return generateCloudflareSecureUrl(hlsPlayUrl, hlsTokenTtlMinutes, true);
    }

    /**
     * HÀM BỔ SUNG: Tiện ích tạo URL bảo mật có Token kiểm tra thời hạn (Thuật toán mã hóa HMAC-SHA256)
     */
    private String generateCloudflareSecureUrl(String rawPath, int durationMinutes, boolean scopeToFolder) {
        try {
            String basePath = rawPath.startsWith("/") ? rawPath : "/" + rawPath;
            long expires = Instant.now().plus(Duration.ofMinutes(durationMinutes)).getEpochSecond();

            // HLS: ký theo thư mục chứa index.m3u8 (kết thúc bằng "/")
            String scope = scopeToFolder
                    ? basePath.substring(0, basePath.lastIndexOf('/') + 1)
                    : basePath;

            String stringToSign = scope + expires;

            // Tính HMAC-SHA256
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    cloudflareSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

            // Đổi sang chuỗi hex -> đây là biến "token"
            StringBuilder hexString = new StringBuilder();
            for (byte b : rawHmac) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String token = hexString.toString();

            return cloudflareDomain + basePath + "?token=" + token + "&expires=" + expires;

        } catch (Exception e) {
            System.err.println("Lỗi khi ký link bảo mật Cloudflare: " + e.getMessage());
            throw new RuntimeException("Không thể ký link bảo mật hệ thống", e);
        }
    }



}