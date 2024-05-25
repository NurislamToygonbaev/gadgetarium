package gadgetarium.repositories;

import gadgetarium.dto.response.AllOrderHistoryResponse;
import gadgetarium.dto.response.OrderResponseFindById;
import gadgetarium.dto.response.PrivateGadgetResponse;
import gadgetarium.entities.Gadget;
import gadgetarium.entities.Order;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id join orders o on o.id = og.orders_id where o.status ilike 'DELIVERED' and o.created_at = current_date;", nativeQuery = true)
    BigDecimal getBuyPrice();

    @Query(value = "select count(o.id) from orders o where o.status ilike 'DELIVERED' and date(o.created_at) = current_date;", nativeQuery = true)
    int getBuyCount();

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id join orders o on o.id = og.orders_id where o.status not ilike 'DELIVERED' and o.status not ilike 'CANCELED' and o.status not ilike 'RECEIVED' and o.created_at = current_date;", nativeQuery = true)
    BigDecimal getOrderPrice();

    @Query("select count(o.id) from Order o where o.status != 'DELIVERED' and o.status != 'CANCELED' and o.status != 'RECEIVED' and o.createdAt = current_date")
    int getOrderCount();

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id inner join orders o on o.id = og.orders_id where o.status ilike 'DELIVERED' and o.created_at = current_date;", nativeQuery = true)
    BigDecimal forCurrentDay();

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id inner join orders o on o.id = og.orders_id where o.status ilike 'DELIVERED' and o.created_at = current_date - interval '1 day';", nativeQuery = true)
    BigDecimal forPreviousDay();

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id inner join orders o on o.id = og.orders_id where o.status ilike 'DELIVERED' and extract(month from o.created_at) = extract(month from current_date);", nativeQuery = true)
    BigDecimal forCurrentMonth();

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id inner join orders o on o.id = og.orders_id where o.status ilike 'DELIVERED' and extract(month from o.created_at) = extract(month from current_date - INTERVAL '1 month');", nativeQuery = true)
    BigDecimal forPreviousMonth();

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id inner join orders o on o.id = og.orders_id where o.status ilike 'DELIVERED' and extract(year from o.created_at) = extract(year from current_date);", nativeQuery = true)
    BigDecimal forCurrentYear();

    @Query(value = "select sum(s.current_price) from sub_gadgets s inner join gadgets g on s.gadget_id = g.id inner join orders_gadgets og on og.gadgets_id = g.id inner join orders o on o.id = og.orders_id where o.status ilike 'DELIVERED' and extract(year from o.created_at) = extract(year from current_date  - INTERVAL '1 year');", nativeQuery = true)
    BigDecimal forPreviousYear();

    @Query("select new gadgetarium.dto.response.AllOrderHistoryResponse(o.id, o.createdAt, o.number, o.status, o.totalPrice) from Order o join o.user u where u.id = :userId")
    List<AllOrderHistoryResponse> getAllHistory(Long userId);

    default Order getOrderById(Long orderId) {
        return findById(orderId).orElseThrow(() ->
                new NotFoundException("Order with id: " + orderId + " not found"));
    }

}