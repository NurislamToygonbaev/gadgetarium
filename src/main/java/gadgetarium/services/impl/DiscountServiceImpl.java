package gadgetarium.services.impl;

import gadgetarium.dto.request.DiscountRequest;
import gadgetarium.dto.response.DiscountResponse;
import gadgetarium.entities.Discount;
import gadgetarium.entities.SubGadget;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.DiscountRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.services.DiscountService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.NotAcceptableStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepo;
    private final SubGadgetRepository subGadgetRepository;

    @PostConstruct
    public void postCurrentPrice() {
        for (SubGadget subGadget : subGadgetRepository.findAll()) {
            if (subGadget.getCurrentPrice() == null) {
                if (subGadget.getDiscount() != null) {
                    subGadget.setCurrentPrice(subGadget.getPrice().multiply(BigDecimal.valueOf(subGadget.getDiscount().getPercent())).divide(BigDecimal.valueOf(100)));
                } else subGadget.setCurrentPrice(subGadget.getPrice());
            }
        }
    }

    @Transactional
    @Override
    public DiscountResponse create(List<Long> subGadgetsId, DiscountRequest discountRequest) {
        if (!discountRequest.endDay().isAfter(discountRequest.startDay()))
            throw new BadRequestException("End day must be after than start day!");
        if (discountRequest.startDay().isBefore(LocalDate.now()))
            throw new BadRequestException("Start day must begin from this date or later!");
        for (int i = 0; i < subGadgetsId.size(); i++) {
            SubGadget subGadget = subGadgetRepository.findById(subGadgetsId.get(i)).orElseThrow(() ->
                    new NotAcceptableStatusException("SubGadget by id not found!"));
            if (subGadget.getDiscount() != null) {
                throw new BadRequestException("Subgaddet with id " + subGadget.getId() + " already has discount!");
            }
            Discount buildDiscount = Discount.builder()
                    .percent(discountRequest.discountSize())
                    .startDate(discountRequest.startDay())
                    .endDate(discountRequest.endDay())
                    .subGadget(subGadget)
                    .build();
            discountRepo.save(buildDiscount);
            subGadget.setCurrentPrice(subGadget.getPrice().subtract(subGadget.getPrice().multiply(BigDecimal.valueOf(discountRequest.discountSize())).divide(BigDecimal.valueOf(100))));

        }
        return DiscountResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("Discount success added!")
                .build();
    }

    @Transactional
    public BigDecimal checkCurrentPrice(SubGadget subGadget) {
        BigDecimal returnCurrentPrice = BigDecimal.ZERO;
        if (subGadget.getDiscount() != null) {
            if (subGadget.getDiscount().getEndDate().isBefore(LocalDate.now())) {
                subGadget.setCurrentPrice(subGadget.getPrice());
                discountRepo.delete(subGadget.getDiscount());
                returnCurrentPrice =  subGadget.getCurrentPrice();
            }
        } else returnCurrentPrice =  subGadget.getCurrentPrice();
        return returnCurrentPrice;
    }
}
