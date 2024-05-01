package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.response.GadgetsResponse;
import gadgetarium.dto.response.PaginationGadget;
import gadgetarium.dto.response.PaginationSHowMoreGadget;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Memory;
import gadgetarium.enums.Ram;
import gadgetarium.enums.Sort;
import gadgetarium.repositories.CategoryRepository;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GadgetJDBCTemplateRepositoryImpl implements GadgetJDBCTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ResultPaginationGadget getAll(Sort sort, Discount discount, int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;
        String orderBy = "";
        String where = "";

        if (sort != null) {
            if (sort.equals(Sort.NEW_PRODUCTS)) orderBy = " order by ga.release_date desc ";
            else if (sort.equals(Sort.PROMOTION)) {
                if (discount != null) {
                    if (discount.equals(Discount.ALL_DISCOUNTS)) where = " where d.percent is not null ";
                    else if (discount.equals(Discount.UP_TO_50)) where = " where d.percent < 50 ";
                    else if (discount.equals(Discount.OVER_50)) where = " where d.percent > 50 ";
                }
            } else if (sort.equals(Sort.RECOMMENDED)) where = " where g.rating > 4 ";
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
                               d.end_date,
                               g.price
                        from sub_gadgets g
                        join sub_gadget_images gi on g.id = gi.sub_gadget_id
                        join gadgets ga on ga.id = g.gadget_id
                        join brands b on ga.brand_id = b.id
                        left outer join discounts d on g.id = d.sub_gadget_id
                        """ + where + """
                        group by g.id, ga.article, g.name_of_gadget, ga.release_date,
                                   b.brand_name,  g.quantity, d.percent, g.price, d.end_date
                        """ + orderBy + """
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    BigDecimal price = rs.getBigDecimal("price");
                    int percent = rs.getInt("percent");
                    BigDecimal currentPrice = price;

                    Date endDate = rs.getDate("end_date");
                    if (endDate != null && endDate.after(new Date())) {
                        currentPrice = price.subtract(price.multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100))));
                    }

                    return new PaginationGadget(
                            rs.getLong("id"),
                            Arrays.asList((String[]) rs.getArray("images").getArray()),
                            rs.getLong("article"),
                            rs.getString("nameOfGadget"),
                            rs.getDate("release_date").toLocalDate(),
                            rs.getInt("quantity"),
                            price,
                            percent,
                            currentPrice
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
    public PaginationSHowMoreGadget allGadgetsForEvery(Sort sort, Discount discount, Memory memory, Ram ram, BigDecimal costFrom, BigDecimal costUpTo, String colour, String brand, int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;

        String orderBy = "";
        String where = "";

        if (brand != null || sort != null || discount != null || memory != null || ram != null || costFrom != null || costUpTo != null || colour != null) {
            boolean needAnd = false;

            if (brand != null) {
                where += " where b.brand_name ilike '"+brand+"'";
                needAnd = true;
            }
            if (costFrom != null && costUpTo != null){
                if (needAnd){
                    where += " and ";
                } else {
                    where += " where ";
                }
                where += " g.price between '"+costFrom+"' and '"+costUpTo+"'";
                needAnd = true;
            } else if (costFrom != null){
                if (needAnd){
                    where += " and ";
                } else {
                    where += " where ";
                }
                where += " g.price > '"+costFrom+"'";
            } else if (costUpTo != null) {
                if (needAnd){
                    where += " and ";
                } else {
                    where += " where ";
                }
                where += " g.price < '"+costUpTo+"'";
            }
            if (colour != null){
                if (needAnd) {
                    where += " and ";
                } else {
                    where += " where ";
                }
                where += " g.main_colour ilike '" + colour + "'";
                needAnd = true;
            }
            if (memory != null){
                if (needAnd){
                    where += " and ";
                } else {
                    where += " where ";
                }
                where += " ga.memory ilike '" + memory.name() + "'";
                needAnd = true;
            }
            if (ram != null){
                if (needAnd){
                    where += " and ";
                } else {
                    where += " where ";
                }
                where += " ga.ram ilike '" + ram.name() + "'";
                needAnd = true;
            }
            if (sort != null){
                if (sort.equals(Sort.RECOMMENDED)){
                    if (needAnd){
                        where += " and ";
                    } else {
                        where += " where ";
                    }
                    where += " g.rating > 3.9 or (select count(*) from orders o where o.id = og.orders_id) > 10 ";
                } else if (sort.equals(Sort.NEW_PRODUCTS)) {
                    orderBy = " order by ga.release_date desc ";
                } else if (sort.equals(Sort.PROMOTION)) {
                    if (discount != null){
                        if (discount.equals(Discount.ALL_DISCOUNTS)){
                            if (needAnd){
                                where += " and ";
                            } else {
                                where += " where ";
                            }
                            where += " d.percent is not null ";
                        } else if (discount.equals(Discount.UP_TO_50)) {
                            if (needAnd){
                                where += " and ";
                            } else {
                                where += " where ";
                            }
                            where += " d.percent < 50 ";
                        } else if (discount.equals(Discount.OVER_50)) {
                            if (needAnd){
                                where += " and ";
                            } else {
                                where += " where ";
                            }
                            where += " d.percent > 50 ";
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
                        left outer join discounts d on g.id = d.sub_gadget_id
                        left join orders_gadgets og on ga.id = og.gadgets_id
                        left join orders o on o.id = og.orders_id
                        left outer join feedbacks f on ga.id = f.gadget_id
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


                    return new GadgetsResponse(
                            rs.getLong("id"),
                            imagesFirst,
                            rs.getInt("quantity"),
                            rs.getString("nameOfGadget"),
                            rs.getString("memory"),
                            rs.getString("main_colour"),
                            rs.getInt("rating"),
                            rs.getInt("countOf"),
                            rs.getBigDecimal("price"),
                            rs.getBigDecimal("currentPrice"),
                            rs.getInt("percent")
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
}
