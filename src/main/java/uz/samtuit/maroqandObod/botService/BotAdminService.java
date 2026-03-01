package uz.samtuit.maroqandObod.botService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.Admin;
import uz.samtuit.maroqandObod.model.AdminState;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.model.OrgInfo;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.OrgInfoService;
import uz.samtuit.maroqandObod.service.OrgService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        AdminState adminState = admin.getState();

        if (adminState == AdminState.SETUP) {
            if (text.equals("\uD83D\uDDC2 Tashkilot qo‘shish")) {
                admin.setState(AdminState.CREATE_ORG);
                adminService.save(admin);
                sendService.send(Utils.text(admin.getId(), "yaxshiroq misol yozaman"),
                        "sendMessage");
                return;
            }
            if (text.equals("📋 Tashkilotlar ro‘yxati")) {

                List<OrgInfo> orgInfos = orgInfoService.findAll();
                if (orgInfos.isEmpty()) {
                    sendService.send(
                            Utils.text(admin.getId(), "Hozircha tashkilotlar mavjud emas."),
                            "sendMessage"
                    );
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
                return;
            }
            if (text.startsWith("/start ")) {

                System.out.println(text);
                int spaceIdx = text.indexOf(' ');
                if (spaceIdx < 0 || spaceIdx + 1 >= text.length()) {
                    return;
                }
                String payload = text.substring(spaceIdx + 1).trim();
                if (payload.isEmpty()) return;

                String[] parts = payload.split("_", 2);
                String ins = parts[0];
                String inn = parts[1];
                if (ins.isBlank() || inn.isBlank()) return;
                List<String> validActions = List.of("done", "edit", "delete");
                if (!validActions.contains(ins)) return;
                Optional<String> optionalName = orgInfoService.findNameByInn(inn);
                if (optionalName.isEmpty()) return;
                admin.setState(AdminState.AGREEMENT);
                admin.setInstructionsName(ins);
                admin.setInstructionsInn(inn);
                adminService.save(admin);
                sendAgreementKeyboard(admin.getId(), ins, optionalName.get());
            }

            if (text.equals("/start")) {
                sendService.send(Utils.adminKeyboard(
                                admin.getId(),
                                "Xush kelibsiz!"),
                        "sendMessage");
            }
        }
        else if (adminState == AdminState.CREATE_ORG) {
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
                if (name.length() > 20) {
                    name = name.substring(20);
                }
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
            //Hali ham kamchilik bor!!
        }
        else if (adminState == AdminState.AGREEMENT) {
            if (text.equals("No")) {
                admin.setState(AdminState.SETUP);
                admin.setInstructionsName(null);
                admin.setInstructionsInn(null);
                adminService.save(admin);
                sendService.send(Utils.text(
                                admin.getId(),
                                "Amal bekor qilindi!"),
                        "sendMessage");
            }
            else if (text.equals("Yes")) {
                String inn = admin.getInstructionsInn();
                String ins = admin.getInstructionsName();

                switch (ins) {
                    case "done" -> {
                        Optional<OrgInfo> optionalOrgInfo = orgInfoService.findByInn(inn);
                        if (optionalOrgInfo.isEmpty()) {
                            sendService.send(Utils.text(admin.getId(), "Tashkilot topilmadi!"), "sendMessage");
                            return;
                        }
                        OrgInfo orgInfo = optionalOrgInfo.get();
                        Org org = orgInfo.getOrg();
                        if (org == null) {
                            sendService.send(Utils.text(admin.getId(), "⚠️"), "sendMessage");
                            return;
                        }
                        org.setFilled(false);
                        orgInfoService.save(orgInfo);
                        sendService.send(Utils.text(
                                        admin.getId(),
                                        orgInfo.getName() + "dagi konteyner bo'shatildi!"),
                                "sendMessage");
                        admin.setState(AdminState.SETUP);
                        admin.setInstructionsName(null);
                        admin.setInstructionsInn(null);
                        adminService.save(admin);
                    }
                    case "edit" -> {
                        admin.setState(AdminState.EDIT);
                        adminService.save(admin);
                        sendService.send(Utils.text(
                                        admin.getId(),
                                        "Tashkilot ma'lumotlarini yangilash uchun quyidagi formatda ma'lumotlarni yuboring:\nINN YangiNomi YangiParoli\nMisol: 1234567890 NewName newpassword"),
                                "sendMessage");
                    }
                    case "delete" -> {
                        Optional<OrgInfo> optionalOrgInfo = orgInfoService.findByInn(inn);
                        if (optionalOrgInfo.isEmpty()) {
                            sendService.send(Utils.text(admin.getId(), "Tashkilot topilmadi!"), "sendMessage");
                            return;
                        }
                        OrgInfo orgInfo = optionalOrgInfo.get();
                        Org org = orgInfo.getOrg();
                        if (org != null) {
                            orgService.deleteOrg(org);
                        }
                        orgInfoService.deleteOrgInfo(orgInfo);
                        sendService.send(Utils.text(
                                        admin.getId(),
                                        orgInfo.getName() + " tashkiloti o'chirildi!"),
                                "sendMessage");
                        admin.setState(AdminState.SETUP);
                        admin.setInstructionsName(null);
                        admin.setInstructionsInn(null);
                        adminService.save(admin);
                    }

                }
            }
        }
        else if (adminState == AdminState.EDIT) {
            String[] columns = text.split("\\s+");
            if (columns.length != 3) {
                sendService.send(Utils.text(
                                admin.getId(),
                                "Iltimos, ma'lumotlarni to'g'ri formatda yuboring!"),
                        "sendMessage");
                return;
            }
            String inn = admin.getInstructionsInn();
            String newInn = columns[0];
            String newName = columns[1];
            String newPassword = columns[2];

            Optional<OrgInfo> optionalOrgInfo = orgInfoService.findByInn(inn);
            if (optionalOrgInfo.isEmpty()) {
                sendService.send(Utils.text(admin.getId(), "Tashkilot topilmadi!"), "sendMessage");
                return;
            }
            OrgInfo orgInfo = optionalOrgInfo.get();
            orgInfo.setInn(newInn);
            orgInfo.setName(newName);
            orgInfo.setPassword(newPassword);
            orgInfoService.save(orgInfo);

            admin.setState(AdminState.SETUP);
            admin.setInstructionsName(null);
            admin.setInstructionsInn(null);
            adminService.save(admin);
            sendService.send(Utils.text(
                            admin.getId(),
                            orgInfo.getName() + " tashkiloti yangilandi!"),
                    "sendMessage");
        }
    }

    private void appendOrgLine(
            StringBuilder sb,
            List<Map<String, Object>> entities,
            OrgInfo orgInfo
    ) {

        String name = orgInfo.getName();
        sb.append(name);
        entities.add(
                Map.of(
                        "type", "italic",
                        "offset", 0,
                        "length", sb.length()
                )
        );
        if (orgInfo.getOrg() == null) {
            sb.append("⚠️   ");
            controller(sb, entities, orgInfo);
            return;
        }
        sb.append(" ".repeat(Math.max(0, 20 - name.length() + 1)));

        Org org = orgInfo.getOrg();
        String status = (org.isFilled() ? " ♻️ " : " \uD83D\uDDD1 ");

        sb.append(status);

        String share = "📥";
        entities.add(
                Map.of(
                        "type", "text_link",
                        "offset", sb.length(),
                        "length", share.length(),
                        "url", "https://t.me/" + botUsername + "?start=share_" + orgInfo.getInn()
                )
        );
        sb.append(share).append(" ");

        if (org.isFilled()) {
            String done = "☑️";
            entities.add(
                    Map.of(
                            "type", "text_link",
                            "offset", sb.length(),
                            "length", done.length(),
                            "url", "https://t.me/" + botUsername + "?start=done_" + orgInfo.getInn()
                    )
            );
            sb.append(done).append(" ");
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
                        "url", "https://t.me/" + botUsername + "?start=edit_" + orgInfo.getInn()
                )
        );
        sb.append(edit);

        String delete = "❌";
        entities.add(
                Map.of(
                        "type", "text_link",
                        "offset", sb.length(),
                        "length", delete.length(),
                        "url", "https://t.me/" + botUsername + "?start=delete_" + orgInfo.getInn()
                )
        );
        sb.append(delete).append("\n");
    }

    private void sendAgreementKeyboard(Long id, String ins, String orgName) {
        System.out.println("!!!");
        switch (ins) {
            case "done" ->
                    sendService.send(Utils.agreementKeyboard(id, orgName + "dagi konteyneri bo'shadimi?"), "sendMessage");
            case "edit" ->
                    sendService.send(Utils.agreementKeyboard(id, orgName + "ning ma'lumotlarini o'zgartirmoqchimisiz?"), "sendMessage");
            case "delete" ->
                    sendService.send(Utils.agreementKeyboard(id, orgName + "ni butunlay o'chirmoqchimisiz?"), "sendMessage");
        }
    }
}
