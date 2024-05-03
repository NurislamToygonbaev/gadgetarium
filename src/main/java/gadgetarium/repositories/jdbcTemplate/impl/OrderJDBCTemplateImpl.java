package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.response.OrderPagination;
import gadgetarium.dto.response.OrderResponse;
import gadgetarium.dto.response.OrderResponseFindById;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.jdbcTemplate.OrderJDBCTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderJDBCTemplateImpl implements OrderJDBCTemplate {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public OrderPagination getAllOrders(Status status, String keyword, LocalDate startDate, LocalDate endDate, int page, int size) {
        int offset = (page - 1) * size;
        int limit = size;

        if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
            throw new BadRequestException("End day must be after the start day!");
        }

        if (startDate != null && startDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("The start day should start before today!");
        }
        List<Object> params = new ArrayList<>();
        String whereClause = "";
        if (status != null || startDate != null || endDate != null || keyword != null) {
            whereClause = " where ";
            boolean needAnd = false;

            if (keyword != null) {
                whereClause += """
                        (u.last_name ilike concat('%', ?, '%')
                        or u.first_name ilike concat('%', ?, '%')
                        or o.number::text ilike concat('%', ?, '%'))
                        """;
                params.add(keyword.trim());
                params.add(keyword.trim());
                params.add(keyword.trim());
                needAnd = true;
            }

            if (status != null) {
                if (needAnd) {
                    whereClause += " and ";
                }
                whereClause += " o.status = ? ";
                needAnd = true;
                params.add(status.name());
            }

            if (startDate != null && endDate != null) {
                if (needAnd) {
                    whereClause += " and ";
                }
                whereClause += " o.created_at between ? and ? ";
                params.add(startDate);
                params.add(endDate);
            } else if (startDate != null) {
                if (needAnd) {
                    whereClause += " and ";
                }
                whereClause += " o.created_at >= ? ";
                params.add(startDate);
            } else if (endDate != null) {
                if (needAnd) {
                    whereClause += " and ";
                }
                whereClause += " o.created_at <= ? ";
                params.add(endDate);
            }
        }


        params.add(limit);
        params.add(offset);

        List<OrderResponse> orderResponses = jdbcTemplate.query("""
                        select o.id,
                               concat(u.last_name, ' ', u.first_name) as fullName,
                               o.number,
                               o.created_at,
                               count(g.id) as totalGadgets,
                               sum(s.current_price) as totalPrice,
                               o.type_order,
                               o.status
                        from orders o
                        join users u on o.user_id = u.id
                        join orders_gadgets og on o.id = og.orders_id
                        join gadgets g on og.gadgets_id = g.id
                        join sub_gadgets s on s.gadget_id = g.id
                        """ + whereClause + """
                        group by o.id, fullName, o.number, o.created_at, o.type_order, o.status
                        limit ? offset ?
                        """,
                params.toArray(),
                (rs, rowNum) -> {
                    return new OrderResponse(
                            rs.getLong("id"),
                            rs.getString("fullName"),
                            rs.getLong("number"),
                            rs.getDate("created_at").toLocalDate(),
                            rs.getInt("totalGadgets"),
                            rs.getBigDecimal("totalPrice"),
                            rs.getBoolean("type_order"),
                            rs.getString("status")
                    );
                });
        return OrderPagination.builder()
                .searchWord(keyword)
                .status(status)
                .quantity(orderResponses.size())
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .orderResponses(orderResponses)
                .build();
    }

    @Override
    public OrderResponseFindById findOrderById(Long orderId) {
        return jdbcTemplate.queryForObject("""
                    select o.id,
                           concat(u.first_name, ' ', u.last_name) as fullName,
                           o.number,
                           concat(b.brand_name, ' ', s.name_of_gadget) as gadgetName,
                           g.memory,
                           s.main_colour,
                           count(g.id) as countOfGadget,
                           sum(s.price) as price,
                           d.percent
                    from orders o
                    join users u on u.id = o.user_id
                    join orders_gadgets og on o.id = og.orders_id
                    join gadgets g on g.id = og.gadgets_id
                    join brands b on b.id = g.brand_id
                    join sub_gadgets s on g.id = s.gadget_id
                    left outer join discounts d on s.id = d.sub_gadget_id
                    where o.id = ?
                    group by o.id, concat(u.first_name, ' ', u.last_name),
                    o.number, concat(b.brand_name, ' ', s.name_of_gadget),
                    g.memory, s.main_colour, d.percent
                    """,
                new Object[]{orderId},
                (rs, rowNum) -> {
                    BigDecimal price = rs.getBigDecimal("price");
                    int percent = rs.getInt("percent");

                    BigDecimal discount = price.multiply(BigDecimal.valueOf(percent).divide(BigDecimal.valueOf(100)));
                    BigDecimal discountedPrice = price.subtract(discount);

                    return new OrderResponseFindById(
                            rs.getLong("id"),
                            rs.getString("fullName"),
                            rs.getLong("number"),
                            rs.getString("gadgetName"),
                            rs.getString("memory"),
                            rs.getString("main_colour"),
                            rs.getInt("countOfGadget"),
                            price,
                            percent,
                            discount,
                            discountedPrice
                    );
                }
        );
    }

}
