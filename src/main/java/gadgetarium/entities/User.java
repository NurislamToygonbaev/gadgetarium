package gadgetarium.entities;

import gadgetarium.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", allocationSize = 1, initialValue = 51)
    private Long id;
    private String firstName;
    private String lastName;
    private String image;
    private String phoneNumber;
    private String email;
    private String password;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = {DETACH, MERGE, REFRESH})
    private List<Order> orders;

    @OneToMany(mappedBy = "user", cascade = {REMOVE, MERGE, REFRESH})
    private List<Feedback> feedbacks;

    @ManyToMany(cascade = {REFRESH, MERGE})
    private List<SubGadget> basket;

    @ManyToMany(cascade = {REFRESH, MERGE}, fetch = FetchType.EAGER)
    private List<SubGadget> comparison;

    @ManyToMany(cascade = {REFRESH, MERGE})
    private List<SubGadget> viewed;

    @ManyToMany(cascade = {REFRESH, MERGE})
    private List<SubGadget> likes;

    @OneToMany(mappedBy = "user", cascade = {REMOVE, REFRESH})
    private List<PasswordResetToken> passwordResetTokens;

    public void addOrder(Order order) {
        if (this.orders == null) this.orders = new ArrayList<>();
        this.orders.add(order);
    }

    public void addFeedback(Feedback feedback) {
        if (this.feedbacks == null) this.feedbacks = new ArrayList<>();
        this.feedbacks.add(feedback);
    }

    public void addBasket(SubGadget basket) {
        if (this.basket == null) this.basket = new ArrayList<>();
        this.basket.add(basket);
    }

    public void addComparison(SubGadget comparison) {
        if (this.comparison == null) this.comparison = new ArrayList<>();
        this.comparison.add(comparison);
    }

    public void addViewed(SubGadget viewed) {
        if (this.viewed == null) this.viewed = new ArrayList<>();
        this.viewed.add(viewed);
    }

    public void addLikes(SubGadget likes) {
        if (this.likes == null) this.likes = new ArrayList<>();
        this.likes.add(likes);
    }

    public void addPasswordResetToken(PasswordResetToken token) {
        if (this.passwordResetTokens == null) this.passwordResetTokens = new ArrayList<>();
        this.passwordResetTokens.add(token);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
