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
                    BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);

                    return new PaginationGadget(
                            rs.getLong("gadgetId"),
                            id,
                            imagesFirst,
                            rs.getLong("article"),
                            rs.getString("nameOfGadget"),
                            String.valueOf(rs.getDate("created_at")),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price"),
                            rs.getInt("percent"),
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
            if (brand != null && !brand.isEmpty()) {
                where += " and sc.sub_category_name ilike any (array" + brand.toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (costFrom != null && costUpTo != null){
                where += " and sg.price between '"+costFrom+"' and '"+costUpTo+"'";
            } else if (costFrom != null){
                where += " and sg.price >= '"+costFrom+"'";
            } else if (costUpTo != null) {
                where += " and sg.price <= '"+costUpTo+"'";
            }
            if (colour != null && !colour.isEmpty()){
                where += " and sg.main_colour ilike any (array" + colour.toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (memory != null && !memory.isEmpty()){
                where += " and sg.memory ilike any (array" + memory.stream().map(Memory::name).toList().toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (ram != null && !ram.isEmpty()){
                where += " and sg.ram ilike any (array" + ram.stream().map(Ram::name).toList().toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
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
                    BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);
                    boolean likes = checkLikes(subGadget, user);
                    boolean comparison = checkComparison(subGadget, user);
                    boolean basket = checkBasket(subGadget, user);

                    return new GadgetsResponse(
                            gadgetId,
                            id,
                            imagesFirst,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("main_colour"),
                            rs.getInt("rating"),
                            rs.getInt("countOf"),
                            rs.getBigDecimal("price"),
                            price,
                            rs.getInt("percent"),
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
                    BigDecimal price = rs.getBigDecimal("price");
                    int percent = rs.getInt("percent");
                    BigDecimal discountedPrice = price.subtract(price.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100)));
                    return new GadgetsResponse(
                            id,
                            rs.getLong("subGadgetId"),
                            imagesFirst,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("colour"),
                            rs.getInt("rating"),
                            rs.getInt("countOf"),
                            price,
                            discountedPrice,
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
            SELECT g.id,
                   array_agg(gi.images) AS images,
                   CONCAT(b.brand_name, ' ', g.name_of_gadget) AS nameOfGadget,
                   sg.quantity,
                   d.percent,
                   COUNT(f.id) AS countOfFeedback,
                   sg.main_colour,
                   g.rating,
                   sg.memory,
                   sg.price,
                   sg.id AS subGadgetId
            FROM sub_gadgets sg
            JOIN gadgets g ON g.id = sg.gadget_id
            LEFT JOIN sub_gadget_images gi ON sg.id = gi.sub_gadget_id
            JOIN brands b ON g.brand_id = b.id
            LEFT OUTER JOIN discounts d ON g.id = d.gadget_id
            LEFT OUTER JOIN feedbacks f ON f.gadget_id = g.id
            WHERE sg.remoteness_status = ?
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
        BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);

        return GadgetResponseMainPage.builder()
                .gadgetId(gadgetId)
                .subGadgetId(subGadgetId)
                .percent(rs.getInt("percent"))
                .newProduct(subGadget.getGadget().isNew())
                .recommend(isRecommended(subGadget.getGadget()))
                .image(image)
                .quantity(rs.getInt("quantity"))
                .nameOfGadget(rs.getString("nameOfGadget"))
                .memory(rs.getString("memory"))
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
    public List<DetailsResponse> gadgetDetails() {
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        List<DetailsResponse> detailsResponses = jdbcTemplate.query("""
                select  sg.id,
                        array_agg(i.images) as images,
                        g.name_of_gadget,
                        sg.main_colour,
                        sg.count_sim,
                        sg.ram,
                        sg.memory,
                        sg.quantity,
                        sg.price
                from sub_gadgets sg
                left join sub_gadget_images i on i.sub_gadget_id = sg.id
                join gadgets g on sg.gadget_id = g.id
                where sg.remoteness_status ="""+"'"+status+"'"+"""
                group by sg.id, g.name_of_gadget, sg.main_colour, sg.count_sim, sg.ram, sg.memory, sg.quantity, sg.price                       
                        """,
                (rs, rowNum) -> {

                    Array imagesArray = rs.getArray("images");
                    String[] images = null;
                    if (imagesArray != null) {
                        images = (String[]) imagesArray.getArray();
                    }
                    String image = images != null && images.length > 0 ? images[0] : null;

                    return DetailsResponse.builder()
                            .id(rs.getLong(1))
                            .image(image)
                            .nameOfGadget(rs.getString(3))
                            .colour(rs.getString(4))
                            .countSim(rs.getInt(5))
                            .ram(rs.getString(6))
                            .memory(rs.getString(7))
                            .quantity(rs.getInt(8))
                            .price(rs.getBigDecimal(9))
                            .build();
                });

        return detailsResponses;
    }


    @Override
    @Transactional
    public GadgetResponse getGadgetById(Long gadgetId, String color, Memory memory, int quantity) {
        User user = getCurrentUser();
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
            condition.append(" and sg.memory = :memory");
            params.put("memory", memory.name());
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
               g.pdfurl
        from gadgets g
        join sub_gadgets sg on sg.gadget_id = g.id
        left join sub_gadget_images i on i.sub_gadget_id = sg.id
        left join sub_gadget_uni_filed su on su.sub_gadget_id = sg.id
        join brands b on b.id = g.brand_id
        left outer join discounts d on d.gadget_id = g.id
        where g.id = :gadgetId
        and sg.remoteness_status = :status
        """ + condition +"""
         group by g.id, sg.id, b.logo, g.name_of_gadget,
         sg.quantity, sg.article, g.rating, d.percent,
          sg.price, sg.main_colour, g.release_date, g.warranty,
          sg.memory, sg.ram, sg.count_sim, g.pdfurl
        """, params, (rs, rowNum
                ) -> {
            Long id = rs.getLong("gadget_id");
            Gadget gadget = gadgetRepo.getGadgetById(id);

            Long subGadgetId = rs.getLong("sub_gadget_id");
            SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);

            Array imagesArray = rs.getArray("images");
            List<String> images = imagesArray != null ? Arrays.asList((String[]) imagesArray.getArray()) : Collections.emptyList();


            Array uniField = rs.getArray("uniField");
            List<String> fields = uniField != null ? Arrays.asList((String[]) uniField.getArray()) : Collections.emptyList();

            BigDecimal finalPrice = GadgetServiceImpl.calculatePrice(subGadget);
            if (quantity > 1) {
                finalPrice = finalPrice.multiply(BigDecimal.valueOf(quantity));
            }

            if (user != null && !user.getViewed().contains(subGadget)){
                user.addViewed(subGadget);
            }

            return GadgetResponse.builder()
                    .gadgetId(id)
                    .subGadgetId(subGadgetId)
                    .brandLogo(rs.getString("logo"))
                    .images(images)
                    .nameOfGadget(rs.getString("name_of_gadget"))
                    .quantity(rs.getInt("quantity"))
                    .articleNumber(rs.getLong("article"))
                    .rating(rs.getFloat("rating"))
                    .percent(rs.getInt("percent"))
                    .newProduct(gadget.isNew())
                    .recommend(isRecommended(gadget))
                    .price(rs.getBigDecimal("price"))
                    .currentPrice(finalPrice)
                    .mainColour(rs.getString("main_colour"))
                    .releaseDate(String.valueOf(rs.getDate("release_date")))
                    .warranty(rs.getInt("warranty"))
                    .memory(rs.getString("memory"))
                    .ram(rs.getString("ram"))
                    .countSim(rs.getInt("count_sim"))
                    .uniField(fields)
                    .likes(checkLikes(subGadget, user))
                    .basket(checkBasket(subGadget, user))
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

}
