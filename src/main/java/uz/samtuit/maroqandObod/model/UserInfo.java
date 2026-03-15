package uz.samtuit.maroqandObod.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "_user_info")
@Table(name = "_users_info")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String login;
    private String password;
    private String name;

    @OneToOne(mappedBy = "userInfo", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(updatable = false)
    private OffsetDateTime createdDate;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = OffsetDateTime.now(ZoneOffset.of("+05:00"));
        }
    }
}