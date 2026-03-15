package uz.samtuit.maroqandObod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.samtuit.maroqandObod.model.AuthUserDto;
import uz.samtuit.maroqandObod.model.Event;
import uz.samtuit.maroqandObod.model.User;
import uz.samtuit.maroqandObod.repo.UserRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;

    public Optional<Event> findEventByUserId(String id) {
        return userRepo.findEventByUserId(id);
    }

    public Optional<String> findUserNameById(String id) {
        return userRepo.findUserNameById(id);
    }

    public Optional<User> findByChatId(Long chatId) {
        return userRepo.findByChatId(chatId);
    }

    public void save(User u) {
        userRepo.save(u);
    }

    public Optional<User> findById(String id) {
        return userRepo.findById(id);
    }

    public List<AuthUserDto> findAllAuthWithUserInfo() {
        return userRepo.findAllAuthWithUserInfo();
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }
}
