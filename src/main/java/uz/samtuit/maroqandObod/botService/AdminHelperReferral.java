package uz.samtuit.maroqandObod.botService;

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
            Optional<UserInfo> optionalUserInfo = userInfoService.findById(id);
            if (optionalUserInfo.isEmpty()) {
                sendService.send(Utils.text(admin.getId(), "Edit qilishda xatolik ketdi"), "sendMessage");
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
            sendService.send(Utils.text(admin.getId(), "Xatolik ketdi"), "sendMessage");
            return;
        }
        User user = optionalUser.get();
        Event event = user.getEvent();

        if (ins.startsWith("vol-")) {
            int index = Integer.parseInt(ins.substring(ins.indexOf("-")+1));
            int[] s = user.getS();
            s[index] += event.getCount();
            user.setS(s);
            userService.save(user);
            String vol = index == 2 ? "large" : index == 1 ? "medium" : "small";
            sendService.send(Utils.text(user.getChatId(), vol + " mashina yuborildi"), "sendMessage");
        }

        switch (ins) {
            case "share" -> {
                String caption = String.format(
                        "Tashkilot nomi: %s%n" +
                        "Telefon raqami: %s%n" +
                                "Konteynerlar soni: %s",
                        user.getName(),
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
                String small = "small";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", small.length(),
                                "url", "https://t.me/" + botUsername + "?start=vol-0_" + user.getId()
                        )
                );
                sb.append(small).append(" ");
                String medium = "medium";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", medium.length(),
                                "url", "https://t.me/" + botUsername + "?start=vol-1_" + user.getId()
                        )
                );
                sb.append(medium).append(" ");
                String large = "large";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", large.length(),
                                "url", "https://t.me/" + botUsername + "?start=vol-2_" + user.getId()
                        )
                );
                sb.append(large);
                sendService.send(Utils.textEntity(admin.getId(), sb.toString(), entities), "sendMessage");
            }
            case "done" -> {
                user.setFilled(false);
                userService.save(user);
                sendService.send(Utils.text(user.getChatId(), DONE_TEXT,
                        List.of(List.of(Map.of("text", FULL)))
                ), "sendMessage");
                sendService.send(Utils.text(admin.getId(), "Ma'lumot yangilandi"), "sendMessage");
            }
            case "stat" -> {
                int[] s = user.getS();
                String stat = "small: " + s[0] + ", medium: " + s[1] + ", large: " + s[2];
                sendService.send(Utils.text(admin.getId(), stat), "sendMessage");
            }
        }

    }
}
