package gadgetarium.services.impl;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import gadgetarium.services.GadgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GadgetServiceImpl implements GadgetService {

    private final GadgetRepository gadgetRepo;
    private final GadgetJDBCTemplateRepository gadgetJDBCTemplateRepo;

    @Override
    public GadgetResponse getGadgetById(Long id) {

        return null;
    }

    @Override
    public ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size) {
        return gadgetJDBCTemplateRepo.getAll(sort, discount, page, size);
    }
}
