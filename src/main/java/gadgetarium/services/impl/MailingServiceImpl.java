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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailingServiceImpl implements MailingService {

    private final MailingRepository mailingRepo;
    private final EmailRepository emailRepository;
    private final JavaMailSender javaMailSender;
    private final ContactRepository contactRepo;

    @Override
    public NewsLetterResponse sendNewsLetter(NewsLetterRequest newsLetterRequest) {
        if (!newsLetterRequest.endDateOfDiscount().isAfter(newsLetterRequest.startDateOfDiscount()))
            throw new BadRequestException("End day must be after than start day!");
        if (newsLetterRequest.startDateOfDiscount().isBefore(LocalDate.now()))
            throw new BadRequestException("Start day must begin from this date or later!");
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        List<EmailAddress> all = emailRepository.findAll();
        Mailing buildedMailing = Mailing.builder()
                .image(newsLetterRequest.image())
                .title(newsLetterRequest.nameOfNewsLetter())
                .description(newsLetterRequest.description())
                .startDate(newsLetterRequest.startDateOfDiscount())
                .endDate(newsLetterRequest.endDateOfDiscount())
                .build();
        mailingRepo.save(buildedMailing);
        try {
            for (EmailAddress emailAddress : all) {
                mailMessage.setFrom("ntoygonbaev098@gmail.com");
                mailMessage.setTo(emailAddress.getEmail());
                mailMessage.setSubject("New discounts:");
                mailMessage.setText("Image: " + newsLetterRequest.image() + "\n" + "Title: " + newsLetterRequest.nameOfNewsLetter() + "\n" + "Description: " + newsLetterRequest.description() + "\n" + "Start date: " + newsLetterRequest.startDateOfDiscount() + "\n" + "End date: " + newsLetterRequest.endDateOfDiscount());
                javaMailSender.send(mailMessage);
            }
        }catch (MailAuthenticationException e){
            throw new AuthenticationException(e.getMessage());
        }
        return NewsLetterResponse.builder()
                .message("Successfully send news letter to emails.")
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
