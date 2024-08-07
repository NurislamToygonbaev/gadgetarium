package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.response.*;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.*;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.exceptions.NotFoundException;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import gadgetarium.services.impl.CurrentUser;
import gadgetarium.services.impl.GadgetServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GadgetJDBCTemplateRepositoryImpl implements GadgetJDBCTemplateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUser currentUser;
    private final SubGadgetRepository subGadgetRepo;
    private final GadgetRepository gadgetRepo;

    @Override
    public ResultPaginationGadget getAll(GetType getType, String keyword, LocalDate startDate, LocalDate endDate, Sort sort, Discount discount, int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;
        String orderBy = "";
        String where = "";
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        if (getType != null) {
            switch (getType) {
                case IN_FAVORITES:
                    where += " and ul.likes_id is not null ";
                    break;
                case IN_BASKET:
                    where += " and ub.basket_key is not null ";
                    break;
                case ON_SALE:
                    where += " and d.percent is not null ";
                    break;
                default:
                    break;
            }
        }

        if (endDate != null && startDate != null){
            if (!endDate.isAfter(startDate)) {
                throw new BadRequestException("End day must be after the start day!");
            }
        }
        if (startDate != null){
            if (!startDate.isBefore(LocalDate.now())) {
                throw new BadRequestException("Start day must before from today!");
            }
        }

        if (keyword != null && !keyword.isEmpty()) {
            where += " and (cast(sg.article as text) like '%" + keyword + "%' " +
                     " or b.brand_name ilike '%" + keyword + "%' " +
                     " or g.name_of_gadget ilike '%" + keyword + "%') ";
        }

        if (startDate != null && endDate != null){
            where += " and g.created_at between '"+startDate+"' and '"+endDate+"' ";
        } else if (startDate != null){
            where += " and g.created_at >= '"+startDate+"'";
        } else if (endDate != null) {
            where += " and g.created_at <= '"+endDate+"'";
        }

        if (sort != null) {
            if (sort.equals(Sort.NEW_PRODUCTS)) orderBy = " order by g.created_at desc ";
            else if (sort.equals(Sort.PROMOTION)) {
                if (discount != null) {
                    if (discount.equals(Discount.ALL_DISCOUNTS)) where = " and  d.percent is not null ";
                    else if (discount.equals(Discount.UP_TO_50)) where = " and  d.percent < 50 ";
                    else if (discount.equals(Discount.OVER_50)) where = " and  d.percent > 50 ";
                }
            } else if (sort.equals(Sort.RECOMMENDED)) where = " and  g.rating > 3.9 or (select count(*) from orders o where o.id = og.orders_id) > 10 ";
            else if (sort.equals(Sort.HIGH_TO_LOW)) orderBy = " order by sg.price desc ";
            else if (sort.equals(Sort.LOW_TO_HIGH)) orderBy = " order by sg.price asc ";

        }

        List<PaginationGadget> list = jdbcTemplate.query("""
                        select sg.id,
                               g.id as gadgetId,
                               array_agg(gi.images) as images,
                               sg.article,
                               concat(b.brand_name, ' ', g.name_of_gadget) as nameOfGadget,
                               g.created_at,
                               sg.quantity,
                               d.percent,
                               sg.price
                        from sub_gadgets sg
                        join gadgets g on g.id = sg.gadget_id
                        left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
                        join brands b on g.brand_id = b.id
                        left join orders_sub_gadgets og on sg.id = og.sub_gadgets_id 
                        left join orders o on o.id = og.orders_id
                        left outer join discounts d on g.id = d.gadget_id
                        left outer join users_likes ul on ul.likes_id = sg.id
                        left outer join user_basket ub on ub.basket_key = sg.id
                        where sg.remoteness_status ="""+"'"+status+"'"+"""
                        """ + where + """
                         group by sg.id, sg.article, g.name_of_gadget, g.created_at, gadgetId,
                                   b.brand_name,  sg.quantity, d.percent, sg.price
                        """ + orderBy + """
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {

                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String imagesFirst = imagesArray.length > 0 ? imagesArray[0] : null;

                    Long id = rs.getLong("id");
                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    int percent = rs.getInt("percent");
                    BigDecimal price = BigDecimal.ZERO;
                    if (percent > 0){
                        price = GadgetServiceImpl.calculatePrice(subGadget);
                    }

                    return new PaginationGadget(
                            rs.getLong("gadgetId"),
                            id,
                            imagesFirst,
                            rs.getLong("article"),
                            rs.getString("nameOfGadget"),
                            String.valueOf(rs.getDate("created_at")),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price"),
                            percent,
                            price
                    );
                });

        return ResultPaginationGadget.builder()
                .keyword(keyword)
                .startDate(startDate)
                .endDate(endDate)
                .allProduct(getAllProductCount())
                .onSale(getOnSaleCount())
                .inFavorites(getInFavoritesCount())
                .inBasket(getInBasketCount())
                .sort(sort)
                .discount(discount)
                .page(page)
                .size(size)
                .paginationGadgets(list)
                .build();
    }

    private int getAllProductCount() {
        String sql = "select count(*) from sub_gadgets g " +
                     " where g.remoteness_status = ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(RemotenessStatus.NOT_REMOTE)}, Integer.class);
        return count != null ? count : 0;
    }

    private int getOnSaleCount() {
        String sql = "select count(*) from sub_gadgets sg " +
                     " join gadgets g on sg.gadget_id = g.id " +
                     " join discounts d on g.id = d.gadget_id " +
                     "where sg.remoteness_status = ? and d.percent is not null";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(RemotenessStatus.NOT_REMOTE)}, Integer.class);
        return count != null ? count : 0;
    }

    private int getInFavoritesCount() {
        String sql = "select count(*) from users_likes u " +
                     "join sub_gadgets g on u.likes_id = g.id " +
                     "where g.remoteness_status = ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(RemotenessStatus.NOT_REMOTE)}, Integer.class);
        return count != null ? count : 0;
    }

    private int getInBasketCount() {
        String sql = "select count(*) from user_basket b " +
                     "join sub_gadgets g on b.basket_key = g.id " +
                     "where g.remoteness_status = ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(RemotenessStatus.NOT_REMOTE)}, Integer.class);
        return count != null ? count : 0;
    }


    @Override
    public PaginationSHowMoreGadget allGadgetsForEvery(Long catId, Sort sort, Discount discount, List<Memory> memory, List<Ram> ram, BigDecimal costFrom, BigDecimal costUpTo, List<String> colour, List<String> brand, int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;

        String orderBy = "";
        String where = "";
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        if (brand != null || sort != null || discount != null || memory != null || ram != null || costFrom != null || costUpTo != null || colour != null) {
            if (costFrom != null && costUpTo != null){
                where += " and sg.price between '"+costFrom+"' and '"+costUpTo+"'";
            } else if (costFrom != null){
                where += " and sg.price >= '"+costFrom+"'";
            } else if (costUpTo != null) {
                where += " and sg.price <= '"+costUpTo+"'";
            }

            if (brand != null && !brand.isEmpty()) {
                where += " and sc.sub_category_name ilike any (array" + brand.toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }

            if (colour != null){
                where += " and sg.main_colour ilike any (array" + colour.toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (memory != null && !memory.isEmpty()) {
                String memoryCondition = memory.stream()
                        .map(name -> " sg.memory ilike '" + name + "' ")
                        .collect(Collectors.joining(" or "));

                where += " and (" + memoryCondition + ") ";
            }

            if (ram != null && !ram.isEmpty()) {
                String ramCondition = ram.stream()
                        .map(name -> " sg.ram ilike '" + name + "' ")
                        .collect(Collectors.joining(" or "));

                where += " and (" + ramCondition + ") ";
            }

            if (sort != null){
                if (sort.equals(Sort.RECOMMENDED)){
                    where += " and g.rating > 3.9 or (select count(*) from orders o where o.id = og.orders_id) > 10 ";
                } else if (sort.equals(Sort.NEW_PRODUCTS)) {
                    orderBy = " order by g.created_at desc ";
                } else if (sort.equals(Sort.PROMOTION)) {
                    if (discount != null){
                        if (discount.equals(Discount.ALL_DISCOUNTS)){
                            where += " and d.percent is not null ";
                        } else if (discount.equals(Discount.UP_TO_50)) {
                            where += " and d.percent < 50 ";
                        } else if (discount.equals(Discount.OVER_50)) {
                            where += " and d.percent > 50 ";
                        }
                    }
                } else if (sort.equals(Sort.HIGH_TO_LOW)) {
                    orderBy = " order by sg.price desc ";
                } else if (sort.equals(Sort.LOW_TO_HIGH)) {
                    orderBy = " order by sg.price asc ";
                }
            }
        }

        List<GadgetsResponse> gadgetsResponses = jdbcTemplate.query(
                "select " +
                "g.id, " +
                "array_agg(gi.images) as images, " +
                "concat(sc.sub_category_name, ' ', g.name_of_gadget) as nameofgadget, " +
                "sg.memory, " +
                "sg.main_colour, " +
                "g.rating, " +
                "count(f.id) as countof, " +
                "sg.quantity, " +
                "d.percent, " +
                "sg.price, " +
                "count(o.id) as totalorders, " +
                "g.release_date, " +
                "sg.id as subGadgetId " +
                "from " +
                "sub_gadgets sg " +
                "join gadgets g on g.id = sg.gadget_id " +
                "left join sub_gadget_images gi on sg.id = gi.sub_gadget_id " +
                "join brands b on g.brand_id = b.id " +
                "join sub_categories sc on g.sub_category_id = sc.id " +
                "join categories c on sc.category_id = c.id " +
                "left join discounts d on g.id = d.gadget_id " +
                "left join orders_sub_gadgets og on sg.id = og.sub_gadgets_id " +
                "left join orders o on o.id = og.orders_id " +
                "left outer join feedbacks f on g.id = f.gadget_id " +
                "where c.id ='" + catId + "' " +
                " and sg.remoteness_status ='" + status + "' " +
                where +
                " group by " +
                "g.id, g.name_of_gadget, sc.sub_category_name, sg.quantity, d.percent, sg.price, " +
                "sg.memory, sg.main_colour, g.rating, g.release_date, d.percent, subGadgetId " +
                orderBy +
                " limit ? offset ? ",
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String imagesFirst = imagesArray.length > 0 ? imagesArray[0] : null;
                    Long id = rs.getLong("subGadgetId");
                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    Long gadgetId = rs.getLong("id");
                    Gadget gadget = gadgetRepo.getGadgetById(gadgetId);
                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }
                    int percent = rs.getInt("percent");
                    BigDecimal price = BigDecimal.ZERO;
                    if (percent > 0){
                        price = GadgetServiceImpl.calculatePrice(subGadget);
                    }
                    boolean likes = checkLikes(subGadget, user);
                    boolean comparison = checkComparison(subGadget, user);
                    boolean basket = checkBasket(subGadget, user);

                    String mem = rs.getString("memory");
                    String memRussian = Memory.getMemoryToRussian(mem);

                    return new GadgetsResponse(
                            gadgetId,
                            id,
                            imagesFirst,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            memRussian,
                            rs.getString("main_colour"),
                            rs.getInt("rating"),
                            rs.getInt("countOf"),
                            rs.getBigDecimal("price"),
                            price,
                            percent,
                            gadget.isNew(),
                            isRecommended(gadget),
                            likes,
                            comparison,
                            basket
                    );
                });

        return PaginationSHowMoreGadget.builder()
                .sort(sort)
                .discount(discount)
                .page(page)
                .size(size)
                .brand(brand)
                .costFrom(costFrom)
                .costUpTo(costUpTo)
                .colour(colour)
                .memory(memory)
                .ram(ram)
                .responses(gadgetsResponses)
                .build();
    }

    public static boolean checkLikes(SubGadget subGadget, User user) {
        if (user == null || subGadget == null) {
            return false;
        }
        return user.getLikes().stream()
                .anyMatch(like -> like.getId().equals(subGadget.getId()));
    }

    public static boolean checkComparison(SubGadget subGadget, User user) {
        if (user == null || subGadget == null) {
            return false;
        }
        return user.getComparison().stream()
                .anyMatch(comp -> comp.getId().equals(subGadget.getId()));
    }

    public static boolean checkBasket(SubGadget subGadget, User user) {
        if (user == null || subGadget == null) {
            return false;
        }
        return user.getBasket().keySet().stream()
                .anyMatch(basketGadget -> basketGadget.getId().equals(subGadget.getId()));
    }

    @Override
    public GadgetPaginationForMain mainPageDiscounts(int page, int size) {
        String additionalWhereClause = "d.percent is not null";
        return getGadgetPaginationForMain(page, size, additionalWhereClause, null, null);
    }

    @Override
    public GadgetPaginationForMain mainPageNews(int page, int size) {
        String additionalOrderByClause = "g.created_at desc";
        return getGadgetPaginationForMain(page, size, null, additionalOrderByClause, null);
    }

    @Override
    public GadgetPaginationForMain mainPageRecommend(int page, int size) {
        String havingClause = "g.rating > 3.9 or count(f.id) > 10";
        return getGadgetPaginationForMain(page, size, null, null, havingClause);
    }

    @Override
    public List<GadgetsResponse> globalSearch(String request) {
        String searchPattern = "%" + request + "%";

        return jdbcTemplate.query("""
                select g.id,
                       sg.id as subGadgetId,
                       array_agg(gi.images) as images,
                       sg.quantity,
                       concat(sc.sub_category_name, ' ', g.name_of_gadget) as nameOfGadget,
                       sg.memory,
                       sg.main_colour as colour,
                       g.rating,
                       count(f.id) as countof,
                       sg.price,
                       sg.price - (sg.price * coalesce(d.percent, 0) / 100) as currentPrice,
                       d.percent,
                       g.created_at > now() - interval '30 days' as newProduct,
                      (g.rating > 3.9 or
                       count(o.id) > 10) as recommend
                from sub_gadgets sg
                left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
                left join gadgets g on g.id = sg.gadget_id 
                left join brands b on g.brand_id = b.id
                left join sub_categories sc on g.sub_category_id = sc.id 
                left outer join discounts d on g.id = d.gadget_id
                left join feedbacks f on g.id = f.gadget_id
                left join orders_sub_gadgets og on sg.id = og.sub_gadgets_id
                left join orders o on o.id = og.orders_id
                where g.name_of_gadget ilike ? 
                   or sc.sub_category_name ilike ?
                   or b.brand_name ilike ?
                group by g.id, sg.id, sc.sub_category_name, g.name_of_gadget, sg.quantity, g.rating, d.percent, sg.price,  sg.main_colour, sg.memory 
                limit 20
                """,
                new Object[]{searchPattern, searchPattern, searchPattern},
                (rs, rowNum) -> {
                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String imagesFirst = imagesArray.length > 0 ? imagesArray[0] : null;
                    Long id = rs.getLong("id");
                    SubGadget subGadget = subGadgetRepo.getByID(id);

                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }

                    int percent = rs.getInt("percent");
                    BigDecimal price = BigDecimal.ZERO;
                    if (percent > 0){
                        price = GadgetServiceImpl.calculatePrice(subGadget);
                    }

                    String mem = rs.getString("memory");
                    String memRussian = Memory.getMemoryToRussian(mem);

                    return new GadgetsResponse(
                            id,
                            rs.getLong("subGadgetId"),
                            imagesFirst,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            memRussian,
                            rs.getString("colour"),
                            rs.getInt("rating"),
                            rs.getInt("countOf"),
                            rs.getBigDecimal("price"),
                            price,
                            percent,
                            rs.getBoolean("newProduct"),
                            rs.getBoolean("recommend"),
                            checkLikes(subGadget, user),
                            checkComparison(subGadget, user),
                            checkBasket(subGadget, user)
                    );
                });
    }

    private GadgetPaginationForMain getGadgetPaginationForMain(int page, int size, String additionalWhereClause, String additionalOrderByClause, String havingClause) {
        int offset = (page - 1) * size;
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        StringBuilder sqlBuilder = new StringBuilder("""
            select g.id,
                   array_agg(gi.images) as images,
                   concat(b.brand_name, ' ', g.name_of_gadget) as nameOfGadget,
                   sg.quantity,
                   d.percent,
                   count(f.id) as countOfFeedback,
                   sg.main_colour,
                   g.rating,
                   sg.memory,
                   sg.price,
                   sg.id AS subGadgetId
            from sub_gadgets sg
            join gadgets g ON g.id = sg.gadget_id
            left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
            join brands b on g.brand_id = b.id
            left outer join discounts d on g.id = d.gadget_id
            left outer join feedbacks f on f.gadget_id = g.id
            where sg.remoteness_status = ?
            """);

        if (additionalWhereClause != null) {
            sqlBuilder.append(" AND ").append(additionalWhereClause);
        }

        sqlBuilder.append(" GROUP BY g.id, g.name_of_gadget, b.brand_name, sg.quantity, d.percent, sg.price, sg.memory, g.rating, sg.main_colour, sg.id");

        if (havingClause != null) {
            sqlBuilder.append(" HAVING ").append(havingClause);
        }

        if (additionalOrderByClause != null) {
            sqlBuilder.append(" ORDER BY ").append(additionalOrderByClause);
        }

        sqlBuilder.append(" LIMIT ? OFFSET ?");

        List<GadgetResponseMainPage> responseMainPages = jdbcTemplate.query(sqlBuilder.toString(),
                new Object[]{status, size, offset}, this::mapToGadgetResponseMainPage);

        return GadgetPaginationForMain.builder()
                .page(page)
                .size(size)
                .mainPages(responseMainPages)
                .build();
    }

    private GadgetResponseMainPage mapToGadgetResponseMainPage(ResultSet rs, int rowNum) throws SQLException {
        Long gadgetId = rs.getLong("id");
        String[] imagesArray = (String[]) rs.getArray("images").getArray();
        String image = imagesArray.length > 0 ? imagesArray[0] : null;
        Long subGadgetId = rs.getLong("subGadgetId");

        SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);
        User user = getCurrentUser();

        int percent = rs.getInt("percent");
        BigDecimal price = BigDecimal.ZERO;
        if (percent > 0){
            price = GadgetServiceImpl.calculatePrice(subGadget);
        }

        String mem = rs.getString("memory");
        String memRussian = Memory.getMemoryToRussian(mem);

        return GadgetResponseMainPage.builder()
                .gadgetId(gadgetId)
                .subGadgetId(subGadgetId)
                .percent(percent)
                .newProduct(subGadget.getGadget().isNew())
                .recommend(isRecommended(subGadget.getGadget()))
                .image(image)
                .quantity(rs.getInt("quantity"))
                .nameOfGadget(rs.getString("nameOfGadget"))
                .memory(memRussian)
                .colour(rs.getString("main_colour"))
                .rating(rs.getDouble("rating"))
                .count(rs.getInt("countOfFeedback"))
                .price(rs.getBigDecimal("price"))
                .currentPrice(price)
                .likes(checkLikes(subGadget, user))
                .comparison(checkComparison(subGadget, user))
                .basket(checkBasket(subGadget, user))
                .build();
    }

    private User getCurrentUser() {
        try {
            return currentUser.get();
        } catch (Exception ignored){
            return null;
        }
    }

    public static boolean isRecommended(Gadget gadget) {
        return gadget.getRating() > 3.9 ||
               gadget.getSubGadgets().stream()
                       .anyMatch(subGadget -> subGadget.getOrders().size() > 10);
    }

    @Override
    public List<DetailsResponse> gadgetDetails(Long gadgetId) {
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        return jdbcTemplate.query("""
                select  g.id as gadgetId,
                        sg.id,
                        array_agg(i.images) as images,
                        concat(b.brand_name, ' ', g.name_of_gadget) as nameOfGadget,
                        sg.main_colour,
                        sg.count_sim,
                        sg.ram,
                        sg.memory,
                        sg.quantity,
                        sg.price
                from sub_gadgets sg
                left join sub_gadget_images i on i.sub_gadget_id = sg.id
                join gadgets g on sg.gadget_id = g.id
                join brands b on g.brand_id = b.id
                where g.id = ? and sg.remoteness_status = ?
                group by sg.id, g.name_of_gadget, sg.main_colour, g.id,
                 sg.count_sim, sg.ram, sg.memory, sg.quantity, sg.price, b.brand_name
                 """,
                new Object[]{gadgetId, status},
                (rs, rowNum) -> {

                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String image = imagesArray.length > 0 ? imagesArray[0] : null;

                    String mem = rs.getString("memory");
                    String memRussian = Memory.getMemoryToRussian(mem);
                    String ram = rs.getString("ram");
                    String ramRussian = Ram.getRamToRussian(ram);

                    return DetailsResponse.builder()
                            .gadgetId(rs.getLong(1))
                            .subGadgetId(rs.getLong(2))
                            .image(image)
                            .nameOfGadget(rs.getString(4))
                            .colour(rs.getString(5))
                            .countSim(rs.getInt(6))
                            .ram(ramRussian)
                            .memory(memRussian)
                            .quantity(rs.getInt(9))
                            .price(rs.getBigDecimal(10))
                            .build();
                });
    }


    @Override
    @Transactional
    public GadgetResponse getGadgetById(Long gadgetId, String color, String memory, int quantity) {
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        Map<String, Object> params = new HashMap<>();
        params.put("gadgetId", gadgetId);
        params.put("status", status);

        StringBuilder condition = new StringBuilder();
        if (color != null) {
            condition.append(" and sg.main_colour = :color");
            params.put("color", color);
        }
        if (memory != null) {
            String memoryToEnglish = Memory.getMemoryToEnglish(memory);
            condition.append(" and sg.memory = :memory");
            params.put("memory", memoryToEnglish);
        }

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

            List<GadgetResponse> response = namedParameterJdbcTemplate.query("""
               select g.id as gadget_id,
               sg.id as sub_gadget_id,
               b.logo,
               array_agg(i.images) as images,
               g.name_of_gadget,
               sg.quantity,
               sg.article,
               g.rating,
               d.percent,
               sg.price,
               sg.main_colour,
               g.release_date,
               g.warranty,
               sg.memory,
               sg.ram,
               sg.count_sim,
               array_agg(su.uni_filed) as uniField,
               g.pdfurl,
               cat.id as categoryId
        from gadgets g
        join sub_gadgets sg on sg.gadget_id = g.id
        left join sub_gadget_images i on i.sub_gadget_id = sg.id
        left join sub_gadget_uni_filed su on su.sub_gadget_id = sg.id
        join brands b on b.id = g.brand_id
        left outer join discounts d on d.gadget_id = g.id
        join sub_categories sc on g.sub_category_id = sc.id
        join categories cat on cat.id = sc.category_id
        where g.id = :gadgetId
        and sg.remoteness_status = :status
        """ + condition +"""
         group by g.id, sg.id, b.logo, g.name_of_gadget, cat.id,
         sg.quantity, sg.article, g.rating, d.percent,
          sg.price, sg.main_colour, g.release_date, g.warranty,
          sg.memory, sg.ram, sg.count_sim, g.pdfurl
        """, params, (rs, rowNum) -> {
            Long id = rs.getLong("gadget_id");
            Gadget gadget = gadgetRepo.getGadgetById(id);

            Long subGadgetId = rs.getLong("sub_gadget_id");
            SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);

            Array imagesArray = rs.getArray("images");
            List<String> images = imagesArray != null ? Arrays.asList((String[]) imagesArray.getArray()) : Collections.emptyList();


            Array uniField = rs.getArray("uniField");
            List<String> fields = uniField != null ? Arrays.asList((String[]) uniField.getArray()) : Collections.emptyList();

            int percent = rs.getInt("percent");
            BigDecimal finalPrice = BigDecimal.ZERO;
            if (percent > 0){
                finalPrice = GadgetServiceImpl.calculatePrice(subGadget);
            }
            if (quantity > 1) {
                finalPrice = finalPrice.multiply(BigDecimal.valueOf(quantity));
            }

            User userCurrent = null;
            try {
                userCurrent = currentUser.get();
            } catch (Exception ignored) {
            }

            if (userCurrent != null && !userCurrent.getViewed().contains(subGadget)){
                userCurrent.addViewed(subGadget);
            }
            boolean likes = checkLikes(subGadget, userCurrent);
            boolean basket = checkBasket(subGadget, userCurrent);

                String mem = rs.getString("memory");
                String memRussian = Memory.getMemoryToRussian(mem);

                String ram = rs.getString("ram");
                String ramRussian = Ram.getRamToRussian(ram);

                double rating = rs.getDouble("rating");
                rating = Math.round(rating * 10.0) / 10.0;
                gadget.setRating(rating);

                return GadgetResponse.builder()
                    .gadgetId(id)
                    .subGadgetId(subGadgetId)
                    .categoryId(rs.getLong("categoryId"))
                    .brandLogo(rs.getString("logo"))
                    .images(images)
                    .nameOfGadget(rs.getString("name_of_gadget"))
                    .quantity(rs.getInt("quantity"))
                    .articleNumber(rs.getLong("article"))
                    .rating(rating)
                    .percent(rs.getInt("percent"))
                    .newProduct(gadget.isNew())
                    .recommend(isRecommended(gadget))
                    .price(rs.getBigDecimal("price"))
                    .currentPrice(finalPrice)
                    .mainColour(rs.getString("main_colour"))
                    .releaseDate(String.valueOf(rs.getDate("release_date")))
                    .warranty(rs.getInt("warranty"))
                    .memory(memRussian)
                    .ram(ramRussian)
                    .countSim(rs.getInt("count_sim"))
                    .uniField(fields)
                    .likes(likes)
                    .basket(basket)
                    .pdfUrl(rs.getString("pdfurl"))
                    .build();
        });
            if (response.isEmpty()) {
                throw new NotFoundException("not found");
            } else {
                return response.getFirst();
            }
    }

    @Override
    public List<GadgetReviewsResponse> getReviewsGadget(Long id, int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;

        return jdbcTemplate.query(
                """
                select f.id as feedbackId,
                       u.image,
                       concat(u.first_name, ' ', u.last_name) AS fullName,
                       to_char(f.date_and_time, 'YYYY-MM-DD HH24:MI:SS') AS formattedTime,
                       f.rating,
                       f.description,
                       f.response_admin
                from feedbacks f
                join users u on f.user_id = u.id
                join gadgets g on f.gadget_id = g.id
                where g.id = ?
                group by f.id, u.image, u.first_name, u.last_name, f.date_and_time, f.rating, f.description, f.response_admin
                limit ? offset ?
                """,
                new Object[]{id, limit, offset},
                (rs, rowNum) -> GadgetReviewsResponse.builder()
                        .id(rs.getLong("feedbackId"))
                        .image(rs.getString("image"))
                        .fullName(rs.getString("fullName"))
                        .dateTime(rs.getString("formattedTime"))
                        .rating(rs.getDouble("rating"))
                        .description(rs.getString("description"))
                        .responseAdmin(rs.getString("response_admin"))
                        .build()
        );
    }

    @Override
    public ColorResponse getColorsWithCount() {
        List<ColorResponseWithCount> query = jdbcTemplate.query(
                "select s.main_colour, count(s.id) as quantity " +
                " from sub_gadgets s group by s.main_colour",
                new Object[]{},
                (rs, rowNum) -> {
                    String mainColor = rs.getString("main_colour");
                    int quantity = rs.getInt("quantity");
                    return new ColorResponseWithCount(
                            mainColor, String.valueOf(quantity)
                    );
                });
        return ColorResponse.builder()
                .countList(query)
                .build();
    }

}
