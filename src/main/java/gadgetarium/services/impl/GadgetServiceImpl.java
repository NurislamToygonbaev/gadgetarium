package gadgetarium.services.impl;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.entities.Gadget;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.services.GadgetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class GadgetServiceImpl implements GadgetService {

    private final GadgetRepository gadgetRepo;

    @Override @Transactional
    public GadgetResponse getGadgetById(Long gadgetId) {
        Gadget gadget = gadgetRepo.getGadgetById(gadgetId);

        int percent = gadget.getSubGadget().getDiscount().getPercent();
        BigDecimal price = gadget.getSubGadget().getPrice();
        BigDecimal currentPrice = price.subtract(price.multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100))));

        return new GadgetResponse(
                gadget.getId(),
                gadget.getBrand().getLogo(),
                gadget.getSubGadget().getImages(),
                gadget.getSubGadget().getNameOfGadget(),
                gadget.getSubGadget().getQuantity(),
                gadget.getArticle(),
                gadget.getSubGadget().getRating(),
                percent,
                gadget.getSubGadget().getPrice(),
                currentPrice,
                gadget.getSubGadget().getMainColour(),
                gadget.getReleaseDate(),
                gadget.getWarranty(),
                gadget.getMemory().name(),
                gadget.getSubGadget().getCharacteristics()
                );
    }
}