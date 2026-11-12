package vn.tandung.jobhunter.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.tandung.jobhunter.util.constant.VideoStatusEnum;

import java.time.Instant;

@Entity
@Table(name = "video_media")
@Getter
@Setter
public class VideoMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Original S3 Key không được để trống")
    private String originalS3Key; // Đường dẫn file gốc (vd: companies/raw/video.mp4)

    private String hlsPlayUrl;    // Đường dẫn file phát HLS (vd: companies/hls/video/index.m3u8)

    @Enumerated(EnumType.STRING)
    private VideoStatusEnum status; // PENDING, PROCESSING, COMPLETED, FAILED

    private String failureReason; // Lưu log lỗi nếu băm video thất bại

    private Long referenceId;      // ID của thực thể sở hữu (id của User, Company, Job...)
    private String referenceType;  // Loại thực thể sở hữu ("USER", "COMPANY", "JOB")

    // Trong file VideoMedia.java
    private Integer progress; // Đổi từ int sang Integer

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
        this.status = VideoStatusEnum.PENDING;
        this.progress = 0;
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
