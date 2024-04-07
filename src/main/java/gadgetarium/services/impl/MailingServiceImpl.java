package gadgetarium.services.impl;

import gadgetarium.repositories.MailingRepository;
import gadgetarium.services.MailingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class MailingServiceImpl implements MailingService {
    private final MailingRepository mailingRepo;
}
