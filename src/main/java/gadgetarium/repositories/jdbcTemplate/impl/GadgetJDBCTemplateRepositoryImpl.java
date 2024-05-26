package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.response.PaginationGadget;
import gadgetarium.dto.response.GadgetsResponse;
import gadgetarium.dto.response.GadgetPaginationForMain;
import gadgetarium.dto.response.GadgetResponseMainPage;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.*;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import gadgetarium.services.impl.CurrentUser;
import gadgetarium.services.impl.GadgetServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GadgetJDBCTemplateRepositoryImpl implements GadgetJDBCTemplateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUser currentUser;
    private final SubGadgetRepository subGadgetRepo;

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
                        from gadgets g
                        join sub_gadgets sg on g.id = sg.gadget_id
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

                    long id = rs.getLong("id");
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
                    orderBy = " order by g.release_date desc ";
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
                    orderBy = " order by g.price desc ";
                } else if (sort.equals(Sort.LOW_TO_HIGH)) {
                    orderBy = " order by g.price asc ";
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
                "gadgets g " +
                "join ( " +
                "select distinct on (sg.gadget_id) sg.id, sg.gadget_id, sg.quantity, sg.main_colour, sg.memory, sg.price " +
                "from " +
                "sub_gadgets sg " +
                "where " +
                "sg.remoteness_status ='" + status + "' " +
                "order by " +
                "sg.gadget_id, sg.id " +
                ") sg on g.id = sg.gadget_id " +
                "left join sub_gadget_images gi on sg.id = gi.sub_gadget_id " +
                "join brands b on g.brand_id = b.id " +
                "join sub_categories sc on g.sub_category_id = sc.id " +
                "join categories c on sc.category_id = c.id " +
                "left join discounts d on g.id = d.gadget_id " +
                "left join orders_sub_gadgets og on sg.id = og.sub_gadgets_id " +
                "left join orders o on o.id = og.orders_id " +
                "left outer join feedbacks f on g.id = f.gadget_id " +
                "where c.id ='" + catId + "' " +
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
                            rs.getLong("id"),
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
        int offset = (page - 1) * size;
        int limit = size;
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        List<GadgetResponseMainPage> responseMainPages = jdbcTemplate.query("""
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
                               sg.id as subGadgetId
                        from gadgets g
                        join (
                        select distinct on (sg.gadget_id) sg.id, sg.gadget_id, sg.quantity, sg.main_colour, sg.memory, sg.price
                        from sub_gadgets sg
                        where sg.remoteness_status ="""+"'"+status+"'"+"""
                        order by sg.gadget_id, sg.id
                        ) sg on g.id = sg.gadget_id
                        left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
                        join brands b on g.brand_id = b.id
                        left outer join discounts d on g.id = d.gadget_id
                        left outer join feedbacks f on f.gadget_id = g.id
                        where d.percent is not null
                        group by g.id, g.name_of_gadget, b.brand_name, sg.quantity, d.percent,
                                sg.price, sg.memory, g.rating, sg.main_colour, subGadgetId
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    Long id = rs.getLong("subGadgetId");
                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String image = imagesArray.length > 0 ? imagesArray[0] : null;

                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }
                    BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);
                    boolean likes = checkLikes(subGadget, user);
                    boolean comparison = checkComparison(subGadget, user);
                    boolean basket = checkBasket(subGadget, user);

                    return new GadgetResponseMainPage(
                            rs.getLong("id"),
                            id,
                            rs.getInt("percent"),
                            image,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("main_colour"),
                            rs.getDouble("rating"),
                            rs.getInt("countOfFeedback"),
                            rs.getBigDecimal("price"),
                            price,
                            likes,
                            comparison,
                            basket
                    );
                });

        return GadgetPaginationForMain.builder()
                .page(page)
                .size(size)
                .mainPages(responseMainPages)
                .build();
    }

    @Override
    public GadgetPaginationForMain mainPageNews(int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        List<GadgetResponseMainPage> newGadgets = jdbcTemplate.query("""
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
                               sg.id as subGadgetId
                        from gadgets g
                        join (
                        select distinct on (sg.gadget_id) sg.id, sg.gadget_id, sg.quantity, sg.main_colour, sg.memory, sg.price
                        from sub_gadgets sg
                        where sg.remoteness_status ="""+"'"+status+"'"+"""
                        order by sg.gadget_id, sg.id
                        ) sg on g.id = sg.gadget_id
                        left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
                        join brands b on g.brand_id = b.id
                        left outer join discounts d on g.id = d.gadget_id
                        left outer join feedbacks f on f.gadget_id = g.id
                        group by g.id, g.name_of_gadget, b.brand_name, sg.quantity, d.percent,
                                sg.price, sg.memory, g.rating, sg.main_colour, g.release_date,
                                subGadgetId
                        order by g.release_date desc
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String image = imagesArray.length > 0 ? imagesArray[0] : null;

                    Long id = rs.getLong("subGadgetId");
                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }
                    BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);
                    boolean likes = checkLikes(subGadget, user);
                    boolean comparison = checkComparison(subGadget, user);
                    boolean basket = checkBasket(subGadget, user);

                    return new GadgetResponseMainPage(
                            rs.getLong("id"),
                            id,
                            rs.getInt("percent"),
                            image,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("main_colour"),
                            rs.getDouble("rating"),
                            rs.getInt("countOfFeedback"),
                            rs.getBigDecimal("price"),
                            price,
                            likes,
                            comparison,
                            basket
                    );
                });

        return GadgetPaginationForMain.builder()
                .page(page)
                .size(size)
                .mainPages(newGadgets)
                .build();
    }

    @Override
    public GadgetPaginationForMain mainPageRecommend(int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        List<GadgetResponseMainPage> newGadgets = jdbcTemplate.query("""
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
                               sg.id as subGadgetId
                        from gadgets g
                        join (
                        select distinct on (sg.gadget_id) sg.id, sg.gadget_id, sg.quantity, sg.main_colour, sg.memory, sg.price
                        from sub_gadgets sg
                        where sg.remoteness_status ="""+"'"+status+"'"+"""
                        order by sg.gadget_id, sg.id
                        ) sg on g.id = sg.gadget_id
                        left join sub_gadget_images gi on sg.id = gi.sub_gadget_id
                        join brands b on g.brand_id = b.id
                        left outer join discounts d on g.id = d.gadget_id
                        left outer join feedbacks f on f.gadget_id = g.id
                        group by g.id, g.name_of_gadget, b.brand_name, sg.quantity, d.percent,
                                sg.price, sg.memory, g.rating, sg.main_colour, subGadgetId
                        having g.rating > 3.9 or count(f.id) > 10
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String image = imagesArray.length > 0 ? imagesArray[0] : null;
                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }
                    Long subGadgetId = rs.getLong("subGadgetId");
                    SubGadget subGadget = subGadgetRepo.getByID(subGadgetId);
                    BigDecimal price = GadgetServiceImpl.calculatePrice(subGadget);
                    boolean likes = checkLikes(subGadget, user);
                    boolean comparison = checkComparison(subGadget, user);
                    boolean basket = checkBasket(subGadget, user);

                    return new GadgetResponseMainPage(
                            rs.getLong("id"),
                            subGadgetId,
                            rs.getInt("percent"),
                            image,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("main_colour"),
                            rs.getDouble("rating"),
                            rs.getInt("countOfFeedback"),
                            rs.getBigDecimal("price"),
                            price,
                            likes,
                            comparison,
                            basket
                    );
                });

        return GadgetPaginationForMain.builder()
                .page(page)
                .size(size)
                .mainPages(newGadgets)
                .build();
    }
}
