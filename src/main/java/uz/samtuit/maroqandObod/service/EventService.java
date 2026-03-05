package uz.samtuit.maroqandObod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.samtuit.maroqandObod.repo.EventRepo;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepo eventRepo;

}
