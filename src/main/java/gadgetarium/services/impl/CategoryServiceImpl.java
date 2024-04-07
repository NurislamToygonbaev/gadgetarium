package gadgetarium.services.impl;

import gadgetarium.repositories.CategoryRepository;
import gadgetarium.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepo;
}
