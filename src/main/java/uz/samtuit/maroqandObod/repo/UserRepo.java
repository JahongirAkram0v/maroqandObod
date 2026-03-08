package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.samtuit.maroqandObod.model.AuthUserDto;
import uz.samtuit.maroqandObod.model.Event;
import uz.samtuit.maroqandObod.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {

    @Query("select u.event from _user u where u.id = :id")
    Optional<Event> findEventByUserId(@Param("id") String id);

    @Query("""
        select
            u.s as s,
            ui.name as name
        from _user u
        left join u.userInfo ui
        where u.isAuth = true
        """)
    List<AuthUserDto> findAllAuthWithUserInfo();

    @Query("select ui.name from _user u join u.userInfo ui where u.id = :id")
    Optional<String> findUserNameById(@Param("id") String id);

    Optional<User> findByChatId(Long chatId);
}
