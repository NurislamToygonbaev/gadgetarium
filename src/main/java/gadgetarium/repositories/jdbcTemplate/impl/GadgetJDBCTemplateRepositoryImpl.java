package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.response.*;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.*;
import gadgetarium.repositories.GadgetRepository;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.UserRepository;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import gadgetarium.services.impl.CurrentUser;
import gadgetarium.services.impl.GadgetServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GadgetJDBCTemplateRepositoryImpl implements GadgetJDBCTemplateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUser currentUser;
    private final SubGadgetRepository subGadgetRepo;
    private final GadgetRepository gadgetRepo;

    @Override
    public ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;
        String orderBy = "";
        String where = "";
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);


        if (sort != null) {
            if (sort.equals(Sort.NEW_PRODUCTS)) orderBy = " order by ga.release_date desc ";
            else if (sort.equals(Sort.PROMOTION)) {
                if (discount != null) {
                    if (discount.equals(Discount.ALL_DISCOUNTS)) where = " and  d.percent is not null ";
                    else if (discount.equals(Discount.UP_TO_50)) where = " and  d.percent < 50 ";
                    else if (discount.equals(Discount.OVER_50)) where = " and  d.percent > 50 ";
                }
            } else if (sort.equals(Sort.RECOMMENDED)) where = " and  g.rating > 4 ";
            else if (sort.equals(Sort.HIGH_TO_LOW)) orderBy = " order by g.price desc ";
            else if (sort.equals(Sort.LOW_TO_HIGH)) orderBy = " order by g.price asc ";

        }

        List<PaginationGadget> list = jdbcTemplate.query("""
                        select sg.id,
                               array_agg(gi.images) as images,
                               sg.article,
                               concat(b.brand_name, ' ', g.name_of_gadget) as nameOfGadget,
                               g.release_date,
                               sg.quantity,
                               d.percent,
                               sg.price
                        from sub_gadgets sg
                        join gadgets g on g.id = sg.gadget_id
                        left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
                        join brands b on g.brand_id = b.id
                        left outer join discounts d on g.id = d.gadget_id
                        where sg.remoteness_status ="""+"'"+status+"'"+"""
                        """ + where + """
                        group by sg.id, sg.article, g.name_of_gadget, g.release_date,
                                   b.brand_name,  sg.quantity, d.percent, sg.price, d.end_date
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
                            id,
                            imagesFirst,
                            rs.getLong("article"),
                            rs.getString("nameOfGadget"),
                            rs.getDate("release_date").toLocalDate(),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price"),
                            rs.getInt("percent"),
                            price
                    );
                });
        return ResultPaginationGadget.builder()
                .sort(sort)
                .discount(discount)
                .page(page)
                .size(size)
                .paginationGadgets(list)
                .build();
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
                where += " and sg.price > '"+costFrom+"'";
            } else if (costUpTo != null) {
                where += " and sg.price < '"+costUpTo+"'";
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
                    orderBy = " order by  sg.price asc ";
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
                      (g.rating > 3.9 or (
                             select count(*) from feedbacks f where f.gadget_id = g.id
                       ) > 10) as recommend
                from sub_gadgets sg
                left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
                left join gadgets g on g.id = sg.gadget_id 
                left join brands b on g.brand_id = b.id
                left join sub_categories sc on g.sub_category_id = sc.id 
                left outer join discounts d on g.id = d.gadget_id
                left join feedbacks f on g.id = f.gadget_id
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
}
