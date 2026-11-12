package vn.tandung.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import vn.tandung.jobhunter.domain.VideoMedia;
import vn.tandung.jobhunter.domain.requert.CompleteUploadRequest;
import vn.tandung.jobhunter.domain.response.ResUploadCompleteDTO;
import vn.tandung.jobhunter.domain.response.ResVideoMediaDTO;
import vn.tandung.jobhunter.domain.response.file.ResUploadFileDTO;
import vn.tandung.jobhunter.service.FileService;
import vn.tandung.jobhunter.util.annotation.ApiMessage;
import vn.tandung.jobhunter.util.constant.S3FolderEnum;
import vn.tandung.jobhunter.util.constant.VideoStatusEnum;

import org.springframework.util.AntPathMatcher; // Thêm dòng này lên đầu file

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1")
public class FileController {

    private final FileService fileService;
    private static final long MAX_CHUNK_SIZE = 10 * 1024 * 1024; // 10MB chặn DoS

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    // Viết thêm API này vào FileController nếu muốn FE có thể lấy danh sách folder động
    @GetMapping("/files/folders")
    @ApiMessage("Get list of allowed S3 folders")
    public ResponseEntity<List<String>> getAllowedFolders() {
        List<String> folders = java.util.stream.Stream.of(S3FolderEnum.values())
                .map(S3FolderEnum::getPath)
                .toList();
        return ResponseEntity.ok(folders);
    }



    @PostMapping("/files/upload/init")
    @ApiMessage("Initialize multipart upload")
    public ResponseEntity<Map<String, String>> initUpload(
            @RequestParam("fileName") String fileName,
            @RequestParam("folder") String folder, // Ví dụ FE truyền: "public/avatars"
            @RequestParam("contentType") String contentType) {

        // 1. Kiểm tra định dạng file hợp lệ
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx", "mp4", "mkv");
        boolean isValidExt = allowedExtensions.stream().anyMatch(ext -> fileName.toLowerCase().endsWith(ext));
        if (!isValidExt) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file extension"));
        }

        // 2. BẢO MẬT: Kiểm tra xem tên folder gửi lên có nằm trong danh sách định nghĩa chuẩn không
        if (!S3FolderEnum.isValidFolder(folder)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Folder '" + folder + "' is not permitted in this system"));
        }

        // 3. Tiến hành tạo fileKey và gọi S3 như cũ
        String fileKey = folder + "/" + System.currentTimeMillis() + "-" + fileName;
        var s3Response = fileService.initiateMultipartUpload(fileKey, contentType);

        Map<String, String> response = new HashMap<>();
        response.put("uploadId", s3Response.uploadId());
        response.put("fileKey", fileKey);
        return ResponseEntity.ok(response);
    }


    // @PostMapping("/files/upload/part" )
    // @ApiMessage("Upload a single file chunk")
    // // THÊM ĐOẠN ANNOTATION ĐẶC TẢ SWAGGER NÀY:
    // @io.swagger.v3.oas.annotations.parameters.RequestBody(
    //     description = "Chọn file phân mảnh nhị phân thô để gửi qua InputStream (Dung lượng < 10MB)",
    //     required = true,
    //     content = @io.swagger.v3.oas.annotations.media.Content(
    //         mediaType = "application/octet-stream",
    //         schema = @io.swagger.v3.oas.annotations.media.Schema(
    //             type = "string", 
    //             format = "binary", 
    //             description = "Nội dung nhị phân thô của mảnh file video"
    //         )
    //     )
    // )
    // public ResponseEntity<Map<String, String>> uploadPart(
    //         @RequestHeader("Content-Length") long contentLength,
    //         @RequestHeader("X-Chunk-Index") int partNumber,
    //         @RequestHeader("X-Upload-Id") String uploadId,
    //         @RequestHeader("X-File-Key") String fileKey,
    //         InputStream chunkStream) {

        
    //     if (contentLength > MAX_CHUNK_SIZE) {
    //         return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
    //                 .body(Map.of("error", "Chunk size exceeds maximum limit of 10MB"));
    //     }

    //     try {
    //         CompletedPart part = fileService.uploadPart(fileKey, uploadId, partNumber, chunkStream, contentLength);
    //         return ResponseEntity.ok(Map.of("partNumber", String.valueOf(part.partNumber()), "eTag", part.eTag()));
    //     } catch (Exception e) {
    //         fileService.abortMultipartUpload(fileKey, uploadId);
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
    //     }
    // }


    @PostMapping(
        value = "/files/upload/part", 
        consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ApiMessage("Upload a single file chunk")
    public ResponseEntity<Map<String, String>> uploadPart(
            // Tạm thời comment @RequestHeader("Content-Length") vì Spring sẽ tự tính từ MultipartFile
            // @RequestHeader("Content-Length") long contentLength,
            @RequestHeader("X-Chunk-Index") int partNumber,
            @RequestHeader("X-Upload-Id") String uploadId,
            @RequestHeader("X-File-Key") String fileKey,
            //InputStream chunkStream
            // Đổi sang nhận file bằng MultipartFile để Swagger hiện nút chọn file
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        // Tự động lấy kích thước thực tế tính bằng byte của file mảnh từ MultipartFile
        long contentLength = file.getSize();

        if (contentLength > MAX_CHUNK_SIZE) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "Chunk size exceeds maximum limit of 10MB"));
        }

        try(InputStream chunkStream = file.getInputStream()) {
            CompletedPart part = fileService.uploadPart(fileKey, uploadId, partNumber, chunkStream, contentLength);
            return ResponseEntity.ok(Map.of("partNumber", String.valueOf(part.partNumber()), "eTag", part.eTag()));
        } catch (Exception e) {
            fileService.abortMultipartUpload(fileKey, uploadId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/files/upload/complete")
    @ApiMessage("Complete multipart upload")
    public ResponseEntity<ResUploadCompleteDTO> completeUpload(@RequestBody CompleteUploadRequest request) {
        List<CompletedPart> completedParts = new ArrayList<>();

        // SỬA TẠI ĐÂY: Dùng request.getParts() thay vì request.parts
        for (var p : request.getParts()) {
            completedParts.add(CompletedPart.builder()
                    .partNumber(p.getPartNumber()) // Đổi p.partNumber thành p.getPartNumber()
                    .eTag(p.getETag())             // Đổi p.eTag thành p.getETag()
                    .build());
        }

        // SỬA TẠI ĐÂY: Sử dụng các hàm get...() tương ứng
        fileService.completeMultipartUpload(request.getFileKey(), request.getUploadId(), completedParts);

        ResUploadCompleteDTO res = new ResUploadCompleteDTO();
        res.setFileKey(request.getFileKey());
        res.setCompletedAt(Instant.now());

        String fileKeyLower = request.getFileKey().toLowerCase();
        if (fileKeyLower.endsWith(".mp4") || fileKeyLower.endsWith(".mkv")) {
            String outputHlsFolder = request.getFileKey().replaceAll("\\.[^.]+$", ""); 
            String hlsPlayUrl = outputHlsFolder + "/index.m3u8";

            // Lưu thông tin ban đầu vào Database
            VideoMedia video = fileService.saveInitialVideoMedia(
                request.getFileKey(), 
                hlsPlayUrl, 
                request.getReferenceId(), 
                request.getReferenceType()
            );

            // Tiến hành băm ngầm video
            fileService.downloadConvertAndUploadHlsAsync(request.getFileKey(), outputHlsFolder, video.getId()); 

            res.setVideoId(video.getId());
            res.setHlsPlayUrl(hlsPlayUrl);
            res.setStatus("PROCESSING_HLS");
        } else {
            res.setStatus("SUCCESS");
        }

        return ResponseEntity.ok(res);
    }

    @PostMapping("/files/upload/abort")
    @ApiMessage("Abort multipart upload and clean up cache data")
    public ResponseEntity<Map<String, String>> abortUpload(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("fileKey") String fileKey) {
        try {
            // Gọi service ra lệnh cho AWS S3 xóa sạch toàn bộ các mảnh dở dang
            this.fileService.abortMultipartUpload(fileKey, uploadId);

            return ResponseEntity.ok(Map.of("message", "Đã hủy tiến trình upload và dọn dẹp file dở dang thành công trên S3"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể hủy tiến trình: " + e.getMessage()));
        }
    }


    @GetMapping("/files/video-play-url")
    @ApiMessage("Get secure CloudFront play URL and detailed transcode progress")
    public ResponseEntity<?> getVideoPlayUrl(
            @RequestParam(value = "videoId", required = false) Long videoId,
            @RequestParam(value = "refType", required = false) String refType,
            @RequestParam(value = "refId", required = false) Long refId) {
        
        VideoMedia video = null;

        // Triệu gọi các hàm truy vấn sạch từ Service kết nối tới Repository
        if (videoId != null) {
            video = fileService.getVideoMediaById(videoId);
        } else if (refType != null && refId != null) {
            video = fileService.getVideoMediaByReference(refType, refId);
        }

        if (video == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy dữ liệu video phù hợp trong hệ thống"));
        }

        // Tạo đối tượng Response DTO chuẩn hóa trả về cho Frontend
        ResVideoMediaDTO res = new ResVideoMediaDTO();
        res.setId(video.getId());
        res.setStatus(video.getStatus());
        res.setProgress(video.getProgress());
        res.setFailureReason(video.getFailureReason());
        res.setCreatedAt(video.getCreatedAt());

        // Kiểm tra trạng thái cấp link chữ ký số CloudFront
        if (VideoStatusEnum.COMPLETED.equals(video.getStatus())) {
            String secureCdnUrl = fileService.generateCloudFrontSignedUrl(video.getHlsPlayUrl());
            res.setHlsPlayUrl(secureCdnUrl);
        } else {
            res.setHlsPlayUrl(null);
        }

        return ResponseEntity.ok(res);
    }



    @GetMapping("/files/download")
    @ApiMessage("Get secure presigned download link")
    public ResponseEntity<Map<String, String>> getSecureDownloadUrl(@RequestParam("fileKey") String fileKey) {
        String secureUrl = fileService.generatePresignedUrl(fileKey);
        return ResponseEntity.ok(Map.of("downloadUrl", secureUrl));
    }
}
