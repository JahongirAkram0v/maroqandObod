package uz.samtuit.maroqandObod.botService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.*;

import java.time.LocalDateTime;
import java.util.*;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

@Component
@RequiredArgsConstructor
public class BotAdminService {

    private final Dotenv dotenv = Dotenv.load();
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");

    private final SendService sendService;
    private final AdminService adminService;

    private final UserService userService;
    private final UserInfoService userInfoService;

    private final AdminHelperAll adminHelperAll;
    private final AdminHelperReferral adminHelperReferral;

    public void handle(Admin admin, String text) {
        if (text.equals("/start")) {
            sendService.send(Utils.admin(admin.getId(), WELCOME),"sendMessage");
            return;
        }
        AdminState adminState = admin.getState();

        switch (adminState) {
            case SETUP -> adminSetup(text, admin);
            case CREATE_ORG -> adminCreateUser(text, admin);
            case EDIT -> adminEdit(text, admin);
        }
    }

    private void adminSetup(String text, Admin admin) {
        if (text.equals(ADD_ORG)) {
            admin.setState(AdminState.CREATE_ORG);
            adminService.save(admin);
            sendService.send(Utils.text(admin.getId(), CREATE_ORG_TEXT), "sendMessage");
            return;
        }
        if (text.equals(ALL_ORG)) {
            adminHelperAll.handle();
        }
        if (text.startsWith("/start ")) {

            String payload = text.substring(text.indexOf(' ') + 1).trim();

            String[] parts = payload.split("_", 2);
            String ins = parts[0];
            String id = parts[1];
            if (ins.isBlank() || id.isBlank()) return;

            adminHelperReferral.handle(admin, ins, id);
        }
    }

    private void adminCreateUser(String text, Admin admin) {
        text = text.replaceAll("[^\\p{L}\\p{Nd}\\r\\n _]", "");

        String[] rows = text.split("\\R");
        int success = 0;

        for (String row : rows) {
            if (row.isBlank()) continue;

            String[] columns = row.split("\\s+");
            if (columns.length != 3) {
                String error = "Xatolik mavjud\n" + row;
                sendService.send(Utils.text(admin.getId(), error), "sendMessage");
                continue;
            }

            String login = columns[0].trim();
            String name = columns[1].trim();
            String password = columns[2].trim();

            success++;

            UserInfo userInfo = UserInfo.builder()
                    .login(login)
                    .name(name)
                    .password(password)
                    .createdDate(LocalDateTime.now())
                    .build();
            userInfoService.save(userInfo);
        }
        admin.setState(AdminState.SETUP);
        adminService.save(admin);
        if (success != 0) {
            String successText = success + "ta tashkilotlar yaratildi!";
            sendService.send(Utils.text(admin.getId(), successText), "sendMessage");
        }
    }

    private void adminEdit(String text, Admin admin) {
        text = text.replaceAll("[^\\p{L}\\p{Nd}\\r\\n _]", "");

        String[] columns = text.split("\\s+");
        if (columns.length != 3) {
            String error = "Iltimos, ma'lumotlarni to'g'ri formatda yuboring!";
            sendService.send(Utils.text(admin.getId(), error), "sendMessage");
            return;
        }
        String id = admin.getEditId();

        String newLogin = columns[0];
        String newName = columns[1];
        String newPassword = columns[2];

        Optional<UserInfo> optionalUserInfo = userInfoService.findById(id);
        if (optionalUserInfo.isEmpty()) {
            admin.setState(AdminState.SETUP);
            admin.setEditId(null);
            adminService.save(admin);
            String error = "Xatolik ketdi, qaytadan urinib ko'ring";
            sendService.send(Utils.text(admin.getId(), error), "sendMessage");
            return;
        }
        UserInfo userInfo = optionalUserInfo.get();

        userInfo.setLogin(newLogin);
        userInfo.setName(newName);
        userInfo.setPassword(newPassword);
        userInfoService.save(userInfo);

        admin.setState(AdminState.SETUP);
        admin.setEditId(null);
        adminService.save(admin);
        String result = String.format(
                "Tashkilot ma'lumotlari yangilandi%n" +
                        "%s %s %s",
                newLogin,
                newName,
                newPassword
        );
        sendService.send(Utils.text(admin.getId(), result),"sendMessage");
    }

}
