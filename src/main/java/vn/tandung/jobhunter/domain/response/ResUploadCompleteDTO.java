package vn.tandung.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResUploadCompleteDTO {
    private String fileKey;
    private Instant completedAt;
    private String status;       // "SUCCESS" hoặc "PROCESSING_HLS"
    private Long videoId;        // null nếu không phải video
    private String hlsPlayUrl;   // null nếu không phải video
}
