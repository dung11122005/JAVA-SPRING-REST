package vn.hoidanit.jobhunter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.service.EmailService;
import vn.hoidanit.jobhunter.service.SubscriberService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;
    private final SubscriberService subscriberService;

    public EmailController(
            EmailService emailService,
            SubscriberService subscriberService) {
        this.emailService = emailService;
        this.subscriberService = subscriberService;
    }

    @GetMapping("/email")
    @ApiMessage("Send simple email")
    public String sendSimpleEmail() {
        //this.emailService.sendSimpleEmail();

        String username = "Dung";
    Object value = List.of("Job1", "Job2", "Job3"); // Example data for jobs

         //this.emailService.sendEmailSync("dungakaishi900@gmail.com", "test sendemail", "<h1>hello</h1>", false, true);

         this.emailService.sendEmailFromTemplateSync("dungakaishi900@gmail.com", "testsend email", "job", username, value);

        this.subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }
}
