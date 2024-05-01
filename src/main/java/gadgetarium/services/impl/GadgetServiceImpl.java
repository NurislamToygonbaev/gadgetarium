package gadgetarium.services.impl;

import gadgetarium.dto.request.ProductsRequest;
import gadgetarium.dto.request.AddProductRequest;
import gadgetarium.dto.request.ProductsIdsRequest;
import gadgetarium.dto.request.ProductPriceRequest;
import gadgetarium.dto.request.ProductDocRequest;
import gadgetarium.dto.response.AddProductsResponse;
import gadgetarium.dto.response.GadgetResponse;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.HttpResponse;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.SubCategory;
import gadgetarium.entities.Brand;
import gadgetarium.entities.CharValue;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.Sort;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.SubCategoryRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.CharValueRepository;
import gadgetarium.repositories.BrandRepository;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import gadgetarium.services.GadgetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GadgetServiceImpl implements GadgetService {

    private final GadgetRepository gadgetRepo;
    private final SubGadgetRepository subGadgetRepo;
    private final GadgetJDBCTemplateRepository gadgetJDBCTemplateRepo;
    private final BrandRepository brandRepo;
    private final SubCategoryRepository subCategoryRepo;
    private final CharValueRepository charValueRepo;
    private static final String PHONE_URL_PREFIX = "https://nanoreview.net/ru/phone/";
    private static final String LAPTOP_URL_PREFIX = "https://nanoreview.net/ru/laptop/";


    @Override
    @Transactional
    public GadgetResponse getGadgetById(Long gadgetId) {
        Gadget gadget = gadgetRepo.getGadgetById(gadgetId);

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
    public PaginationSHowMoreGadget allGadgetsForEvery(Sort sort, Discount discount, Memory memory, Ram ram, BigDecimal costFrom, BigDecimal costUpTo, String colour, String brand, int page, int size) {
        return gadgetJDBCTemplateRepo.allGadgetsForEvery(sort, discount, memory, ram, costFrom, costUpTo, colour, brand, page, size);
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
    @Transactional
    public HttpResponse addGadget(Long subCategoryId, Long brandId, AddProductRequest addProductRequest) {
        SubCategory subCategory = subCategoryRepo.getSubCategoryById(subCategoryId);
        Brand brand = brandRepo.getBrandById(brandId);

        for (ProductsRequest productsRequest : addProductRequest.productsRequests()) {
            Gadget gadget = new Gadget();
            SubGadget subGadget = new SubGadget();

            gadget.setBrand(brand);
            gadget.setSubGadget(subGadget);
            subGadget.setGadget(gadget);
            gadget.setSubCategory(subCategory);
            subCategory.addGadget(gadget);

            subGadget.setNameOfGadget(addProductRequest.nameOfGadget());
            gadget.setReleaseDate(addProductRequest.dateOfIssue());
            gadget.setWarranty(addProductRequest.warranty());

            subGadget.setMainColour(productsRequest.mainColour());
            gadget.setMemory(productsRequest.memory());
            gadget.setRam(productsRequest.ram());
            subGadget.setCountSim(productsRequest.countSim());
            subGadget.setImages(productsRequest.images());

            gadgetRepo.save(gadget);
            gadget.setSubGadget(subGadget);
            subGadget.setGadget(gadget);
            subGadgetRepo.save(subGadget);
        }

        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("success added")
                .build();
    }

    @Override
    public List<AddProductsResponse> getNewProducts() {
        List<SubGadget> all = subGadgetRepo.findAll();
        List<AddProductsResponse> addProductsResponses = new ArrayList<>();

        for (SubGadget subGadget : all) {
            if (subGadget.getPrice() == null && subGadget.getQuantity() == 0) {
                AddProductsResponse addProductsResponse = new AddProductsResponse(
                        subGadget.getId(),
                        subGadget.getGadget().getBrand().getBrandName(),
                        subGadget.getMainColour(),
                        subGadget.getGadget().getMemory().name(),
                        subGadget.getGadget().getRam().name(),
                        subGadget.getCountSim(),
                        subGadget.getGadget().getReleaseDate()
                );
                addProductsResponses.add(addProductsResponse);
            }
        }

        return addProductsResponses;
    }

    @Override
    @Transactional
    public HttpResponse addPrice(ProductsIdsRequest productsIds) {
        for (Long id : productsIds.ids()) {
            Gadget gadget = gadgetRepo.getGadgetById(id);
            gadget.getSubGadget().setPrice(productsIds.price());
            gadget.getSubGadget().setQuantity(productsIds.quantity());

            gadgetRepo.save(gadget);
        }
        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Success price!")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse setPriceOneProduct(ProductPriceRequest productPriceRequest) {
        Gadget gadget = gadgetRepo.getGadgetById(productPriceRequest.id());
        gadget.getSubGadget().setQuantity(productPriceRequest.quantity());
        gadget.getSubGadget().setPrice(productPriceRequest.price());

        gadgetRepo.save(gadget);
        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Success set price!")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse addDocument(ProductDocRequest productDocRequest) throws gadgetarium.exceptions.IOException {
        for (Gadget gadget : gadgetRepo.findAll()) {
            if (gadget.getPDFUrl() == null && gadget.getVideoUrl() == null && gadget.getDescription() == null) {
                String apiUrl = buildApiUrl(gadget);
                try {
                    Document doc = Jsoup.connect(apiUrl).get();
                    System.out.println("HTML страница получена: " + doc.title());

                    Map<String, Map<String, String>> characteristics = extractDataFromTables(doc);

                    UUID uuid = UUID.randomUUID();
                    String hexUUID = uuid.toString().replace("-", "");
                    long article = Long.parseLong(hexUUID.substring(0, 12), 16);

                    gadget.setPDFUrl(productDocRequest.pdf());
                    gadget.setVideoUrl(productDocRequest.videoUrl());
                    gadget.setDescription(productDocRequest.description());
                    gadget.setArticle(article);

                    SubGadget subGadget = gadget.getSubGadget();


                    for (Map.Entry<String, Map<String, String>> entry : characteristics.entrySet()) {
                        String s = entry.getKey();
                        Map<String, String> value = entry.getValue();

                        CharValue charValue = new CharValue();
                        for (Map.Entry<String, String> charEntry : value.entrySet()) {
                            charValue.addCharacteristic(charEntry.getKey(), charEntry.getValue());
                        }

                        subGadget.getCharName().put(charValue, s);
                        charValueRepo.save(charValue);
                    }

                    gadgetRepo.save(gadget);
                    subGadgetRepo.save(subGadget);

                } catch (IOException e) {
                    throw new gadgetarium.exceptions.IOException(e.getMessage());
                }
            }
        }
        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Success set documents, descriptions!")
                .build();
    }

    private String buildApiUrl(Gadget gadget) {
        String modelName = gadget.getSubGadget().getNameOfGadget();
        String brand = gadget.getBrand().getBrandName();
        String categoryName = gadget.getSubCategory().getCategory().getCategoryName();

        if (categoryName.equalsIgnoreCase("phone")) {
            return PHONE_URL_PREFIX + brand.toLowerCase().replaceAll("\\s+", "-") + ("-") + modelName.toLowerCase().replaceAll("\\s+", "-");
        } else if (categoryName.equalsIgnoreCase("laptop")) {
            return LAPTOP_URL_PREFIX + brand.toLowerCase().replaceAll("\\s+", "-") + ("-") + modelName.toLowerCase().replaceAll("\\s+", "-");
        }
        return "";
    }

    private Map<String, Map<String, String>> extractDataFromTables(Document doc) {
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        Elements cards = doc.select("div.card");
        for (Element card : cards) {
            String head = card.select(".card-head h3.title-h2").text();

            String[] headers = head.split(",");
            for (String header : headers) {
                if (!data.containsKey(header.trim())) {
                    data.put(header.trim(), new LinkedHashMap<>());
                }
            }

            Elements rows = card.select("table.specs-table tbody tr");
            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() == 2) {
                    String key = cells.get(0).text();
                    String value = cells.get(1).text();

                    for (String header : headers) {
                        data.get(header.trim()).put(key, value);
                    }
                }
            }
        }
        return data;
    }
}