package gadgetarium.services.impl;

import gadgetarium.dto.request.DiscountRequest;
import gadgetarium.dto.response.DiscountResponse;
import gadgetarium.entities.Discount;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.DiscountRepository;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.services.DiscountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.NotAcceptableStatusException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepo;
    private final GadgetRepository gadgetRepo;

    @Override
    @Transactional
    public DiscountResponse create(List<Long> gadgetId, DiscountRequest discountRequest) {
        if (!discountRequest.endDay().isAfter(discountRequest.startDay())) {
            throw new BadRequestException("End day must be after the start day!");
        }
        if (discountRequest.startDay().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start day must begin from today or later!");
        }
        for (Long id : gadgetId) {
            Gadget gadget = gadgetRepo.getGadgetById(id);
            if (gadget.getDiscount() != null) {
                throw new BadRequestException("Gadget with ID " + gadget.getId() + " already has a discount!");
            }
            Discount buildDiscount = Discount.builder()
                    .percent(discountRequest.discountSize())
                    .startDate(discountRequest.startDay())
                    .endDate(discountRequest.endDay())
                    .gadget(gadget)
                    .build();
            discountRepo.save(buildDiscount);
            gadget.setDiscount(buildDiscount);
        }
        return DiscountResponse.builder()
                .httpStatus(HttpStatus.OK)
                .message("Discount successfully added!")
                .build();
    }



//    @Transactional
//    @Override
//    public BigDecimal checkCurrentPrice(SubGadget subGadget) {
//        BigDecimal returnCurrentPrice = BigDecimal.ZERO;
//        if (subGadget.getDiscount() != null) {
//            if (subGadget.getDiscount().getEndDate().isBefore(LocalDate.now())) {
//                subGadget.setCurrentPrice(subGadget.getPrice());
//                discountRepo.delete(subGadget.getDiscount());
//                returnCurrentPrice =  subGadget.getCurrentPrice();
//            }
//        } else returnCurrentPrice =  subGadget.getCurrentPrice();
//        return returnCurrentPrice;
//    }
}
