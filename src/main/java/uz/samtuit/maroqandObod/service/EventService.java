package uz.samtuit.maroqandObod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.samtuit.maroqandObod.model.Event;
import uz.samtuit.maroqandObod.repo.EventRepo;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepo eventRepo;

    public List<Event> findAll() {
        return eventRepo.findAll();
    }


}
