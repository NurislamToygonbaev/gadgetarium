package gadgetarium.repositories.jdbcTemplate.impl;

import gadgetarium.dto.response.CompareResponses;
import gadgetarium.dto.response.OrderPagination;
import gadgetarium.dto.response.OrderResponse;
import gadgetarium.dto.response.OrderResponseFindById;
import gadgetarium.entities.SubGadget;
import gadgetarium.entities.User;
import gadgetarium.enums.GadgetType;
import gadgetarium.enums.RemotenessStatus;
import gadgetarium.enums.Status;
import gadgetarium.exceptions.BadRequestException;
import gadgetarium.repositories.SubGadgetRepository;
import gadgetarium.repositories.jdbcTemplate.OrderJDBCTemplate;
import gadgetarium.services.impl.CurrentUser;
import gadgetarium.services.impl.GadgetServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderJDBCTemplateImpl implements OrderJDBCTemplate {

    private final JdbcTemplate jdbcTemplate;
    private final CurrentUser currentUser;
    private final SubGadgetRepository subGadgetRepo;

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

    @Override
    public List<CompareResponses> comparing(GadgetType gadgetType, boolean isDifferences) {
        User user = currentUser.get();
        String status = String.valueOf(RemotenessStatus.NOT_REMOTE);

        List<CompareResponses> compareResponsesList = jdbcTemplate.query(
                "select " +
                "sg.id, " +
                "g.id as gadgetId, " +
                "array_agg(gi.images) as images, " +
                "g.name_of_gadget as nameofgadget, " +
                "sg.memory, " +
                "sg.main_colour, " +
                "d.percent, " +
                "sg.price, " +
                "b.brand_name, " +
                "sg.ram, " +
                "sg.count_sim, " +
                "g.warranty " +
                "from sub_gadgets sg " +
                "join gadgets g on g.id = sg.gadget_id " +
                "left join sub_gadget_images gi on sg.id = gi.sub_gadget_id " +
                "join brands b on g.brand_id = b.id " +
                "join sub_categories sc on g.sub_category_id = sc.id " +
                "join categories c on sc.category_id = c.id " +
                "left join discounts d on g.id = d.gadget_id " +
                "right join users_comparison uc on sg.id = uc.comparison_id " +
                "join users u on u.id = uc.user_id " +
                "where u.id = ? and sg.remoteness_status = ? and lower(c.category_name) = ? " +
                "group by " +
                "g.id, g.name_of_gadget, b.brand_name, sg.ram, d.percent, sg.price, g.warranty, " +
                "sg.memory, sg.main_colour, sg.id, sg.count_sim, g.name_of_gadget",
                new Object[]{user.getId(), status, gadgetType.name().toLowerCase()},
                (rs, rowNum) -> {

                    String[] imagesArray = (String[]) rs.getArray("images").getArray();
                    String imagesFirst = imagesArray.length > 0 ? imagesArray[0] : null;

                    int percent = rs.getInt("percent");
                    BigDecimal price = BigDecimal.ZERO;

                    Long id = rs.getLong("id");
                    SubGadget subGadget = subGadgetRepo.getByID(id);
                    if (percent != 0) {
                        price = price.add(GadgetServiceImpl.calculatePrice(subGadget));
                    } else {
                        price = price.add(subGadget.getPrice());
                    }

                    String nameOfGadget = rs.getString("nameofgadget");
                    String brand = rs.getString("brand_name");
                    String memory = rs.getString("memory");
                    String ram = rs.getString("ram");
                    String color = rs.getString("main_colour");
                    String warranty = rs.getString("warranty");

                    return new CompareResponses(
                            rs.getLong("gadgetId"),
                            id,
                            imagesFirst,
                            brand + " " + nameOfGadget,
                            memory,
                            color,
                            price,
                            nameOfGadget,
                            color,
                            brand,
                            memory,
                            ram,
                            GadgetJDBCTemplateRepositoryImpl.checkBasket(subGadget, user),
                            rs.getInt("count_sim"),
                            warranty,
                            phoneCount(),
                            laptopCount(),
                            watchCount()
                    );
                });

        if (isDifferences) {
            for (int i = 0; i < compareResponsesList.size(); i++) {
                CompareResponses current = compareResponsesList.get(i);

                for (int j = 0; j < compareResponsesList.size(); j++) {
                    if (i != j) {
                        CompareResponses other = compareResponsesList.get(j);

                        if (current.getNameOfGadgetCompare() != null && current.getNameOfGadgetCompare().equalsIgnoreCase(other.getNameOfGadgetCompare())) {
                            current.setNameOfGadgetCompare(null);
                        }
                        if (current.getColorCompare() != null && current.getColorCompare().equalsIgnoreCase(other.getColorCompare())) {
                            current.setColorCompare(null);
                        }
                        if (current.getBrandCompare() != null && current.getBrandCompare().equalsIgnoreCase(other.getBrandCompare())) {
                            current.setBrandCompare(null);
                        }
                        if (current.getMemoryCompare() != null && current.getMemoryCompare().equalsIgnoreCase(other.getMemoryCompare())) {
                            current.setMemoryCompare(null);
                        }
                        if (current.getRamCompare() != null && current.getRamCompare().equalsIgnoreCase(other.getRamCompare())) {
                            current.setRamCompare(null);
                        }
                        if (current.getSimCompare() == other.getSimCompare()) {
                            current.setSimCompare(0);
                        }
                        if (current.getWarrantyCompare() != null && current.getWarrantyCompare().equalsIgnoreCase(other.getWarrantyCompare())) {
                            current.setWarrantyCompare(null);
                        }
                    }
                }
            }
        }
        compareResponsesList.sort(Comparator.comparingLong(CompareResponses::getSubGadgetId));
        return compareResponsesList;
    }

    private int phoneCount() {
        User user = currentUser.get();
        String sql = "select count(*) from sub_gadgets sg " +
                     "join gadgets g on sg.gadget_id = g.id " +
                     "join sub_categories sc on g.sub_category_id = sc.id " +
                     "right join users_comparison uc on sg.id = uc.comparison_id " +
                     "join users u on u.id = uc.user_id " +
                     "join categories c on sc.category_id = c.id " +
                     "where lower(c.category_name) = 'phone' and " +
                     "sg.remoteness_status = ? and u.id = ?";

        Integer count = jdbcTemplate.queryForObject(
                sql,
                new Object[]{String.valueOf(RemotenessStatus.NOT_REMOTE), user.getId()},
                Integer.class
        );

        return count != null ? count : 0;
    }

    private int laptopCount() {
        User user = currentUser.get();
        String sql = "select count(*) from sub_gadgets sg " +
                     "join gadgets g on sg.gadget_id = g.id " +
                     "join sub_categories sc on g.sub_category_id = sc.id " +
                     "right join users_comparison uc on sg.id = uc.comparison_id " +
                     "join users u on u.id = uc.user_id " +
                     "join categories c on sc.category_id = c.id " +
                     "where lower(c.category_name) = 'laptop' and " +
                     "sg.remoteness_status = ? and u.id = ?";

        Integer count = jdbcTemplate.queryForObject(
                sql,
                new Object[]{String.valueOf(RemotenessStatus.NOT_REMOTE), user.getId()},
                Integer.class
        );

        return count != null ? count : 0;
    }

    private int watchCount() {
        User user = currentUser.get();
        String sql = "select count(*) from sub_gadgets sg " +
                     "join gadgets g on sg.gadget_id = g.id " +
                     "join sub_categories sc on g.sub_category_id = sc.id " +
                     "right join users_comparison uc on sg.id = uc.comparison_id " +
                     "join users u on u.id = uc.user_id " +
                     "join categories c on sc.category_id = c.id " +
                     "where lower(c.category_name) = 'watch' and " +
                     "sg.remoteness_status = ? and u.id = ?";

        Integer count = jdbcTemplate.queryForObject(
                sql,
                new Object[]{String.valueOf(RemotenessStatus.NOT_REMOTE), user.getId()},
                Integer.class
        );

        return count != null ? count : 0;
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
