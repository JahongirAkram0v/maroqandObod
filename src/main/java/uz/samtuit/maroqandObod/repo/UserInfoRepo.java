package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.samtuit.maroqandObod.model.UserInfo;

import java.util.Optional;

public interface UserInfoRepo extends JpaRepository<UserInfo, String> {

    Optional<UserInfo> findByLoginAndPassword(String login, String password);
}
