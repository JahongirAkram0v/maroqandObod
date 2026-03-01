package uz.samtuit.maroqandObod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.samtuit.maroqandObod.model.OrgInfo;
import uz.samtuit.maroqandObod.repo.OrgInfoRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrgInfoService {

    private final OrgInfoRepo orgInfoRepo;

    public Optional<OrgInfo> findByInnAndPassword(String inn, String password) {
        return orgInfoRepo.findByInnAndPassword(inn, password);
    }

    public List<OrgInfo> findAll() {
        return orgInfoRepo.findAll();
    }

    public void save(OrgInfo orgInfo) {
        orgInfoRepo.save(orgInfo);
    }

    public Optional<String> findNameByInn(String inn) {
        return orgInfoRepo.findNameByInn(inn);
    }

    public Optional<OrgInfo> findByInn(String inn) {
        return orgInfoRepo.findByInn(inn);
    }

    public void deleteOrgInfo(OrgInfo orgInfo) {
        orgInfoRepo.delete(orgInfo);
    }
}
