package gadgetarium.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "char_values")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CharValue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "values_seq")
    @SequenceGenerator(name = "values_seq", allocationSize = 1, initialValue = 60)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @Size(max = 3000)
    private Map<String, String> values;

    public void addCharacteristic(String key, String value) {
        if (this.values == null) this.values = new LinkedHashMap<>();
        this.values.put(key, value);
    }

    @PrePersist
    private void initialReview() {
        this.values = new LinkedHashMap<>();
    }
}