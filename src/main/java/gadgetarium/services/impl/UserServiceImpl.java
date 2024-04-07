package gadgetarium.services.impl;

import gadgetarium.repositories.UserRepository;
import gadgetarium.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
}
