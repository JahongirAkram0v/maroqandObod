package uz.samtuit.maroqandObod.bot;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.UserInfoService;
import uz.samtuit.maroqandObod.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

@Component
@RequiredArgsConstructor
public class AdminHelperReferral {

    private final Dotenv dotenv = Dotenv.load();
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");

    private final SendService sendService;
    private final UserInfoService userInfoService;
    private final UserService userService;
    private final AdminService adminService;

    public void handle(Admin admin, String ins, String id) {

        if (ins.equals("edit")) {
            //password qismini oylashim kerak
            Optional<UserInfo> optionalUserInfo = userInfoService.findById(id);
            if (optionalUserInfo.isEmpty()) {
                sendService.send(Utils.text(admin.getId(), NOT_FOUND_ERROR_USERINFO), "sendMessage");
                return;
            }
            UserInfo userInfo = optionalUserInfo.get();
            admin.setEditId(id);
            admin.setState(AdminState.EDIT);
            adminService.save(admin);
            String oldAuth = String.format(
                    "Ma'lumotlarni o'zgartirishingiz mumkin%n" +
                            "Eski ma'lumotlar: %s %s %s",
                    userInfo.getLogin(),
                    userInfo.getName(),
                    userInfo.getPassword()
            );
            sendService.send(Utils.text(admin.getId(),oldAuth), "sendMessage");
            return;
        }

        Optional<User> optionalUser = userService.findById(id);
        if (optionalUser.isEmpty()) {
            sendService.send(Utils.text(admin.getId(), NOT_FOUND_ERROR_USER), "sendMessage");
            return;
        }
        User user = optionalUser.get();
        if (ins.equals("stat")) {
            int[] s = user.getS();
            String stat = "small: " + s[0] + ", medium: " + s[1] + ", large: " + s[2];
            sendService.send(Utils.text(admin.getId(), stat), "sendMessage");
            return;
        }

        Optional<Event> optionalEvent = userService.findEventByUserId(user.getId());
        if (optionalEvent.isEmpty()) {
            sendService.send(Utils.text(admin.getId(), NOT_FOUND_ERROR_EVENT), "sendMessage");
            return;
        }
        Event event = optionalEvent.get();

        //
        if (ins.startsWith("vol-")) {
            int index = Integer.parseInt(ins.substring(ins.indexOf("-")+1));
            int[] s = user.getS();
            UserRole role = user.getRole();
            //TODO:teginmay tursammikan
            if (role == UserRole.USER) {
                s[1] += 1;//ozgartirishim mumkin
            } else {
                s[index] += event.getCount();
            }
            user.setS(s);
            userService.save(user);
            String vol = index == 5 ? "Yirik" : index == 4 ? "O'rta" : "Kichik";
            sendService.send(Utils.text(admin.getId(), "Ma'lumotlar yuborildi"), "sendMessage");
            if (user.getState() != UserState.BLOCK) {
                sendService.send(Utils.text(user.getChatId(), vol + " mashina yuborildi"), "sendMessage");
            }
            return;
        }

        switch (ins) {
            case "share" -> {
                if (user.getState() != UserState.FULL) return;
                Optional<String> optionalName = userService.findUserNameById(user.getId());
                if (optionalName.isEmpty()) {
                    sendService.send(Utils.text(admin.getId(), NOT_FOUND_ERROR), "sendMessage");
                    return;
                }
                String name = optionalName.get();
                String caption = String.format(
                        "Tashkilot nomi: %s%n" +
                        "Telefon raqami: %s%n" +
                                "Konteynerlar soni: %s",
                        name,
                        user.getPhoneNumber(),
                        event.getCount()
                );
                sendService.send(Utils.photo(admin.getId(), event.getImageId(), caption), "sendPhoto");
                sendService.send(
                        Utils.location(admin.getId(), event.getLatitude(), event.getLongitude()),
                        "sendLocation");

                StringBuilder sb = new StringBuilder();
                List<Map<String, Object>> entities = new ArrayList<>();
                sb.append("Yuboriladigan yukmashina turini tanlang: ");
                String small = "Kichik";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", small.length(),
                                "url", "https://t.me/" + botUsername + "?start=vol-3_" + user.getId()
                        )
                );
                sb.append(small).append(" ");
                String medium = "O'rta";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", medium.length(),
                                "url", "https://t.me/" + botUsername + "?start=vol-4_" + user.getId()
                        )
                );
                sb.append(medium).append(" ");
                String large = "Yirik";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", large.length(),
                                "url", "https://t.me/" + botUsername + "?start=vol-5_" + user.getId()
                        )
                );
                sb.append(large);
                sendService.send(Utils.textEntity(admin.getId(), sb.toString(), entities), "sendMessage");
            }
            case "done" -> {
                if (event.getCreatedDate() == null) return;
                int[] s = user.getS();
                UserRole role = user.getRole();
                //TODO:teginmay tursammikan
                if (role == UserRole.USER) {
                    s[0] = s[1];
                } else {
                    s[0] = s[3];
                    s[1] = s[4];
                    s[2] = s[5];
                }
                user.setS(s);
                user.setEvent(null);
                user.setState(UserState.READY);
                userService.save(user);
                if (user.getState() != UserState.BLOCK) {
                    sendService.send(Utils.text(user.getChatId(), DONE_TEXT,
                            List.of(List.of(Map.of("text", FULL)))
                    ), "sendMessage");
                }

                sendService.send(Utils.text(admin.getId(), "Ma'lumot yangilandi"), "sendMessage");
            }
        }

    }
}
