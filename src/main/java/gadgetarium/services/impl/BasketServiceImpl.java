package gadgetarium.services.impl;

import gadgetarium.dto.response.HttpResponse;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.services.BasketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {

    private final CurrentUser currentUser;
    private final SubGadgetRepository gadgetRepo;

    @Override
    @Transactional
    public HttpResponse addToBasket(Long gadgetId, int quantity) {
        SubGadget gadget = gadgetRepo.getByID(gadgetId);
        User user = currentUser.get();
        user.addToBasket(gadget, quantity);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully added to basket.")
                .build();
    }

    @Override
    public HttpResponse removeFromBasket(Long gadgetId, int quantity) {
        SubGadget gadget = gadgetRepo.getByID(gadgetId);
        User user = currentUser.get();
        user.removeFromBasket(gadget, quantity);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully removed from basket.")
                .build();
    }

    @Override
    public HttpResponse deleteFromBasket(Long gadgetId) {
        SubGadget gadget = gadgetRepo.getByID(gadgetId);
        User user = currentUser.get();
        user.deleteFromBasket(gadget);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully deleted from basket.")
                .build();
    }
}
