package vn.tandung.jobhunter.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.tandung.jobhunter.util.constant.VideoStatusEnum;
import java.time.Instant;

@Getter 
@Setter 
@AllArgsConstructor
@NoArgsConstructor
public class ResVideoMediaDTO {
    private long id;
    private String hlsPlayUrl;      // Link phát CDN tuyệt đối (hoặc null nếu chưa xong)
    private VideoStatusEnum status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int progress;           // Tiến độ số nguyên từ 0 -> 100%
    private String failureReason;   // Lý do lỗi xuất ra màn hình nếu xử lý thất bại
    private Instant createdAt;
}
