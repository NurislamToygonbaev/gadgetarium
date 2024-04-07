package gadgetarium.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "emails")
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_seq")
    @SequenceGenerator(name = "email_seq",allocationSize = 1)
    private Long id;
    private String email;
}