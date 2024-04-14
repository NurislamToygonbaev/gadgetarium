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

    @Override
    public ResultPaginationGadget getAll(PaginationRequest request) {
        int offset = (request.page() - 1) * request.size();
        int limit = request.size();
        String sort = "";
        String discount = "";
        String orderByClause = "";
        String whereClause = "";

        if (!request.sort().isEmpty()) {
            if (request.sort().equals(Sort.NEW_PRODUCTS.name())) {
                sort = " ga.release_date desc ";
                orderByClause = " order by " + sort;
            } else if (request.sort().equals(Sort.PROMOTION.name())) {
                if (!request.discount().isEmpty()) {
                    if (request.discount().equalsIgnoreCase(Discount.ALL_DISCOUNTS.name())){
                        discount = " d.percent is not null ";
                        whereClause = " where " + discount;
                    }
                    else if (request.discount().equalsIgnoreCase(Discount.UP_TO_50.name())){
                        discount = " d.percent < 50 ";
                        whereClause = " where " + discount;
                    }
                    else if (request.discount().equalsIgnoreCase(Discount.OVER_50.name())) {
                        discount = " d.percent > 50 ";
                        whereClause = " where " + discount;
                    }
                }
            } else if (request.sort().equalsIgnoreCase(Sort.RECOMMENDED.name())) {
                sort = " g.rating > 4 ";
                whereClause = " where " + sort;
            } else if (request.sort().equalsIgnoreCase(Sort.HIGH_TO_LOW.name())) {
                sort = " g.price desc ";
                orderByClause = " order by " + sort;
            } else if (request.sort().equalsIgnoreCase(Sort.LOW_TO_HIGH.name())) {
                sort = " g.price asc ";
                orderByClause = " order by " + sort;
            }
        }

        List<PaginationGadget> list = jdbcTemplate.query("""
            select g.id,
                   array_agg(gi.images) as images,
                   ga.article,
                   concat(b.brand_name, ' ', g.name_of_gadget) as nameOfGadget,
                   ga.release_date,
                   g.quantity,
                   d.percent,
                   g.price
            from sub_gadgets g
            join sub_gadget_images gi on g.id = gi.sub_gadget_id
            join discounts d on g.id = d.sub_gadget_id
            join gadgets ga on ga.id = g.gadget_id
            join brands b on ga.brand_id = b.id
            """ + whereClause + """
            group by g.id, ga.article, g.name_of_gadget, ga.release_date,
                       b.brand_name,  g.quantity, d.percent, g.price
            """ + orderByClause + """
            limit ? offset ?
            """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    BigDecimal price = rs.getBigDecimal("price");
                    int percent = rs.getInt("percent");
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
