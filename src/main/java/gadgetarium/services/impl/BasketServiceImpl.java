package gadgetarium.services.impl;

import gadgetarium.dto.request.BasketIdsRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.SubGadgetRepository;
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
    private final SubGadgetRepository gadgetRepo;

    @Override
    @Transactional
    public HttpResponse addToBasket(Long gadgetId, int quantity) {
        if (quantity <= 0){
            throw new BadRequestException("can't be quantity -");
        }
        SubGadget gadget = gadgetRepo.getByID(gadgetId);
        User user = currentUser.get();
        user.addToBasket(gadget, quantity);
        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("successfully added to basket.")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse removeFromBasket(Long gadgetId) {
        SubGadget gadget = gadgetRepo.getByID(gadgetId);
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
        SubGadget gadget = gadgetRepo.getByID(gadgetId);
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

            GetAllBasketResponse all = new GetAllBasketResponse(
                    subGadget.getId(), subGadget.getImages().getFirst(),
                    subGadget.getGadget().getBrand().getBrandName() + " "
                    + subGadget.getNameOfGadget(),
                    subGadget.getGadget().getMemory().name(), subGadget.getMainColour(),
                    subGadget.getRating(), subGadget.getGadget().getFeedbacks().size(),
                    subGadget.getQuantity(), subGadget.getGadget().getArticle(),
                    subGadget.getCurrentPrice(), entry.getValue()
            );
            responses.add(all);
        }
        return responses;
    }

    @Override
    @Transactional
    public GetBasketAmounts allAmounts(BasketIdsRequest basketIdsRequest) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalPriceWithDiscount = BigDecimal.ZERO;
        int count = 0;

        User user = currentUser.get();

        for (Long id : basketIdsRequest.ids()) {
            SubGadget subGadget = gadgetRepo.getByID(id);
            if (user.getBasket().containsKey(subGadget)) {
                count++;
                Integer quantity = user.getBasket().get(subGadget);

                BigDecimal price = subGadget.getPrice();
                BigDecimal discount = BigDecimal.ZERO;

                if (subGadget.getDiscount() != null) {
                    int percent = subGadget.getDiscount().getPercent();
                    discount = price.multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100)));
                }

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
    public SumOrderWithGadgetResponse sumOrderWithGadgets(BasketIdsRequest basketIdsRequest) {
        User user = currentUser.get();
        List<GadgetResponseInOrder> response = new ArrayList<>();
        for (Long id : basketIdsRequest.ids()) {
            SubGadget subGadget = gadgetRepo.getByID(id);
            if (user.getBasket().containsKey(subGadget)) {
                Integer quantity = user.getBasket().get(subGadget);
                GadgetResponseInOrder inOrder = new GadgetResponseInOrder(
                        subGadget.getId(), subGadget.getImages().getFirst(),
                        subGadget.getGadget().getBrand().getBrandName() + " " +
                        subGadget.getNameOfGadget(), subGadget.getGadget().getMemory().name(),
                        subGadget.getMainColour(), subGadget.getGadget().getArticle(),
                        quantity
                );
                response.add(inOrder);
            }
        }
        return SumOrderWithGadgetResponse.builder()
                .basketAmounts(allAmounts(basketIdsRequest))
                .gadgetResponse(response)
                .build();
    }

    @Override
    @Transactional
    public HttpResponse deleteALlFromBasket(BasketIdsRequest basketIdsRequest) {
        User user = currentUser.get();
        for (Long id : basketIdsRequest.ids()) {
            SubGadget gadget = gadgetRepo.getByID(id);
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
