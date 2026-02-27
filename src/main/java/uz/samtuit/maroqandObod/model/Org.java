package uz.samtuit.maroqandObod.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orges")
public class Org {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long chatId;

    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private String imageId;
    private int containerCount;
    private boolean isFilled;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrgState orgState = OrgState.PHONE_NUMBER;

    @OneToOne
    @JoinColumn(name = "org_info_id")
    private OrgInfo orgInfo;
}
