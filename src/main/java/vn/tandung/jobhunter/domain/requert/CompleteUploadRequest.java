package vn.tandung.jobhunter.domain.requert;

import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Getter 
@Setter
public class CompleteUploadRequest {

    private String uploadId;
    private String fileKey;
    private List<PartDTO> parts;
    private Long referenceId;      
    private String referenceType;  

    @Getter
    @Setter
    public static class PartDTO {
        private int partNumber;
        private String eTag;
    }
}
