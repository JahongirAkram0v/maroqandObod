package uz.samtuit.maroqandObod.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.UserInfoService;
import uz.samtuit.maroqandObod.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

@Component
@RequiredArgsConstructor
public class AuthService {

    private final SendService sendService;
    private final UserService userService;
    private final UserInfoService userInfoService;

    public void auth(User user, Message message) {
        UserState state = user.getState();
        Long chatId = user.getChatId();

        switch (state) {
            case START -> {
                user.setState(UserState.ROLE);
                userService.save(user);
                sendService.send(Utils.text(chatId, START_TEXT), "sendMessage");//keyboard
            }
            case ROLE -> {
                if (!message.hasText()) return;
                String text = message.getText();

                switch (text) {
                    case ORG_K -> applyRoleAndAuth(user, UserRole.ORG, 3, chatId);
                    case USER_K -> applyRoleAndAuth(user, UserRole.USER, 1, chatId);
                }
            }
            case AUTH -> {
                if (!message.hasText()) return;
                String text = message.getText().replaceAll("[^\\p{L}\\p{Nd}\\r\\n _]", "");

                String[] texts = text.split("\\R");
                if (texts.length != 2) {
                    String error = "Iltimos, INN va parolni to'g'ri formatda kiriting";
                    sendService.send(Utils.text(chatId, error), "sendMessage");
                    return;
                }

                Optional<UserInfo> optionalUserInfo = userInfoService.findByLoginAndPassword(texts[0], texts[1]);
                if (optionalUserInfo.isEmpty()) {
                    String error = "INN yoki parol noto'g'ri, iltimos qayta urinib ko'ring";
                    sendService.send(Utils.text(chatId, error), "sendMessage");
                    return;
                }
                UserInfo userInfo = optionalUserInfo.get();
                Optional<User> optionalUser = userInfoService.findUserByUserInfoId(userInfo.getId());

                if (optionalUser.isEmpty()) {
                    user.setUserInfo(userInfo);
                    userService.save(user);
                }
                else {
                    User oldUser = optionalUser.get();
                    oldUser.setChatId(chatId);
                    userInfo.setUser(oldUser);
                    userInfoService.save(userInfo);
                }

                sendService.send(Utils.text(chatId, CONTACT_TEXT,
                        List.of(List.of(Map.of("text", CONTACT,"request_contact", true)))
                ), "sendMessage");
                user.setState(UserState.PHONE_NUMBER);
                userService.save(user);
            }
            case PHONE_NUMBER -> {
                if (!message.hasContact()) return;
                String phoneNumber = message.getContact().getPhoneNumber();
                user.setPhoneNumber(phoneNumber.substring(3));
                user.setAuth(true);
                user.setState(UserState.READY);
                userService.save(user);
                sendService.send(Utils.text(chatId, CONG_TEXT,
                        List.of(List.of(Map.of("text", FULL)))
                ), "sendMessage");
            }
        }
    }

    private void applyRoleAndAuth(User user, UserRole userRole, int i, Long chatId) {
        user.setRole(userRole);
        user.setS(new int[i]);
        user.setState(UserState.AUTH);
        userService.save(user);
        sendService.send(Utils.remove(chatId, AUTH_VALID), "sendMessage");
    }
}
