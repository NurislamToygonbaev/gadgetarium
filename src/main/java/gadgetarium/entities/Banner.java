package gadgetarium.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
    @SequenceGenerator(name = "banner_seq", allocationSize = 1, initialValue = 60)
    private Long id;

    @Size(max = 1000)
    @ElementCollection
    private List<String> images;

    public void addImages(String image) {
        if (this.images == null) this.images = new ArrayList<>();
        this.images.add(image);
    }
}