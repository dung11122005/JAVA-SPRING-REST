package vn.tandung.jobhunter.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@Profile("!prod") // Khóa cấu hình này lại, không chạy khi ở môi trường sản xuất (Production)
public class OpenAPIConfig {

    // HÀM 1: Cấu hình cơ chế bảo mật (JWT) cho tài liệu Swagger
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    // HÀM 2: Tạo thông tin cấu hình một Server định tuyến định danh môi trường
    private Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }

    // HÀM 3: Định nghĩa thông tin liên hệ của lập trình viên chịu trách nhiệm hệ thống
    private Contact createContact() {
        return new Contact()
                .email("dungakaishi900@gmail.com")
                .name("Tan Dung Developer")
                .url("https://tandung.vn"); // Đổi sang domain cá nhân của bạn
    }

    // HÀM 4: Định nghĩa loại bản quyền phần mềm phân phối (MIT)
    private License createLicense() {
        return new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");
    }

    // HÀM 5: Đóng gói toàn bộ tiêu đề, mô tả tóm tắt tổng quan của hệ thống API
    private Info createApiInfo() {
        return new Info()
                .title("Job Hunter Enterprise API")
                .version("1.0")
                .contact(createContact())
                .description("Hệ thống tài liệu đặc tả và kiểm thử toàn bộ API dự án mạng tuyển dụng Job Hunter")
                .termsOfService("https://tandung.vn") // Đổi sang trang điều khoản của bạn
                .license(createLicense());
    }

    // HÀM CHÍNH (BEAN): Nạp và ráp toàn bộ cấu hình nhỏ ở trên thành thực thể OpenAPI hoàn chỉnh
    @Bean
    public OpenAPI myOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo()) // Đưa tiêu đề, mô tả vào tài liệu công khai
                .servers(List.of(
                        createServer("http://localhost:8080", "Môi trường Phát triển (Local/Dev)"),
                        createServer("https://tandung.vn", "Môi trường Chạy thật (Production)"))) // Đổi URL thật của bạn
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication")) // Bật yêu cầu khóa bảo mật cho các API
                .components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())); // Đính kèm nút nạp token JWT
    }
}
