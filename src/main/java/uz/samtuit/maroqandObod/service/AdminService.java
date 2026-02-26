package uz.samtuit.maroqandObod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.Admin;
import uz.samtuit.maroqandObod.repo.AdminRepo;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepo adminRepo;

    public Optional<Admin> findById(Long id) {
        return adminRepo.findById(id);
    }

    public void save(Admin admin) {
        adminRepo.save(admin);
    }
}
