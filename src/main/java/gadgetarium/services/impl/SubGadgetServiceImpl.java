package gadgetarium.services.impl;

import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.services.SubGadgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class SubGadgetServiceImpl implements SubGadgetService {
    private final SubGadgetRepository subGadgetRepo;
}
