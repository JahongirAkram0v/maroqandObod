package uz.samtuit.maroqandObod.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orges_info")
public class OrgInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String inn;
    private String password;
    private String name;

    @OneToOne(mappedBy = "orgInfo", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Org org;
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}
