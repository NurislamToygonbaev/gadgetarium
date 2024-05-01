package gadgetarium.services.impl;

import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.dto.response.ViewedProductsResponse;
import gadgetarium.entities.Feedback;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import gadgetarium.services.GadgetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GadgetServiceImpl implements GadgetService {

    private final GadgetRepository gadgetRepo;
    private final SubGadgetRepository subGadgetRepo;
    private final CurrentUser currentUser;
    private final GadgetJDBCTemplateRepository gadgetJDBCTemplateRepo;

    @Override
    @Transactional
    public GadgetResponse getGadgetById(Long gadgetId) {
        Gadget gadget = gadgetRepo.getGadgetById(gadgetId);

        User user = currentUser.get();
        user.addViewed(gadget.getSubGadget());

        int percent = gadget.getSubGadget().getDiscount().getPercent();
        BigDecimal price = gadget.getSubGadget().getPrice();
        BigDecimal currentPrice = checkCurrentPrice(price, percent);

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

    @Override
    public ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size) {
        return gadgetJDBCTemplateRepo.getAll(sort, discount, page, size);
    }

    @Override
    public GadgetResponse getGadgetSelectColour(String colour, String nameOfGadget) {
        List<SubGadget> gadgets = subGadgetRepo.findByNameOfGadget(nameOfGadget);

        if (gadgets.isEmpty()) {
            throw new NotFoundException("Gadget with name: " + nameOfGadget + " not found!");
        }

        SubGadget gadget = null;

        for (SubGadget foundGadget : gadgets) {
            if (foundGadget.getMainColour().equalsIgnoreCase(colour)) {
                gadget = foundGadget;
                break;
            }
        }
        if (gadget == null) {
            throw new NotFoundException("Gadget with colour: " + colour + " not found!");
        }

        int percent = gadget.getDiscount().getPercent();
        BigDecimal price = gadget.getPrice();
        BigDecimal currentPrice = checkCurrentPrice(price, percent);

        return new GadgetResponse(
                gadget.getId(),
                gadget.getGadget().getBrand().getLogo(),
                gadget.getImages(),
                gadget.getNameOfGadget(),
                gadget.getQuantity(),
                gadget.getGadget().getArticle(),
                gadget.getRating(),
                percent,
                gadget.getPrice(),
                currentPrice,
                gadget.getMainColour(),
                gadget.getGadget().getReleaseDate(),
                gadget.getGadget().getWarranty(),
                gadget.getGadget().getMemory().name(),
                gadget.getCharacteristics()
        );
    }

    private BigDecimal checkCurrentPrice(BigDecimal price, int percent) {
        return price.subtract(price.multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100))));
    }

    @Override
    public List<ViewedProductsResponse> viewedProduct() {
        User user = currentUser.get();
        List<SubGadget> viewed = user.getViewed();
        List<ViewedProductsResponse> responses = new ArrayList<>();

        for (SubGadget subGadget : viewed) {
                responses.add(new ViewedProductsResponse(
                        subGadget.getId(),
                        subGadget.getDiscount().getPercent(),
                        subGadget.getImages().getFirst(),
                        subGadget.getNameOfGadget(),
                        subGadget.getRating(),
                        subGadget.getGadget().getFeedbacks().size(),
                        subGadget.getPrice(),
                        subGadget.getCurrentPrice()
                ));
        }

        return responses;
    }
}
