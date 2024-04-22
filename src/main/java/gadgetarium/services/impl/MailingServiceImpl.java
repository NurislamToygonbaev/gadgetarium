package gadgetarium.services.impl;

import gadgetarium.dto.request.NewsLetterRequest;
import gadgetarium.dto.response.NewsLetterResponse;
import gadgetarium.entities.EmailAddress;
import gadgetarium.entities.Mailing;
import gadgetarium.exceptions.AuthenticationException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.EmailRepository;
import gadgetarium.repositories.MailingRepository;
import gadgetarium.services.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
