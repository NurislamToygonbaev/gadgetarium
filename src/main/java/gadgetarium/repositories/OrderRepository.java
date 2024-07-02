package gadgetarium.repositories;

import gadgetarium.dto.response.AllOrderHistoryResponse;
import gadgetarium.dto.response.OrderIdsResponse;
import gadgetarium.entities.Order;
import gadgetarium.entities.SubGadget;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   "inner join gadgets g on s.gadget_id = g.id " +
                   "inner join orders_sub_gadgets og on og.sub_gadgets_id = g.id " +
                   "join orders o on o.id = og.orders_id " +
                   "where o.status in ('DELIVERED', 'RECEIVED') " +
                   " and o.created_at = current_date ",
            nativeQuery = true)
    BigDecimal getBuyPrice();


    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   "inner join gadgets g on s.gadget_id = g.id " +
                   "inner join orders_sub_gadgets og on og.sub_gadgets_id = s.id " +
                   "join orders o on o.id = og.orders_id " +
                   "where o.status not in ('DELIVERED', 'RECEIVED') " +
                   " and o.created_at = current_date ",
            nativeQuery = true)
    BigDecimal getOrderPrice();

    @Query(value = "select count(o.id) from orders o " +
                   "where upper(o.status) in ('DELIVERED', 'RECEIVED') " +
                   " and extract(day from o.created_at) = extract(day from current_date);",
            nativeQuery = true)
    int getBuyCount();


    @Query(value = "select count(o.id) from orders o " +
                   "where upper(o.status) not in ('DELIVERED', 'RECEIVED') " +
                   " and extract(day from o.created_at) = extract(day from current_date);",
            nativeQuery = true)
    Integer getOrderCount();



    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   "inner join gadgets g on s.gadget_id = g.id " +
                   "inner join orders_sub_gadgets og on og.sub_gadgets_id = s.id " +
                   "inner join orders o on o.id = og.orders_id " +
                   "where upper(o.status) in ('DELIVERED', 'RECEIVED') " +
                   " and extract(day from o.created_at) = extract(day from current_date);",
            nativeQuery = true)
    BigDecimal forCurrentDay();


    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   "inner join gadgets g on s.gadget_id = g.id " +
                   "inner join orders_sub_gadgets og on og.sub_gadgets_id = s.id " +
                   "inner join orders o on o.id = og.orders_id " +
                   "where upper(o.status) in ('DELIVERED', 'RECEIVED') " +
                   " and extract(day from o.created_at) = extract(day from (current_date - interval '1 day'));",
            nativeQuery = true)
    BigDecimal forPreviousDay();




    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   " inner join gadgets g on s.gadget_id = g.id " +
                   " inner join orders_sub_gadgets og on og.sub_gadgets_id = s.id " +
                   " inner join orders o on o.id = og.orders_id " +
                   "where upper(o.status) in ('DELIVERED', 'RECEIVED') " +
                   " and extract(month from o.created_at) = extract(month from current_date);", nativeQuery = true)
    BigDecimal forCurrentMonth();


    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   " inner join gadgets g on s.gadget_id = g.id " +
                   " inner join orders_sub_gadgets og on og.sub_gadgets_id = s.id " +
                   " inner join orders o on o.id = og.orders_id " +
                   "where upper(o.status) in ('DELIVERED', 'RECEIVED') " +
                   " and extract(month from o.created_at) = extract(month from (current_date - interval '1 month'));", nativeQuery = true)
    BigDecimal forPreviousMonth();



    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   " inner join gadgets g on s.gadget_id = g.id " +
                   " inner join orders_sub_gadgets og on og.sub_gadgets_id = s.id " +
                   " inner join orders o on o.id = og.orders_id " +
                   "where upper(o.status) in ('DELIVERED', 'RECEIVED') " +
                   " and extract(year from o.created_at) = extract(year from current_date);", nativeQuery = true)
    BigDecimal forCurrentYear();

    @Query(value = "select sum(o.total_price) from sub_gadgets s " +
                   " inner join gadgets g on s.gadget_id = g.id " +
                   " inner join orders_sub_gadgets og on og.sub_gadgets_id = s.id " +
                   " inner join orders o on o.id = og.orders_id " +
                   "where upper(o.status) in ('DELIVERED', 'RECEIVED') " +
                   " and date_trunc('year', o.created_at) = date_trunc('year', (current_date - interval '1 year'));", nativeQuery = true)
    BigDecimal forPreviousYear();


    default Order getOrderById(Long orderId) {
        return findById(orderId).orElseThrow(() ->
                new NotFoundException("Order with id: " + orderId + " not found"));
    }

    List<Order> findBySubGadgetsContains(SubGadget subGadget);



    @Query(value = "select g.name_of_gadget as nameOfGadget, " +
                   "s.memory as memory, " +
                   "s.main_colour as color, " +
                   "d.percent as percent, " +
                   "sum(s.price) as price, " +
                   "count(s.id) as countOfGadgets " +
                   "from orders o " +
                   "join orders_sub_gadgets osg on o.id = osg.orders_id " +
                   "join sub_gadgets s on s.id = osg.sub_gadgets_id " +
                   "join gadgets g on g.id = s.gadget_id " +
                   "left join discounts d on d.gadget_id = g.id " +
                   "where o.id = ?1 " +
                   " group by g.name_of_gadget, s.memory, s.main_colour, d.percent "
            , nativeQuery = true)
    List<Object[]> getGadgetsFields(Long id);


    @Query("select max(o.id) from Order o where o.status is null and o.user.id = :userId")
    Long findLastByStatusIsNullAndUserId(Long userId);

}