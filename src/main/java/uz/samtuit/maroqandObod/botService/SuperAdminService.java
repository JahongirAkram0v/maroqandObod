package uz.samtuit.maroqandObod.botService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.Admin;
import uz.samtuit.maroqandObod.model.AdminState;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.OrgService;

@Component
@RequiredArgsConstructor
public class SuperAdminService {

    private final AdminService adminService;
    private final OrgService orgService;

    public void handleAdminMessage(Admin admin, String text) {
        AdminState adminState = admin.getState();
        System.out.println(adminState);

        switch (adminState) {
            case SETUP -> {
                if (text.equals("add")) {
                    admin.setState(AdminState.CREATE_ORG);
                    adminService.save(admin);
                    System.out.println("add");
                    return;
                }
                System.out.println("boshqaruv klaviaturasini ko'rsatish");
            }
            case CREATE_ORG -> {
                String[] texts = text.split("\n");
                if (texts.length != 3) {
                    System.out.println("Noto'g'ri format.");
                    return;
                }
                Org org = Org.builder()
                        .inn(texts[0].trim())
                        .password(texts[1].trim())
                        .name(texts[2].trim())
                        .build();
                orgService.save(org);
                admin.setState(AdminState.SETUP);
                adminService.save(admin);
                System.out.println("f");
            }
        }
    }
}
