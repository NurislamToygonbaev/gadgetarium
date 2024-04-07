package gadgetarium.services.impl;

import gadgetarium.repositories.DiscountRepository;
import gadgetarium.services.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepo;
}
