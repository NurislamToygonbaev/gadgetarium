package gadgetarium.services.impl;

import gadgetarium.entities.User;
import gadgetarium.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepo;

    public User get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepo.getByEmail(email);
    }
}
