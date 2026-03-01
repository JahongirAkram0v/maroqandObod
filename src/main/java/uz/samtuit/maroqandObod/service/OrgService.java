package uz.samtuit.maroqandObod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.repo.OrgRepo;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrgService {

    private final OrgRepo orgRepo;

    public void save(Org org) {
        orgRepo.save(org);
    }

    public Optional<Org> findByChatId(Long id) {
        return orgRepo.findByChatId(id);
    }

    public void deleteOrg(Org org) {
        orgRepo.delete(org);
    }
}
