package gadgetarium.services.impl;

import gadgetarium.dto.response.*;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.jdbcTemplate.impl.GadgetJDBCTemplateRepositoryImpl;
import gadgetarium.services.BasketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {

    private final CurrentUser currentUser;
    private final SubGadgetRepository subGadgetRepo;

    @Override
    @Transactional
    public HttpResponse addToBasket(Long subGadgetId, int quantity) {
        if (quantity <= 0){
            throw new BadRequestException("can't be quantity -");
        }
        SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);
        User user = currentUser.get();
        user.addToBasket(subGadget, quantity);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully added to basket.")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse removeFromBasket(Long gadgetId) {
        SubGadget gadget = subGadgetRepo.getByID(gadgetId);
        User user = currentUser.get();
        user.removeFromBasket(gadget);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully removed from basket.")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse deleteFromBasket(Long gadgetId) {
        SubGadget gadget = subGadgetRepo.getByID(gadgetId);
        User user = currentUser.get();
        if (!user.getBasket().containsKey(gadget)) {
            return HttpResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Gadget not found with id: " + gadgetId + " in the basket.")
                    .build();
        }
        user.deleteFromBasket(gadget);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully deleted from basket.")
                .build();
    }

    @Override
    public List<GetAllBasketResponse> gelAllBasket() {
        User user = currentUser.get();
        Map<SubGadget, Integer> basket = user.getBasket();

        List<GetAllBasketResponse> responses = new ArrayList<>();
        for (Map.Entry<SubGadget, Integer> entry : basket.entrySet()) {
            SubGadget subGadget = entry.getKey();
            boolean likes = GadgetJDBCTemplateRepositoryImpl.checkLikes(subGadget, user);

            BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);

            GetAllBasketResponse all = new GetAllBasketResponse(
                    subGadget.getId(), subGadget.getImages().isEmpty() ? null : subGadget.getImages().getFirst(),
                    subGadget.getGadget().getBrand().getBrandName() + " "
                    + subGadget.getGadget().getNameOfGadget(),
                    subGadget.getMemory().name(), subGadget.getMainColour(),
                    subGadget.getGadget().getRating(), subGadget.getGadget().getFeedbacks().size(),
                    subGadget.getQuantity(), subGadget.getArticle(),
                    price, entry.getValue(), likes
            );
            responses.add(all);
        }
        return responses;
    }

    @Override
    @Transactional
    public GetBasketAmounts allAmounts(List<Long> ids) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalPriceWithDiscount = BigDecimal.ZERO;
        int count = 0;

        User user = currentUser.get();

        for (Long id : ids) {
            SubGadget subGadget = subGadgetRepo.getByID(id);
            if (user.getBasket().containsKey(subGadget)) {
                count++;
                Integer quantity = user.getBasket().get(subGadget);

                BigDecimal price = subGadget.getPrice();
                BigDecimal discount = GadgetServiceImpl.calculatePrice(subGadget);

                totalDiscount = totalDiscount.add(discount.multiply(BigDecimal.valueOf(quantity)));

                BigDecimal totalItemPrice = price.multiply(BigDecimal.valueOf(quantity));
                totalPrice = totalPrice.add(totalItemPrice);

                BigDecimal totalItemPriceWithDiscount = totalItemPrice.subtract(discount.multiply(BigDecimal.valueOf(quantity)));
                totalPriceWithDiscount = totalPriceWithDiscount.add(totalItemPriceWithDiscount);
            }
        }

        return GetBasketAmounts.builder()
                .quantity(count)
                .discountPrice(totalDiscount)
                .price(totalPrice)
                .currentPrice(totalPriceWithDiscount)
                .build();
    }

    @Override
    @Transactional
    public SumOrderWithGadgetResponse sumOrderWithGadgets(List<Long> ids) {
        User user = currentUser.get();
        List<GadgetResponseInOrder> response = new ArrayList<>();
        for (Long id : ids) {
            SubGadget subGadget = subGadgetRepo.getByID(id);
            if (user.getBasket().containsKey(subGadget)) {
                Integer quantity = user.getBasket().get(subGadget);
                GadgetResponseInOrder inOrder = new GadgetResponseInOrder(
                        subGadget.getId(), subGadget.getImages().isEmpty() ? null : subGadget.getImages().getFirst(),
                        subGadget.getGadget().getBrand().getBrandName() + " " +
                        subGadget.getGadget().getNameOfGadget(), subGadget.getMemory().name(),
                        subGadget.getMainColour(), subGadget.getArticle(),
                        quantity
                );
                response.add(inOrder);
            }
        }
        return SumOrderWithGadgetResponse.builder()
                .basketAmounts(allAmounts(ids))
                .gadgetResponse(response)
                .build();
    }

    @Override
    @Transactional
    public HttpResponse deleteALlFromBasket(List<Long> ids) {
        User user = currentUser.get();
        for (Long id : ids) {
            SubGadget gadget = subGadgetRepo.getByID(id);
            if (user.getBasket().containsKey(gadget)) {
                user.deleteFromBasket(gadget);
            }
        }
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully deleted from basket.")
                .build();
    }
}
