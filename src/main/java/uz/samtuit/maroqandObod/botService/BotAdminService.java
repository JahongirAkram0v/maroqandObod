package uz.samtuit.maroqandObod.botService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.Admin;
import uz.samtuit.maroqandObod.model.AdminState;
import uz.samtuit.maroqandObod.model.OrgInfo;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.OrgInfoService;

@Component
@RequiredArgsConstructor
public class BotAdminService {

    private final AdminService adminService;
    private final OrgInfoService orgInfoService;
    private final SendService sendService;

    public void handleAdminMessage(Admin admin, String text) {
        AdminState adminState = admin.getState();

        switch (adminState) {
            case SETUP -> {
                if (text.equals("add")) {
                    admin.setState(AdminState.CREATE_ORG);
                    adminService.save(admin);
                    sendService.send(Utils.text(admin.getId(), "Tashkilot ma'lumotlarini yuboring (INN, parol, nom)"),
                            "sendMessage");
                    return;
                }
                if (text.equals("all")) {
                    StringBuilder sb = new StringBuilder();
                    orgInfoService.findAll().forEach(
                            orgInfo -> sb.append(orgInfo.getName()).append(orgInfo.getOrg().isFilled()?" y\n":" n\n")
                    );
                    sendService.send(Utils.adminKeyboard(
                                    admin.getId(),
                                    sb.toString()),
                            "sendMessage");
                    return;
                }
                if (text.startsWith("/start ")) {
                    String[] parts = text.substring(text.indexOf(" ")+1).split("_");
                    if (parts.length == 2) {
                        System.out.println(parts[0]);
                        System.out.println(parts[1]);
                        return;
                    }
                }
                if (text.equals("/start")) {
                    sendService.send(Utils.adminKeyboard(
                                    admin.getId(),
                                    "Xush kelibsiz!"),
                            "sendMessage");
                }
            }
            case CREATE_ORG -> {
                String[] texts = text.split("\n");
                if (texts.length != 3) {
                    sendService.send(Utils.adminKeyboard(
                                    admin.getId(),
                                    "Noto'g'ri format."),
                            "sendMessage");
                    return;
                }
                OrgInfo orgInfo = OrgInfo.builder()
                        .inn(texts[0].trim())
                        .password(texts[1].trim())
                        .name(texts[2].trim())
                        .build();
                orgInfoService.save(orgInfo);
                admin.setState(AdminState.SETUP);
                adminService.save(admin);
                sendService.send(Utils.adminKeyboard(
                                admin.getId(),
                                "Tashkilot qo'shildi."),
                        "sendMessage");
            }
        }
    }
}
