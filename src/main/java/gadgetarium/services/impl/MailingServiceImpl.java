package gadgetarium.services.impl;

import gadgetarium.dto.request.ContactRequest;
import gadgetarium.dto.request.EmailRequest;
import gadgetarium.dto.request.NewsLetterRequest;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.NewsLetterResponse;
import gadgetarium.entities.Contact;
import gadgetarium.entities.EmailAddress;
import gadgetarium.entities.Mailing;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.ContactRepository;
import gadgetarium.repositories.EmailRepository;
import gadgetarium.repositories.MailingRepository;
import gadgetarium.services.MailingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailingServiceImpl implements MailingService {

    private final MailingRepository mailingRepo;
    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;
    private final ContactRepository contactRepo;

    @Async
    public void sendEmail(List<String> emails, NewsLetterRequest newsLetterRequest) {
        for (String email : emails) {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            try {
                MimeMessageHelper helper = getMimeMessageHelper(newsLetterRequest, mimeMessage);
                helper.setTo(email);
                helper.setSubject("Новая рассылка!!!");
                helper.setFrom("GADGETARIUM <gadgetarium22@gmail.com>");
                javaMailSender.send(mimeMessage);
            } catch (MessagingException e) {
                throw new BadRequestException("Failed to send email to " + email);
            }
        }
    }

    private static MimeMessageHelper getMimeMessageHelper(NewsLetterRequest newsLetterRequest, MimeMessage mimeMessage) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        String htmlMessage = "<!DOCTYPE html>"
                             + "<html lang=\"en\">"
                             + "<head>"
                             + "<style>"
                             + "@import url(\"https://fonts.googleapis.com/css2?family=Inter:wght@100..900&display=swap\");"
                             + "* { margin: 0 auto; box-sizing: border-box; }"
                             + "body { width: 100%; height: 100vh; max-width: 1500px; font-family: 'Inter', sans-serif; background-color: #f4f4f9; padding: 20px; }"
                             + ".container { max-width: 750px; background-color: #fff; border-radius: 8px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); overflow: hidden; }"
                             + ".header { background-color: #c11bab; color: #fff; padding: 20px; text-align: center; }"
                             + ".header img { max-width: 60px; }"
                             + ".content { padding: 20px; }"
                             + ".content h1 { font-size: 24px; color: #333; margin-bottom: 10px; }"
                             + ".content p { font-size: 16px; color: #666; margin-bottom: 20px; }"
                             + ".content .image { text-align: center; margin-bottom: 20px; }"
                             + ".content .image img { max-width: 100%; border-radius: 8px; }"
                             + ".content .details { font-size: 16px; color: #333; }"
                             + ".footer { background-color: #c11bab; color: #fff; text-align: center; padding: 10px; }"
                             + "</style>"
                             + "</head>"
                             + "<body>"
                             + "<div class=\"container\">"
                             + "<div class=\"header\">"
                             + "<img src=\"https://gadgetarium-b12.s3.eu-central-1.amazonaws.com/a35fd672-e51f-4515-9236-dca608d5cb1b\" alt=\"GADGETARIUM\" />"
                             + "<h1>" + newsLetterRequest.nameOfNewsLetter() + "</h1>"
                             + "</div>"
                             + "<div class=\"content\">"
                             + "<div class=\"image\">"
                             + "<img src=\"" + newsLetterRequest.image() + "\" alt=\"Newsletter Image\" />"
                             + "</div>"
                             + "<p>" + newsLetterRequest.description() + "</p>"
                             + "<div class=\"details\">"
                             + "<p><strong>Start Date:</strong> " + newsLetterRequest.startDateOfDiscount() + "</p>"
                             + "<p><strong>End Date:</strong> " + newsLetterRequest.endDateOfDiscount() + "</p>"
                             + "</div>"
                             + "</div>"
                             + "<div class=\"footer\">"
                             + "<p>Thank you for being with us!</p>"
                             + "</div>"
                             + "</div>"
                             + "</body>"
                             + "</html>";
        helper.setText(htmlMessage, true);
        return helper;
    }

    @Override
    public NewsLetterResponse sendNewsLetter(NewsLetterRequest newsLetterRequest) {
        if (!newsLetterRequest.endDateOfDiscount().isAfter(newsLetterRequest.startDateOfDiscount())) {
            throw new BadRequestException("End day must be after the start day!");
        }
        if (newsLetterRequest.startDateOfDiscount().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start day must begin from this date or later!");
        }

        List<String> allEmails = emailRepository.findAll()
                .stream()
                .map(EmailAddress::getEmail)
                .collect(Collectors.toList());

        Mailing builtMailing = Mailing.builder()
                .image(newsLetterRequest.image())
                .title(newsLetterRequest.nameOfNewsLetter())
                .description(newsLetterRequest.description())
                .startDate(newsLetterRequest.startDateOfDiscount())
                .endDate(newsLetterRequest.endDateOfDiscount())
                .build();

        mailingRepo.save(builtMailing);

        sendEmail(allEmails, newsLetterRequest);

        return NewsLetterResponse.builder()
                .message("Successfully sent newsletter to emails.")
                .build();
    }

    @Override
    public HttpResponse followUs(EmailRequest emailRequest) {
        boolean b = emailRepository.existsByEmail(emailRequest.email().toLowerCase());
        if (b) {
            return HttpResponse.builder()
                    .status(HttpStatus.OK)
                    .message("you are already followed!!!")
                    .build();
        }
        EmailAddress address = new EmailAddress();
        address.setEmail(emailRequest.email());
        emailRepository.save(address);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully follow US!!!")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse contactUs(ContactRequest contactRequest) {
        Contact contact = new Contact();
        contact.setFirstname(contactRequest.firstname());
        contact.setLastname(contactRequest.lastname());
        contact.setPhoneNumber(contactRequest.phoneNumber());
        contact.setMessage(contactRequest.message());
        contactRepo.save(contact);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success!!!")
                .build();
    }
}
