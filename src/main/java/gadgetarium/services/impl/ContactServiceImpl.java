package gadgetarium.services.impl;

import gadgetarium.repositories.ContactRepository;
import gadgetarium.services.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepo;
}
