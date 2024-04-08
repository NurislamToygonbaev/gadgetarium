package gadgetarium.entities;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "banners")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banner_seq")
    @SequenceGenerator(name = "banner_seq", allocationSize = 1)
    private Long id;

    @ElementCollection
    private List<String> images;

    private void addImages(String image){
        if (this.images == null) this.images = new ArrayList<>();
        this.images.add(image);
    }
}