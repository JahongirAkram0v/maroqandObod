package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.samtuit.maroqandObod.model.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {

    Optional<User> findByChatId(Long chatId);
}
