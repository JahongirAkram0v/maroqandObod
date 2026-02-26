package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.samtuit.maroqandObod.model.Org;

import java.util.Optional;

public interface OrgRepo extends JpaRepository<Org, String> {

    Optional<Org> findByInn(String inn);
    Optional<Org> findByChatId(Long id);
}
