package uz.samtuit.maroqandObod.botService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.OrgInfoService;
import uz.samtuit.maroqandObod.service.OrgService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uz.samtuit.maroqandObod.config.KeyboardNameConfig.ADD_ORG;
import static uz.samtuit.maroqandObod.config.KeyboardNameConfig.ALL_ORG;

@Component
@RequiredArgsConstructor
public class BotAdminService {

    private final Dotenv dotenv = Dotenv.load();
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");

    private final AdminService adminService;
    private final OrgInfoService orgInfoService;
    private final OrgService orgService;
    private final SendService sendService;

    public void handleAdminMessage(Admin admin, String text) {

        if (text.equals("/start")) {
            sendService.send(Utils.adminKeyboard(admin.getId(),
                            "Xush kelibsiz!"
                    ),"sendMessage");
            return;
        }

        AdminState adminState = admin.getState();

        switch (adminState) {
            case SETUP -> adminSetup(text, admin);
            case CREATE_ORG -> adminCreateOrg(text, admin);
            case EDIT -> adminEdit(text, admin);
        }
    }

    private void adminEdit(String text, Admin admin) {
        String[] columns = text.split("\\s+");
        if (columns.length != 3) {
            sendService.send(Utils.text(
                            admin.getId(),
                            "Iltimos, ma'lumotlarni to'g'ri formatda yuboring!"),
                    "sendMessage");
            return;
        }
        String id = admin.getEditId();
        if (id == null) {
            admin.setState(AdminState.SETUP);
            adminService.save(admin);
            return;
        }
        String newInn = columns[0];
        String newName = columns[1];
        String newPassword = columns[2];

        Optional<OrgInfo> optionalOrgInfo = orgInfoService.findById(id);
        if (optionalOrgInfo.isEmpty()) {
            admin.setState(AdminState.SETUP);
            admin.setEditId(null);
            adminService.save(admin);
            sendService.send(Utils.text(admin.getId(), "Xatolik ketdi, qaytadan urinib ko'ring"), "sendMessage");
            return;
        }
        OrgInfo orgInfo = optionalOrgInfo.get();
        orgInfo.setInn(newInn);
        orgInfo.setName(newName);
        orgInfo.setPassword(newPassword);
        orgInfoService.save(orgInfo);

        admin.setState(AdminState.SETUP);
        admin.setEditId(null);
        adminService.save(admin);
        sendService.send(Utils.text(admin.getId(),
                "Tashkilot ma'lumotlari yangilandi\n" +
                        newInn + " " + newName + " " + newPassword
        ),"sendMessage");
    }

    private void adminCreateOrg(String text, Admin admin) {
        String[] rows = text.split("\\R");
        int success = rows.length;

        for (String row : rows) {
            row = row.trim();
            if (row.isBlank()) {
                success--;
                continue;
            }

            String[] columns = row.split("\\s+");
            if (columns.length != 3) {
                success--;
                continue;
            }
            if (columns[0].isBlank() && columns[1].isBlank() && columns[2].isBlank()) {
                success--;
                continue;
            }

            String inn = columns[0].trim();
            String name = columns[1].trim();
            String password = columns[2].trim();

            OrgInfo orgInfo = OrgInfo.builder()
                    .inn(inn)
                    .name(name)
                    .password(password)
                    .build();
            orgInfoService.save(orgInfo);
        }
        admin.setState(AdminState.SETUP);
        adminService.save(admin);
        if (success != 0) {
            sendService.send(Utils.text(
                            admin.getId(),
                            success + "ta tashkilotlar yaratildi!"),
                    "sendMessage");
        } else {
            sendService.send(Utils.text(
                            admin.getId(),
                            "Tashkilot yaratilmadi, iltimos formatga e'tibor bering!"),
                    "sendMessage");
        }
    }

    private void adminSetup(String text, Admin admin) {
        switch (text) {
            case ADD_ORG -> {
                admin.setState(AdminState.CREATE_ORG);
                adminService.save(admin);
                sendService.send(Utils.text(admin.getId(),
                                "Ma'lumotlarni quyidagi formatda kiriting:\n" +
                                        "[123456789 Tashkilot_nomi password]"
                        ),"sendMessage");
            }
            case ALL_ORG -> {
                List<OrgInfo> orgInfos = orgInfoService.findAll();
                if (orgInfos.isEmpty()) {
                    sendService.send(Utils.text(admin.getId(),
                                    "Hozircha tashkilotlar mavjud emas."
                            ),"sendMessage");
                    return;
                }
                StringBuilder sb = new StringBuilder();
                List<Map<String, Object>> entities = new ArrayList<>();

                String title = "📋 Tashkilotlar ro‘yxati";
                sb.append(title).append("\n\n");

                entities.add(Map.of(
                        "type", "bold",
                        "offset", 0,
                        "length", title.length()
                ));

                for (OrgInfo orgInfo : orgInfos) {
                    appendOrgLine(sb, entities, orgInfo);
                }

                sendService.send(
                        Utils.textEntity(admin.getId(), sb.toString(), entities),
                        "sendMessage"
                );
            }
        }
        if (text.startsWith("/start ")) {

            String payload = text.substring(text.indexOf(' ') + 1).trim();

            String[] parts = payload.split("_", 2);
            String ins = parts[0];
            String id = parts[1];
            if (ins.isBlank() || id.isBlank()) return;
            Optional<OrgInfo> optionalOrgInfo = orgInfoService.findById(id);
            if (optionalOrgInfo.isEmpty()) {
                sendService.send(Utils.text(admin.getId(),
                        "Xatolik ketdi: link, org mavjud emas"
                ), "sendMessage");
                return;
            }
            OrgInfo orgInfo = optionalOrgInfo.get();

            switch (ins) {
                case "share" -> {
                    Org org = orgInfo.getOrg();
                    if (org == null) {
                        sendService.send(Utils.text(admin.getId(),
                                "Tashkilot hali ro'yxatdan o'tmagan"
                        ), "sendMessage");
                        return;
                    }
                    sendService.send(Utils.sendPhoto(admin.getId(), org.getImageId(),
                            "Tashkilot nomi: " + orgInfo.getName() + "\n" +
                                    "Telefon raqami: " + org.getPhoneNumber()
                            ), "sendPhoto");
                    sendService.send(
                            Utils.sendLocation(admin.getId(), org.getLatitude(), org.getLongitude()),
                            "sendLocation");
                }
                case "done" -> {
                    Org org = orgInfo.getOrg();
                    if (org == null) {
                        sendService.send(Utils.text(admin.getId(),
                                "Tashkilot hali ro'yxatdan o'tmagan"
                        ), "sendMessage");
                        return;
                    }
                    org.setFilled(false);
                    orgService.save(org);
                    sendService.send(
                            Utils.org(org.getChatId(), "Chiqindi olib ketildi", false),
                            "sendMessage");
                    sendService.send(Utils.text(admin.getId(), "Ma'lumot yangilandi"), "sendMessage");
                }
                case "edit" -> {
                    admin.setEditId(id);
                    admin.setState(AdminState.EDIT);
                    adminService.save(admin);
                    sendService.send(Utils.text(admin.getId(),
                            "Ma'lumotlarni o'zgartirishingiz mumkin\nEski ma'lumotlar: " +
                                    orgInfo.getInn() + " " + orgInfo.getName() + " " + orgInfo.getPassword()
                            ), "sendMessage");
                }
                case "delete" -> {
                    Org org = orgInfo.getOrg();
                    if (org != null) {
                        orgService.deleteOrg(org);
                    }
                    orgInfoService.deleteOrgInfo(orgInfo);
                    sendService.send(Utils.text(admin.getId(),
                            "Tashkilot ma'lumotlari butunlay ochirildi!"
                    ), "sendMessage");
                }
            }
        }
    }

    private void appendOrgLine(
            StringBuilder sb,
            List<Map<String, Object>> entities,
            OrgInfo orgInfo
    ) {

        String inn = orgInfo.getInn();
        entities.add(Map.of(
                        "type", "italic",
                        "offset", sb.length(),
                        "length", inn.length()
        ));
        sb.append(inn);

        Org org = orgInfo.getOrg();
        if (org == null) {
            sb.append("⚠️          ");
            controller(sb, entities, orgInfo);
            return;
        }
        if (org.getOrgState() != OrgState.READY && org.getOrgState() != OrgState.BLOCKED) {
            sb.append("⚠️          ");
            controller(sb, entities, orgInfo);
            return;
        }
        sb.append("          ");

        String status = (org.isFilled() ? " ♻️ " : " \uD83D\uDDD1 ");
        sb.append(status);

        String share = "📥";
        entities.add(
                Map.of(
                        "type", "text_link",
                        "offset", sb.length(),
                        "length", share.length(),
                        "url", "https://t.me/" + botUsername + "?start=share_" + orgInfo.getId().trim()
                )
        );
        sb.append(share).append(" • ");

        if (org.isFilled()) {
            String done = "☑️";
            entities.add(
                    Map.of(
                            "type", "text_link",
                            "offset", sb.length(),
                            "length", done.length(),
                            "url", "https://t.me/" + botUsername + "?start=done_" + orgInfo.getId().trim()
                    )
            );
            sb.append(done).append("  • ");
        }

        controller(sb, entities, orgInfo);
    }

    private void controller(StringBuilder sb, List<Map<String, Object>> entities, OrgInfo orgInfo) {
        String edit = "✏️";
        entities.add(
                Map.of(
                        "type", "text_link",
                        "offset", sb.length(),
                        "length", edit.length(),
                        "url", "https://t.me/" + botUsername + "?start=edit_" + orgInfo.getId().trim()
                )
        );
        sb.append(edit).append(" • ");

        String delete = "❌";
        entities.add(
                Map.of(
                        "type", "text_link",
                        "offset", sb.length(),
                        "length", delete.length(),
                        "url", "https://t.me/" + botUsername + "?start=delete_" + orgInfo.getId().trim()
                )
        );
        sb.append(delete).append("\n");
    }
}
