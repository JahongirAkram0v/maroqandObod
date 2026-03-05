package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.samtuit.maroqandObod.model.User;
import uz.samtuit.maroqandObod.model.UserInfo;

import java.util.Optional;

public interface UserInfoRepo extends JpaRepository<UserInfo, String> {

    @Query("select u from _user_info ui left join ui.user u where ui.id = :id")
    Optional<User> findUserByUserInfoId(@Param("id") String id);

    Optional<UserInfo> findByLoginAndPassword(String login, String password);
}
