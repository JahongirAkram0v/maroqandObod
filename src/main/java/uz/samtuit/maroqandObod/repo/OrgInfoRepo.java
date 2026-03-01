package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.samtuit.maroqandObod.model.OrgInfo;

import java.util.Optional;

public interface OrgInfoRepo  extends JpaRepository<OrgInfo, String> {

    @Query("SELECT o FROM OrgInfo o WHERE o.inn = :inn AND o.password = :password")
    Optional<OrgInfo> findByInnAndPassword(String inn, String password);

    Optional<String> findNameByInn(String inn);

    Optional<OrgInfo> findByInn(String inn);
}
