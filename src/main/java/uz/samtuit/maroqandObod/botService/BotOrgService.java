package uz.samtuit.maroqandObod.botService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.service.OrgService;

@Component
@RequiredArgsConstructor
public class BotOrgService {

    private final Dotenv dotenv = Dotenv.load();
    private final Long adminChatId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));

    private final SendService sendService;
    private final OrgService orgService;


    public void handleOrgMessage(Org org, String text) {
        if (org.isFilled()) {
            sendService.send(Utils.text(org.getChatId(), "Ma'lumotlar yuborilgan"), "sendMessage");
            return;
        }
        org.setFilled(true);
        orgService.save(org);
        sendService.send(Utils.orgKeyboard(org.getChatId(), "Ma'llumotlar yuborildi.", true), "sendMessage");
        sendService.send(Utils.text(adminChatId,  org.getOrgInfo().getName()+" da konteyner toldi"), "sendMessage");
    }
}
