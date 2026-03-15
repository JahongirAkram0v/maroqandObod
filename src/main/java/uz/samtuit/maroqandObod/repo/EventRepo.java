package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.samtuit.maroqandObod.model.Event;

public interface EventRepo extends JpaRepository<Event, String> {
}
