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

        String where = " where o.status is not null ";

        if (keyword != null && !keyword.isEmpty()) {
            where += " and (cast(o.number as text) like '%" + keyword + "%' " +
                     " or u.last_name ilike '%" + keyword + "%' " +
                     " or u.first_name ilike '%" + keyword + "%') ";
        }

        if (status != null) {
            if (status.equals(Status.DELIVERED) || status.equals(Status.RECEIVED)){
                where += " and o.status in ('"+Status.DELIVERED.name()+"', '"+Status.RECEIVED.name()+"')";
            }else where += " and o.status = '"+status.name()+"'";
        }

        if (startDate != null && endDate != null){
            where += " and o.created_at between '"+startDate+"' and '"+endDate+"' ";
        } else if (startDate != null){
            where += " and o.created_at >= '"+startDate+"'";
        } else if (endDate != null) {
            where += " and o.created_at <= '"+endDate+"'";
        }

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
                           """ + where + """
                           group by o.id, o.number, o.created_at, o.type_order, o.status,
                           o.total_price, u.last_name, u.first_name
                           limit ? offset ?
                           """,
                new Object[]{limit, offset},
                (rs, rowNum) -> {
                    return new OrderResponse(
                            rs.getLong("id"),
                            rs.getString("fullName"),
                            rs.getLong("number"),
                            String.valueOf(rs.getDate("created_at")),
                            rs.getInt("totalGadgets"),
                            rs.getBigDecimal("total_price"),
                            rs.getBoolean("type_order"),
                            rs.getString("status")
                    );
                });
        return OrderPagination.builder()
                .searchWord(keyword)
                .status(status)
                .waiting(getOrderInWaitingCount())
                .progress(getOrderInProgressCount())
                .onTheWay(getOrderInOnTheWayCount())
                .delivered(getOrderInDeliveredCount())
                .canceled(getOrderInCanceledCount())
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .orderResponses(orderResponses)
                .build();
    }

    private int getOrderInWaitingCount(){
        String sql = "select count(*) from orders o " +
                     " where o.status = ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(Status.PENDING)}, Integer.class);
        return count != null ? count : 0;
    }

    private int getOrderInProgressCount(){
        String sql = "select count(*) from orders o " +
                     " where o.status = ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(Status.READY)}, Integer.class);
        return count != null ? count : 0;
    }

    private int getOrderInOnTheWayCount(){
        String sql = "select count(*) from orders o " +
                     " where o.status = ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(Status.COURIER_ON_THE_WAY)}, Integer.class);
        return count != null ? count : 0;
    }

    private int getOrderInDeliveredCount(){
        String sql = "select count(*) from orders o " +
                     " where o.status in (?, ?)";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(Status.DELIVERED), String.valueOf(Status.RECEIVED)}, Integer.class);
        return count != null ? count : 0;
    }

    private int getOrderInCanceledCount(){
        String sql = "select count(*) from orders o " +
                     " where o.status = ?";

        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{String.valueOf(Status.CANCELLED)}, Integer.class);
        return count != null ? count : 0;
    }
}
