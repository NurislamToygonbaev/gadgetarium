package gadgetarium.repositories;
import gadgetarium.entities.Order;
import gadgetarium.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    default Order getOrderById(Long orderId){
        return findById(orderId).orElseThrow(() ->
                new NotFoundException("Order with id: "+orderId+" not found"));
    }
}