package gadgetarium.services.impl;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.services.BasketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {

    private final CurrentUser currentUser;
    private final SubGadgetRepository gadgetRepo;


    @Override
    @Transactional
    public HttpResponse addToBasket(Long gadgetId) {
        SubGadget gadget = gadgetRepo.getByID(gadgetId);
        User user = currentUser.get();
        if (user.getBasket().contains(gadget)){
            user.getBasket().remove(gadget);
        }else {
            user.getBasket().add(gadget);
        }
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully added or deleted.")
                .build();
    }
}
