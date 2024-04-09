package gadgetarium.services.impl;

import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.services.SubGadgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubGadgetServiceImpl implements SubGadgetService {

    private final SubGadgetRepository subGadgetRepo;
}
