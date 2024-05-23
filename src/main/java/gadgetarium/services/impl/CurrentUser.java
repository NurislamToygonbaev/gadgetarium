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
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        if (email == null || email.isEmpty()) {
            return null;
        }

        return userRepo.getByEmail(email);
    }
}
