package uz.samtuit.maroqandObod.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity(name = "_event")
@Table(name = "_events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String imageId;
    private int count;
    private double latitude;
    private double longitude;

    @Column(updatable = false)
    private LocalDateTime createdDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "_user_id", unique = true)
    private User user;
}
