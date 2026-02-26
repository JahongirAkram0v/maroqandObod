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

    private String inn;
    private String password;//TODO: passwordni saqlash uchun maxsus usul qo'llash kerak, masalan, hashing
    private String name;

    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private String imageId;
    private int containerCount;
    private boolean isFilled;
}
