package gadgetarium.services.impl;

import gadgetarium.repositories.EmailRepository;
import gadgetarium.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class EmailServiceImpl implements EmailService {
    private final EmailRepository emailRepo;
}
