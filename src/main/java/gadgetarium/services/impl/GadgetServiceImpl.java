package gadgetarium.services.impl;

import gadgetarium.dto.request.AddProductRequest;
import gadgetarium.dto.request.GadgetNewDataRequest;
import gadgetarium.dto.request.ProductDocRequest;
import gadgetarium.dto.request.ProductsRequest;
import gadgetarium.dto.response.*;
import gadgetarium.entities.*;
import gadgetarium.enums.Discount;
import gadgetarium.enums.*;
import gadgetarium.exceptions.AlreadyExistsException;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.*;
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
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GadgetServiceImpl implements GadgetService {

    private final GadgetRepository gadgetRepo;
    private final CategoryRepository categoryRepo;
    private final SubGadgetRepository subGadgetRepo;
    private final CurrentUser currentUser;
    private final GadgetJDBCTemplateRepository gadgetJDBCTemplateRepo;
    private final BrandRepository brandRepo;
    private final SubCategoryRepository subCategoryRepo;
    private final CharValueRepository charValueRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;

    private static final String PHONE_URL_PREFIX = "https://nanoreview.net/ru/phone/";
    private static final String LAPTOP_URL_PREFIX = "https://nanoreview.net/ru/laptop/";

    public static BigDecimal calculatePrice(SubGadget subGadget) {
        BigDecimal currentPrice = subGadget.getPrice();
        BigDecimal discountedPrice = currentPrice;

        if (subGadget.getGadget().getDiscount() != null) {
            int percent = subGadget.getGadget().getDiscount().getPercent();
            BigDecimal discountAmount = currentPrice.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100));
            discountedPrice = currentPrice.subtract(discountAmount);
        }

        return discountedPrice;
    }

    @Override
    @Transactional
    public GadgetResponse getGadgetById(Long gadgetId, String color, Memory memory, int quantity) {
       return gadgetJDBCTemplateRepo.getGadgetById(gadgetId, color, memory, quantity);
    }

    @Override
    public ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size) {
        return gadgetJDBCTemplateRepo.getAll(sort, discount, page, size);
    }

    @Override
    public PaginationSHowMoreGadget allGadgetsForEvery(Long catId, Sort sort, Discount discount, List<Memory> memory, List<Ram> ram, BigDecimal costFrom, BigDecimal costUpTo, List<String> colour, List<String> brand, int page, int size) {
        return gadgetJDBCTemplateRepo.allGadgetsForEvery(catId, sort, discount, memory, ram, costFrom, costUpTo, colour, brand, page, size);
    }

    @Override
    public List<ViewedProductsResponse> viewedProduct() {
        User user = currentUser.get();
        List<SubGadget> viewed = user.getViewed();
        List<ViewedProductsResponse> responses = new ArrayList<>();

        for (SubGadget subGadget : viewed) {
            responses.add(new ViewedProductsResponse(
                    subGadget.getId(),
                    subGadget.getGadget().getDiscount().getPercent(),
                    subGadget.getImages().getFirst(),
                    subGadget.getGadget().getNameOfGadget(),
                    subGadget.getGadget().getRating(),
                    subGadget.getGadget().getFeedbacks().size(),
                    subGadget.getPrice(),
                    calculatePrice(subGadget)
            ));
        }

        return responses;
    }

    @Override
    @Transactional
    public HttpResponse addGadget(Long subCategoryId, Long brandId, AddProductRequest addProductRequest) {
        boolean exist = gadgetRepo.existsByNameOfGadget(addProductRequest.nameOfGadget());
        Gadget gadget;

        if (exist) {
            gadget = gadgetRepo.findByNameOfGadget(addProductRequest.nameOfGadget());
        } else {
            gadget = new Gadget();
            gadget.setNameOfGadget(addProductRequest.nameOfGadget());
            gadget.setReleaseDate(addProductRequest.dateOfIssue());
            gadget.setWarranty(addProductRequest.warranty());

            SubCategory subCategory = subCategoryRepo.getSubCategoryById(subCategoryId);
            Brand brand = brandRepo.getBrandById(brandId);
            gadget.setBrand(brand);
            gadget.setSubCategory(subCategory);

            brand.addGadget(gadget);
            subCategory.addGadget(gadget);
        }

        if (gadget.getSubGadgets() == null) {
            gadget.setSubGadgets(new ArrayList<>());
        }

        for (ProductsRequest productsRequest : addProductRequest.productsRequests()) {
            for (SubGadget subGadget : gadget.getSubGadgets()) {
                if (subGadget.getMainColour().equalsIgnoreCase(productsRequest.mainColour()) &&
                    subGadget.getMemory() == productsRequest.memory() &&
                    subGadget.getRam() == productsRequest.ram()) {
                    throw new AlreadyExistsException(
                            "SubGadget with color: " + productsRequest.mainColour() +
                            ", memory: " + productsRequest.memory().name() +
                            ", ram: " + productsRequest.ram().name() + " already exists for this gadget"
                    );
                }
            }
        }

        gadgetRepo.save(gadget);

        for (ProductsRequest productsRequest : addProductRequest.productsRequests()) {
            SubGadget subGadget = new SubGadget();
            subGadget.setGadget(gadget);
            gadget.addSubGadget(subGadget);
            subGadget.setMainColour(productsRequest.mainColour());
            subGadget.setMemory(productsRequest.memory());

            if (subGadget.getImages() == null) {
                subGadget.setImages(new ArrayList<>());
            }

            subGadgetRepo.save(subGadget);
            subGadget.getImages().addAll(productsRequest.images());

            UUID uuid = UUID.randomUUID();
            String hexUUID = uuid.toString().replace("-", "");
            long article = Long.parseLong(hexUUID.substring(0, 12), 16);
            subGadget.setArticle(article);

            Category category = categoryRepo.getCategoryBySubcategoryId(subCategoryId);

            if (category.getCategoryName().toLowerCase().contains("phone") ||
                category.getCategoryName().toLowerCase().contains("laptop")) {
                subGadget.setCountSim(productsRequest.countSim());
                subGadget.setRam(productsRequest.ram());
            } else if (category.getCategoryName().toLowerCase().contains("watch") ||
                       category.getCategoryName().toLowerCase().contains("accessories")) {
                subGadget.getUniFiled().add(productsRequest.materialBracelet());
                subGadget.getUniFiled().add(productsRequest.dumas());
                subGadget.getUniFiled().add(productsRequest.genderWatch());
                subGadget.getUniFiled().add(productsRequest.materialBody());
                subGadget.getUniFiled().add(productsRequest.shapeBody());
                subGadget.getUniFiled().add(productsRequest.sizeWatch());
                subGadget.getUniFiled().add(productsRequest.waterproof());
                subGadget.getUniFiled().add(productsRequest.wireless());
            }

        }

        return HttpResponse.builder()
                .status(HttpStatus.OK)
                .message("Успешно добавлено")
                .build();
    }






    @Override
    public List<AddProductsResponse> getNewProducts() {
        List<SubGadget> all = subGadgetRepo.findAll();
        List<AddProductsResponse> addProductsResponses = new ArrayList<>();

        for (SubGadget subGadget : all) {
            if (subGadget.getPrice() == null || subGadget.getQuantity() == 0) {
                AddProductsResponse addProductsResponse = new AddProductsResponse(
                        subGadget.getId(),
                        subGadget.getGadget().getBrand().getBrandName(),
                        subGadget.getMainColour(),
                        subGadget.getMemory().name(),
                        subGadget.getRam().name(),
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
    public HttpResponse addPrice(List<Long> ids, BigDecimal price, int quantity) {
        for (Long id : ids) {
            SubGadget subGadget = subGadgetRepo.getByID(id);
            if (subGadget.getPrice() == null && subGadget.getQuantity() <= 0){
                subGadget.setPrice(price);
                subGadget.setQuantity(quantity);
                subGadgetRepo.save(subGadget);
            }
        }
        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Success added price and quantity!")
                .build();
    }

    @Override
    @Transactional
    public HttpResponse setPriceOneProduct(Long id, BigDecimal price) {
        SubGadget subGadget = subGadgetRepo.getByID(id);
        if (subGadget.getPrice() == null){
            subGadget.setPrice(price);
            subGadgetRepo.save(subGadget);
        }
        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Success set price!")
                .build();
    }

    @Override
    public HttpResponse setQuantityOneProduct(Long id, int quantity) {
        SubGadget subGadget = subGadgetRepo.getByID(id);
        if (subGadget.getQuantity() <= 0){
            subGadget.setQuantity(quantity);
            subGadgetRepo.save(subGadget);
        }

        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Success set quantity!")
                .build();
    }

    @Override
    public List<String> getAllColours(Long gadgetId) {
        gadgetRepo.getGadgetById(gadgetId);
        return gadgetRepo.getColors(gadgetId);
    }

    @Override
    public List<GadgetsResponse> globalSearch(String request) {
        return gadgetJDBCTemplateRepo.globalSearch(request);
    }

    @Override
    @Transactional
    public HttpResponse addDocument(Long subGadgetId, ProductDocRequest productDocRequest) throws gadgetarium.exceptions.IOException {
        try {
            SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);
            Gadget gadget = subGadget.getGadget();
            if (gadget == null){
                throw new BadRequestException("No gadget to add document.");
            }

            gadget.setPDFUrl(productDocRequest.pdf());
            gadget.setVideoUrl(productDocRequest.videoUrl());
            gadget.setDescription(productDocRequest.description());

            String lowerCase = gadget.getSubCategory().getCategory().getCategoryName().toLowerCase();
            if (lowerCase.contains("phone") || lowerCase.contains("laptop")) {
                String apiUrl = buildApiUrl(gadget);
                Document doc = Jsoup.connect(apiUrl).get();
                System.out.println("HTML page obtained: " + doc.title());

                Map<String, Map<String, String>> characteristics = extractDataFromTables(doc);

                for (Map.Entry<String, Map<String, String>> entry : characteristics.entrySet()) {
                    String characteristicKey = entry.getKey();
                    Map<String, String> characteristicValues = entry.getValue();

                    CharValue charValue = new CharValue();
                    charValueRepo.save(charValue);

                    for (Map.Entry<String, String> charEntry : characteristicValues.entrySet()) {
                        charValue.addCharacteristic(charEntry.getKey(), charEntry.getValue());
                    }

                    gadget.getCharName().put(charValue, characteristicKey);
                }
            }

            gadgetRepo.save(gadget);

            return HttpResponse
                    .builder()
                    .status(HttpStatus.OK)
                    .message("Documents and descriptions successfully added!")
                    .build();
        } catch (IOException e) {
            throw new gadgetarium.exceptions.IOException(e.getMessage());
        }
    }



    @Override
    public GadgetPaginationForMain mainPageDiscounts(int page, int size) {
        return gadgetJDBCTemplateRepo.mainPageDiscounts(page, size);
    }

    @Override
    public GadgetPaginationForMain mainPageNews(int page, int size) {
        return gadgetJDBCTemplateRepo.mainPageNews(page, size);
    }

    @Override
    public GadgetPaginationForMain mainPageRecommend(int page, int size) {
        return gadgetJDBCTemplateRepo.mainPageRecommend(page, size);
    }

    @Override
    public GadgetDescriptionResponse getDescriptionGadget(Long id) {
        Gadget gadget = gadgetRepo.getGadgetById(id);
        return GadgetDescriptionResponse.builder()
                .id(gadget.getId())
                .videoUrl(gadget.getVideoUrl())
                .description(gadget.getDescription())
                .build();
    }

    @Override
    public GadgetCharacteristicsResponse getCharacteristicsGadget(Long id) {
        try {
            Gadget gadget = gadgetRepo.getGadgetById(id);
            Map<String, Map<String, String>> mainCharacteristics = new HashMap<>();

            Map<CharValue, String> charName = gadget.getCharName();

            for (Map.Entry<CharValue, String> entry : charName.entrySet()) {
                CharValue charValue = entry.getKey();
                if (charValue == null || charValue.getValues() == null) {
                    log.warn("Null CharValue or its values encountered.");
                    continue;
                }
                Map<String, String> values = new HashMap<>(charValue.getValues());
                mainCharacteristics.put(entry.getValue(), values);
            }

            return GadgetCharacteristicsResponse.builder()
                    .id(gadget.getId())
                    .mainCharacteristics(mainCharacteristics)
                    .build();

        } catch (Exception e) {
            log.error("Error while retrieving gadget characteristics", e);
            return GadgetCharacteristicsResponse.builder().build();
        }
    }

    @Override
    public List<GadgetReviewsResponse> getReviewsGadget(Long id, int page, int size) {
        return gadgetJDBCTemplateRepo.getReviewsGadget(id, page, size);
    }

    @Override
    public GadgetDeliveryPriceResponse getDeliveryPriceGadget(Long id) {
        gadgetRepo.getGadgetById(id);
        return GadgetDeliveryPriceResponse.builder()
                .deliveryPrice(BigDecimal.valueOf(200))
                .build();
    }


    private String buildApiUrl(Gadget gadget) {
        String modelName = gadget.getNameOfGadget();
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

    @Override
    @Transactional
    public HttpResponse updateGadget(Long subGadgetId, GadgetNewDataRequest request) {
        SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);
        Gadget gadget = subGadget.getGadget();

        subGadget.setMainColour(request.colour());
        subGadget.setMemory(request.memory());
        subGadget.getImages().addAll((request.images()));

        String categoryName = gadget.getSubCategory().getCategory().getCategoryName().toLowerCase();
        if (categoryName.contains("phone") ||
            categoryName.contains("laptop")) {
            subGadget.setRam(request.ram());
            subGadget.setCountSim(request.countSim());
        } else if (categoryName.contains("smart") ||
                   categoryName.contains("accessories")) {

            subGadget.addUniField(request.materialBody());
            subGadget.addUniField(request.dumas());
            subGadget.addUniField(request.genderWatch());
            subGadget.addUniField(request.shapeBody());
            subGadget.addUniField(request.sizeWatch());
            subGadget.addUniField(request.waterproof());
            subGadget.addUniField(request.materialBracelet());
            subGadget.addUniField(request.wireless());
        }

        subGadgetRepo.save(subGadget);

        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Gadget updated!")
                .build();
    }


    @Override
    @Transactional
    public HttpResponse deleteGadget(Long subGadgetId) {
        SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);


        List<User> usersWithGadget = userRepo.findByBasketContainingKey(subGadget);
        for (User user : usersWithGadget) {
            user.getBasket().remove(subGadget);

            user.getComparison().removeIf(gadgetRemove -> gadgetRemove.getId().equals(subGadget.getId()));
            user.getViewed().removeIf(gadgetRemove -> gadgetRemove.getId().equals(subGadget.getId()));
            user.getLikes().removeIf(gadgetRemove -> gadgetRemove.getId().equals(subGadget.getId()));
            userRepo.save(user);
        }

        List<Order> orders = orderRepo.findBySubGadgetsContains(subGadget);
        if (!orders.isEmpty()) {
            subGadget.setRemotenessStatus(RemotenessStatus.REMOTE);
        } else {
            subGadgetRepo.delete(subGadget);
        }

        return HttpResponse
                .builder()
                .status(HttpStatus.OK)
                .message("Gadget deleted!")
                .build();
    }

    @Override
    public List<CatResponse> getCategories() {
        return categoryRepo.getAllCategories();
    }

    @Override
    public List<CatResponse> getSubCategories(Long catId) {
        return subCategoryRepo.getSubCategories(catId);
    }

    @Override
    public List<Memory> getAllMemories(Long gadgetId, String color) {
        Gadget gadgetById = gadgetRepo.getGadgetById(gadgetId);

        List<Memory> memories = new ArrayList<>();

        for (SubGadget subGadget : gadgetById.getSubGadgets()) {
            if (subGadget.getMainColour().equalsIgnoreCase(color)){
                memories.add(subGadget.getMemory());
            }
        }

        return memories;
    }

    public List<DetailsResponse> gadgetDetails() {
        return gadgetJDBCTemplateRepo.gadgetDetails();
    }
}
