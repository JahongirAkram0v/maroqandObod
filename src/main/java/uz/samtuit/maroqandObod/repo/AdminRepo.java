package uz.samtuit.maroqandObod.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.samtuit.maroqandObod.model.Admin;

public interface AdminRepo extends JpaRepository<Admin, Long> {
}
