package uz.samtuit.maroqandObod.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Admin {

    @Id
    private Long id;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AdminState state = AdminState.SETUP;
    private String editId;
}
