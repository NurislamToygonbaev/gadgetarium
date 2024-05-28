package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.response.OrderPagination;
import gadgetarium.dto.response.OrderResponse;
import gadgetarium.dto.response.OrderResponseFindById;
import gadgetarium.enums.RemotenessStatus;
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
                           count(s.id) as totalGadgets,
                           o.total_price,
                           o.type_order,
                           o.status
                    from orders o
                    join users u on o.user_id = u.id
                    left join orders_sub_gadgets og on o.id = og.orders_id
                    left join sub_gadgets s on s.id = og.sub_gadgets_id
                    left join gadgets g on s.gadget_id = g.id
                    """ + whereClause + """
                    group by o.id, o.number, o.created_at, o.type_order, o.status,
                         o.total_price, u.last_name, u.first_name
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
                            rs.getBigDecimal("total_price"),
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

}
