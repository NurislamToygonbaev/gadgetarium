package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.request.PaginationRequest;
import gadgetarium.dto.response.PaginationGadget;
import gadgetarium.dto.response.ResultPaginationGadget;
import gadgetarium.enums.Discount;
import gadgetarium.enums.Sort;
import gadgetarium.repositories.jdbcTemplate.GadgetJDBCTemplateRepository;
import gadgetarium.services.impl.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GadgetJDBCTemplateRepositoryImpl implements GadgetJDBCTemplateRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUser currentUser;

    @Override
    public ResultPaginationGadget getAll(PaginationRequest request) {
        int offset = (request.page() - 1) * request.size();
        int limit = request.size();
        String sort = "";
        String discount = "";
        if (request.sort().equals(Sort.NEW_PRODUCTS)) sort = "order by d.release_date desc";
        if (request.sort().equals(Sort.PROMOTION)) {
            if (request.discount().equals(Discount.ALL_DISCOUNTS)) discount = "and g.discount is not null";
            if (request.discount().equals(Discount.UP_TO_50)) discount = "and d.percent < 50";
            if (request.discount().equals(Discount.OVER_50)) discount = "and d.percent > 50";
        }
        if (request.sort().equals(Sort.RECOMMENDED)) sort = "and g.rating > 4";
        if (request.sort().equals(Sort.HIGH_TO_LOW)) sort = "order by g.price desc";
        if (request.sort().equals(Sort.LOW_TO_HIGH)) sort = "order by g.price asc";

        List<PaginationGadget> list = jdbcTemplate.query("""
                        select g.id,
                               array_agg(gi.images) as images,
                               g.article,
                               concat(b.brand_name, g.name_of_gadget) as nameOfGadget
                               g.release_date,
                               g.quantity,
                               d.percent,
                               g.price
                        from sub_gadgets g
                        join sub_gadget_images gi on g.id = gi.sub_gadget_id
                        join discounts d on g.id = d.sub_gadget_id
                        join gadgets ga on ga.id = g.gadget_id
                        join brands b on ga.brand_id = b.id
                        where
                        """ + sort + """
                        """ + discount + """
                        group by g.id, g.article, g.name_of_gadget, g.release_date,
                        g.quantity, d.percent, g.price
                        limit ? offset ?
                        """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    BigDecimal price = rs.getBigDecimal(7);
                    int percent = rs.getInt(8);
                    BigDecimal currentPrice = price.subtract(price.multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100))));

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
                .sort(request.sort())
                .discount(request.discount())
                .page(request.page())
                .size(request.size())
                .paginationGadgets(list)
                .build();
    }
}
