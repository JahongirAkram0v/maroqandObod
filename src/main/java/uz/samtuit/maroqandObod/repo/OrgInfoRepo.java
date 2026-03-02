package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.samtuit.maroqandObod.model.OrgInfo;

import java.util.List;
import java.util.Optional;

public interface OrgInfoRepo  extends JpaRepository<OrgInfo, String> {

    Optional<OrgInfo> findByInnAndPassword(String inn, String password);

    List<OrgInfo> findAllByOrgInfoByCreatedDateAsc();
}
