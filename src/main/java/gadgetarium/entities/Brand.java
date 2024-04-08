package gadgetarium.entities;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.ArrayList;
import java.util.List;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.DETACH;
@Getter
@Setter
@Entity
@Table(name = "brands")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "brand_seq")
    @SequenceGenerator(name = "brand_seq", allocationSize = 1)
    private Long id;
    private String brandName;
    private String logo;

    @ManyToOne(cascade = {DETACH})
    private SubCategory subCategory;

    @OneToMany(mappedBy = "brand", cascade = {REFRESH, REMOVE, MERGE})
    private List<Gadget> gadgets;

    private void addGadget(Gadget gadget){
        if (this.gadgets == null) this.gadgets = new ArrayList<>();
        this.gadgets.add(gadget);
    }
}