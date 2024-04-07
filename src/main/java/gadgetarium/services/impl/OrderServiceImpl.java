package gadgetarium.services.impl;

import gadgetarium.repositories.OrderRepository;
import gadgetarium.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepo;
}
