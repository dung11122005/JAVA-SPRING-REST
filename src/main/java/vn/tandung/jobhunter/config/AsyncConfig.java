package vn.tandung.jobhunter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "videoExecutor")
    public Executor videoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);     // Ít nhất 2 luồng băm video chạy song song
        executor.setMaxPoolSize(4) ;     // Tối đa 4 luồng chạy cùng lúc tùy cấu hình CPU
        executor.setQueueCapacity(50);   // Cho phép 50 video xếp hàng đợi tiếp theo
        executor.setThreadNamePrefix("HlsConvert-");
        executor.initialize();
        return executor;
    }
}
