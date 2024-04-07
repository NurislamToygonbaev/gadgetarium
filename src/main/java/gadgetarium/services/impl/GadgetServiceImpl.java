package gadgetarium.services.impl;

import gadgetarium.repositories.GadgetRepository;
import gadgetarium.services.GadgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class GadgetServiceImpl implements GadgetService {
    private final GadgetRepository gadgetRepo;
}
