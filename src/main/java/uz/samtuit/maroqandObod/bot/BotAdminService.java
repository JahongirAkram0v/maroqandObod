package uz.samtuit.maroqandObod.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.*;

import java.util.*;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

@Component
@RequiredArgsConstructor
public class BotAdminService {

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
        switch (text) {
            case ADD_ORG -> {
                admin.setState(AdminState.CREATE_ORG);
                adminService.save(admin);
                sendService.send(Utils.text(admin.getId(), CREATE_ORG_TEXT), "sendMessage");
                return;
            }
            case ALL_ORG -> {
                adminHelperAll.handle();
                return;
            }
            case STAT_ORG -> statOrg(admin.getId());
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

    private void statOrg(Long id) {
        List<AuthUserDto> users = userService.findAllAuthWithUserInfo();
        StringBuilder sb = new StringBuilder();
        if (users.isEmpty()) {
            return;
        }
        for (AuthUserDto user : users) {
            int[] stat = user.getS();
            sb.append(user.getName()).append(": ");
            sb.append(stat[0]).append(" ").append(stat[1]).append(" ").append(stat[2]);
            sb.append("\n");
        }
        sendService.send(Utils.text(id, sb.toString()), "sendMessage");
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
            sendService.send(Utils.text(admin.getId(), FORMAT_ERROR), "sendMessage");
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
            sendService.send(Utils.text(admin.getId(), NOT_FOUND_ERROR_USERINFO), "sendMessage");
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
