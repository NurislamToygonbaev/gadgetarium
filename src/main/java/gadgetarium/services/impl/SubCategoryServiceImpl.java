package gadgetarium.services.impl;

import gadgetarium.repositories.SubCategoryRepository;
import gadgetarium.services.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class SubCategoryServiceImpl implements SubCategoryService {
    private final SubCategoryRepository subCategoryRepo;
}
