package vn.tandung.jobhunter.util.constant;

public enum S3FolderEnum {
    // Các folder công khai
    USER_AVATAR("public/avatars"),
    COMPANY_LOGO("public/company-logos"),
    JOB_POSTER("public/job-posters"),
    
    // Các folder bảo mật
    RESUME_PDF("private/resumes"),
    
    // Các folder chứa luồng video stream
    VIDEO_COMPANY("video-streams/companies"),
    VIDEO_USER_CV("video-streams/user-cvs");

    private final String path;

    S3FolderEnum(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    // Hàm kiểm tra xem chuỗi folder FE gửi lên có hợp lệ không
    public static boolean isValidFolder(String inputFolder) {
        for (S3FolderEnum folder : S3FolderEnum.values()) {
            if (folder.getPath().equalsIgnoreCase(inputFolder)) {
                return true;
            }
        }
        return false;
    }
}