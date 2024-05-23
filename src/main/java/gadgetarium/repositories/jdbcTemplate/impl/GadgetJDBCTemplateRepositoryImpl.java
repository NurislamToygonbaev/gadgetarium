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
                        select g.id,
                               array_agg(gi.images) as images,
                               ga.article,
                               concat(b.brand_name, ' ', g.name_of_gadget) as nameOfGadget,
                               ga.release_date,
                               g.quantity,
                               d.percent,
                               g.current_price,
                               g.price
                        from sub_gadgets g
                        left outer join sub_gadget_images gi on g.id = gi.sub_gadget_id
                        join gadgets ga on ga.id = g.gadget_id
                        join brands b on ga.brand_id = b.id
                        left outer join discounts d on g.id = d.sub_gadget_id
                        where ga.remoteness_status ="""+"'"+status+"'"+"""
                        """ + where + """
                        group by g.id, ga.article, g.name_of_gadget, ga.release_date,
                                   b.brand_name,  g.quantity, d.percent, g.price, d.end_date
                        """ + orderBy + """
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {

                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String imagesFirst = imagesArray.length > 0 ? imagesArray[0] : null;


                    return new PaginationGadget(
                            rs.getLong("id"),
                            imagesFirst,
                            rs.getLong("article"),
                            rs.getString("nameOfGadget"),
                            rs.getDate("release_date").toLocalDate(),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price"),
                            rs.getInt("percent"),
                            rs.getBigDecimal("current_price")
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
                where += " and b.brand_name ilike ANY (array" + brand.toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (costFrom != null && costUpTo != null){
                where += " and g.current_price between '"+costFrom+"' and '"+costUpTo+"'";
            } else if (costFrom != null){
                where += " and g.current_price > '"+costFrom+"'";
            } else if (costUpTo != null) {
                where += " and g.current_price < '"+costUpTo+"'";
            }
            if (colour != null && !colour.isEmpty()){
                where += " and g.main_colour ilike ANY (array" + colour.toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (memory != null && !memory.isEmpty()){
                where += " and ga.memory ilike ANY (array" + memory.stream().map(Memory::name).toList().toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (ram != null && !ram.isEmpty()){
                where += " and ga.ram ilike ANY (array" + ram.stream().map(Ram::name).toList().toString().replace("[", "['").replace("]", "']").replace(", ", "','") + ")";
            }
            if (sort != null){
                if (sort.equals(Sort.RECOMMENDED)){
                    where += " and g.rating > 3.9 or (select count(*) from orders o where o.id = og.orders_id) > 10 ";
                } else if (sort.equals(Sort.NEW_PRODUCTS)) {
                    orderBy = " order by ga.release_date desc ";
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

        List<GadgetsResponse> gadgetsResponses = jdbcTemplate.query("""
                        select g.id,
                               array_agg(gi.images) as images,
                               concat(b.brand_name, ' ', g.name_of_gadget) as nameOfGadget,
                               ga.memory,
                               g.main_colour,
                               g.rating,
                               count(f.id) as countOf,
                               g.quantity,
                               d.percent,
                               g.current_price as currentPrice,
                               g.price,
                               count(o.id) as totalOrders,
                               ga.release_date
                        from sub_gadgets g
                        join sub_gadget_images gi on g.id = gi.sub_gadget_id
                        join gadgets ga on ga.id = g.gadget_id
                        join brands b on ga.brand_id = b.id
                        join sub_categories sc on ga.sub_category_id = sc.id
                        join categories c on sc.category_id = c.id
                        left outer join discounts d on g.id = d.sub_gadget_id
                        left join orders_gadgets og on ga.id = og.gadgets_id
                        left join orders o on o.id = og.orders_id
                        left outer join feedbacks f on ga.id = f.gadget_id
                        where c.id ="""+"'"+catId+"'"+ """ 
                        and  ga.remoteness_status ="""+"'"+status+"'"+ """ 
                        """ + where + """
                         group by g.id, g.name_of_gadget, b.brand_name,  g.quantity, d.percent, g.price,
                                   ga.memory, g.main_colour, g.current_price, g.rating, ga.release_date
                        """ + orderBy + """
                         limit ? offset ?
                        """,
                new Object[]{limit, offset},
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
                    return new GadgetsResponse(
                            id,
                            imagesFirst,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("main_colour"),
                            rs.getInt("rating"),
                            rs.getInt("countOf"),
                            rs.getBigDecimal("price"),
                            rs.getBigDecimal("currentPrice"),
                            rs.getInt("percent"),
                            checkLikes(subGadget, user),
                            checkComparison(subGadget, user),
                            checkBasket(subGadget, user)
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
                               g.quantity,
                               d.percent,
                               count(f.id) as countOfFeedback,
                               g.main_colour as colour,
                               g.rating,
                               ga.memory,
                               g.price,
                               g.current_price as currentPrice
                        from sub_gadgets g
                        join sub_gadget_images gi on g.id = gi.sub_gadget_id
                        join gadgets ga on ga.id = g.gadget_id
                        join brands b on ga.brand_id = b.id
                        left outer join discounts d on g.id = d.sub_gadget_id
                        left outer join feedbacks f on f.gadget_id = ga.id
                        where d.percent is not null and ga.remoteness_status ="""+"'"+status+"'"+"""
                        group by g.id, g.name_of_gadget, b.brand_name, g.quantity, d.percent,
                                g.price, currentPrice, ga.memory, g.rating, colour
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    String image = Arrays.asList((String[]) rs.getArray("images").getArray()).getFirst();

                    Long id = rs.getLong("id");
                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }
                    return new GadgetResponseMainPage(
                            id,
                            rs.getInt("percent"),
                            image,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("colour"),
                            rs.getDouble("rating"),
                            rs.getInt("countOfFeedback"),
                            rs.getBigDecimal("price"),
                            rs.getBigDecimal("currentPrice"),
                            checkLikes(subGadget, user),
                            checkComparison(subGadget, user),
                            checkBasket(subGadget, user)
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
                               g.quantity,
                               d.percent,
                               count(f.id) as countOfFeedback,
                               g.main_colour as colour,
                               g.rating,
                               ga.memory,
                               g.price,
                               g.current_price as currentPrice,
                               ga.release_date
                        from sub_gadgets g
                        join sub_gadget_images gi on g.id = gi.sub_gadget_id
                        join gadgets ga on ga.id = g.gadget_id
                        join brands b on ga.brand_id = b.id
                        left outer join discounts d on g.id = d.sub_gadget_id
                        left outer join feedbacks f on f.gadget_id = ga.id
                        where ga.remoteness_status ="""+"'"+status+"'"+"""
                        group by g.id, g.name_of_gadget, b.brand_name, g.quantity, d.percent,
                                g.price, currentPrice, ga.memory, g.rating, colour, ga.release_date
                        order by ga.release_date desc
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    String image = Arrays.asList((String[]) rs.getArray("images").getArray()).getFirst();

                    Long id = rs.getLong("id");
                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }
                    return new GadgetResponseMainPage(
                            id,
                            rs.getInt("percent"),
                            image,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("colour"),
                            rs.getDouble("rating"),
                            rs.getInt("countOfFeedback"),
                            rs.getBigDecimal("price"),
                            rs.getBigDecimal("currentPrice"),
                            checkLikes(subGadget, user),
                            checkComparison(subGadget, user),
                            checkBasket(subGadget, user)
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
                               g.quantity,
                               d.percent,
                               count(f.id) as countOfFeedback,
                               g.main_colour as colour,
                               g.rating,
                               ga.memory,
                               g.price,
                               g.current_price as currentPrice
                        from sub_gadgets g
                        join sub_gadget_images gi on g.id = gi.sub_gadget_id
                        join gadgets ga on ga.id = g.gadget_id
                        join brands b on ga.brand_id = b.id
                        left outer join discounts d on g.id = d.sub_gadget_id
                        left outer join feedbacks f on f.gadget_id = ga.id
                        where ga.remoteness_status ="""+"'"+status+"'"+"""
                        group by g.id, g.name_of_gadget, b.brand_name, g.quantity, d.percent,
                                g.price, currentPrice, ga.memory, g.rating, colour
                        having g.rating > 3.9 or count(f.id) > 10
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    String image = Arrays.asList((String[]) rs.getArray("images").getArray()).getFirst();

                    Long id = rs.getLong("id");
                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    User user = null;
                    try {
                        user = currentUser.get();
                    } catch (Exception ignored) {
                    }
                    return new GadgetResponseMainPage(
                            rs.getLong("id"),
                            rs.getInt("percent"),
                            image,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("colour"),
                            rs.getDouble("rating"),
                            rs.getInt("countOfFeedback"),
                            rs.getBigDecimal("price"),
                            rs.getBigDecimal("currentPrice"),
                            checkLikes(subGadget, user),
                            checkComparison(subGadget, user),
                            checkBasket(subGadget, user)
                    );
                });

        return GadgetPaginationForMain.builder()
                .page(page)
                .size(size)
                .mainPages(newGadgets)
                .build();
    }
}
