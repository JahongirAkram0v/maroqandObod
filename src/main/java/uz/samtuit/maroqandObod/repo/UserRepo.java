package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.samtuit.maroqandObod.model.Event;
import uz.samtuit.maroqandObod.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {

    @Query("select u from _user u left join fetch u.userInfo where u.isAuth = true")
    List<User> findAllAuthWithUserInfo();

    @Query("select u.event from _user u where u.id = :id")
    Optional<Event> findEventByUserId(@Param("id") String id);

    @Query("select ui.name from _user u join u.userInfo ui where u.id = :id")
    Optional<String> findUserNameById(@Param("id") String id);

    Optional<User> findByChatId(Long chatId);
}
