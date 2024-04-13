package gadgetarium.services.impl;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.services.GadgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GadgetServiceImpl implements GadgetService {

    private final GadgetRepository gadgetRepo;

    @Override
    public GadgetResponse getGadgetById(Long id) {

        return null;
    }
}
