package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.*;

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
}