package uz.samtuit.maroqandObod.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "_user")
@Table(name = "_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private Long chatId;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.ORG;
    private String phoneNumber;

    private boolean isAuth;

    @Builder.Default
    private int[] s = new int[3];

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserState state = UserState.AUTH;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Event event;

    public void setEvent(Event newEvent) {
        if (this.event != null) {
            Event oldEvent = this.event;
            this.event = null;
            oldEvent.setUser(null);
        }

        if (newEvent != null) {
            this.event = newEvent;
            newEvent.setUser(this);
        }
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "_user_info_id", unique = true)
    private UserInfo userInfo;

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
        if (userInfo != null) {
            userInfo.setUser(this);
        }
    }
}