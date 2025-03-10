package unibank.web.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
//@Table(name = "cards")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String cardId;

    @Column(unique = true, nullable = false)
    private long cardNumber;
    private String cardHolder;
    private Double balance;
    private String cvv;
    private LocalDateTime exp;
    @JsonIgnore
    private String pin;
    private String billingAddress;

    @CreationTimestamp
    private LocalDateTime issuedAt;
    @UpdateTimestamp
    private LocalDateTime lastUpdated;

    @OneToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Transaction> transactions;
}