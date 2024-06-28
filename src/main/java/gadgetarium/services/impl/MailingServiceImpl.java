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
    public void sendHtmlEmail(String from, List<String> to, String subject, String htmlMessage) throws EmailException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setText(htmlMessage, true);
            for (String s : to) {
                helper.setTo(s);
            }
            helper.setSubject(subject);
            helper.setFrom(from);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BadRequestException("fail");
        }
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

        try {
            String htmlMessage = "Image: " + newsLetterRequest.image() + "<br>" +
                                 "Title: " + newsLetterRequest.nameOfNewsLetter() + "<br>" +
                                 "Description: " + newsLetterRequest.description() + "<br>" +
                                 "Start date: " + newsLetterRequest.startDateOfDiscount() + "<br>" +
                                 "End date: " + newsLetterRequest.endDateOfDiscount();
            sendHtmlEmail("gadgetarium22@gmail.com", allEmails, "New discounts:", htmlMessage);
        } catch (EmailException e) {
            throw new AuthenticationException(e.getMessage());
        }

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
