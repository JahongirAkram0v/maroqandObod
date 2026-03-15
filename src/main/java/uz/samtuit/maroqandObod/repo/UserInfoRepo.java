package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.samtuit.maroqandObod.model.User;
import uz.samtuit.maroqandObod.model.UserInfo;
import uz.samtuit.maroqandObod.model.UserInfoDto;

import java.util.List;
import java.util.Optional;

public interface UserInfoRepo extends JpaRepository<UserInfo, String> {

    @Query("""
        select
            ui.id as id,
            ui.login as login,
            u.id as userId,
            u.isAuth as isAuth,
            case
                when e.createdDate is not null then true
                else false
            end as isFilled
        from _user_info ui
        left join ui.user u
        left join u.event e
        """)
    List<UserInfoDto> findAllDto();


    @Query("select u from _user_info ui left join ui.user u where ui.id = :id")
    Optional<User> findUserByUserInfoId(@Param("id") String id);

    Optional<UserInfo> findByLoginAndPassword(String login, String password);
}
