package gadgetarium.services.impl;

import gadgetarium.repositories.MailingRepository;
import gadgetarium.services.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailingServiceImpl implements MailingService {

    private final MailingRepository mailingRepo;
}
