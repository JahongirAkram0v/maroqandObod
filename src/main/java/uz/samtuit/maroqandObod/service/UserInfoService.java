package uz.samtuit.maroqandObod.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.samtuit.maroqandObod.model.Event;
import uz.samtuit.maroqandObod.model.User;
import uz.samtuit.maroqandObod.model.UserInfo;
import uz.samtuit.maroqandObod.repo.UserInfoRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final UserInfoRepo userInfoRepo;

    public Optional<User> findUserByUserInfoId(String id) {
        return userInfoRepo.findUserByUserInfoId(id);
    }

    public Optional<UserInfo> findByLoginAndPassword(String login, String password) {
        return userInfoRepo.findByLoginAndPassword(login, password);
    }

    public void save(UserInfo userInfo) {
        userInfoRepo.save(userInfo);
    }

    public List<UserInfo> findAll() {
        return userInfoRepo.findAll();
    }

    public Optional<UserInfo> findById(String id) {
        return userInfoRepo.findById(id);
    }
}
