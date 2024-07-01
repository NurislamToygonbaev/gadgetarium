package gadgetarium.services.impl;

import gadgetarium.dto.response.*;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.Memory;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {

    private final CurrentUser currentUser;
    private final SubGadgetRepository subGadgetRepo;

    @Override
    @Transactional
    public HttpResponse addToBasket(Long subGadgetId, int quantity) {
        if (quantity <= 0) {
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
    public List<GetAllBasketResponse> getAllBasket() {
        User user = currentUser.get();
        Map<SubGadget, Integer> basket = user.getBasket();

        return basket.keySet().stream()
                .sorted(Comparator.comparingLong(SubGadget::getId))
                .map(subGadget -> {
                    boolean likes = GadgetJDBCTemplateRepositoryImpl.checkLikes(subGadget, user);
                    BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);
                    String memory = Memory.getMemoryToRussian(subGadget.getMemory().name());
                    return new GetAllBasketResponse(
                            subGadget.getGadget().getId(),
                            subGadget.getId(),
                            subGadget.getImages().isEmpty() ? null : subGadget.getImages().getFirst(),
                            subGadget.getGadget().getBrand().getBrandName() + " " + subGadget.getGadget().getNameOfGadget(),
                            memory,
                            subGadget.getMainColour(),
                            subGadget.getGadget().getRating(),
                            subGadget.getGadget().getFeedbacks().size(),
                            subGadget.getQuantity(),
                            subGadget.getArticle(),
                            price,
                            basket.get(subGadget),
                            likes
                    );
                })
                .collect(Collectors.toList());
    }


    @Override
    public GetBasketAmounts allAmounts(List<Long> ids) {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalPriceWithDiscount = BigDecimal.ZERO;
        int count = 0;

        User user = currentUser.get();

        for (Long id : ids) {
            SubGadget subGadget = subGadgetRepo.getByID(id);
            if (user.getBasket().containsKey(subGadget)) {
                Integer quantity = user.getBasket().get(subGadget);
                count += quantity;

                BigDecimal price = subGadget.getPrice();
                BigDecimal priceWithDiscount = GadgetServiceImpl.calculatePrice(subGadget);

                BigDecimal totalItemPrice = price.multiply(BigDecimal.valueOf(quantity));
                BigDecimal totalItemDiscount = priceWithDiscount.multiply(BigDecimal.valueOf(quantity));
                BigDecimal totalItemPriceWithDiscount = totalItemPrice.subtract(totalItemDiscount);

                totalDiscount = totalDiscount.add(totalItemDiscount);
                totalPrice = totalPrice.add(totalItemPrice);
                totalPriceWithDiscount = totalPriceWithDiscount.add(totalItemPriceWithDiscount);
            }
        }

        return GetBasketAmounts.builder()
                .quantity(count)
                .discountPrice(totalPriceWithDiscount)
                .price(totalPrice)
                .currentPrice(totalDiscount)
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
                String memory = Memory.getMemoryToRussian(subGadget.getMemory().name());
                GadgetResponseInOrder inOrder = new GadgetResponseInOrder(
                        subGadget.getId(), subGadget.getImages().isEmpty() ? null : subGadget.getImages().getFirst(),
                        subGadget.getGadget().getBrand().getBrandName() + " " +
                        subGadget.getGadget().getNameOfGadget(), memory,
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
