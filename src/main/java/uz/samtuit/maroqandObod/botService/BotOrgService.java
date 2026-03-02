package uz.samtuit.maroqandObod.botService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.model.OrgInfo;
import uz.samtuit.maroqandObod.service.OrgService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uz.samtuit.maroqandObod.config.KeyboardNameConfig.*;

@Component
@RequiredArgsConstructor
public class BotOrgService {

    private final Dotenv dotenv = Dotenv.load();
    private final Long adminChatId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");

    private final SendService sendService;
    private final OrgService orgService;


    public void handleOrgMessage(Org org, String text) {
        if (org.isFilled()) {
            sendService.send(Utils.text(org.getChatId(), "\uD83D\uDCEC Ma’lumotlar qabul qilingan."), "sendMessage");
            return;
        }
        switch (text) {
            case EMPTY_CON -> sendService.send(Utils.agreementKeyboard(org.getChatId(), "\uD83D\uDD3D Iltimos, Konteynerlar to'lganligini haqida ma'lumot berish uchun quyidagi tugmalardan birini bosing."), "sendMessage");
            case YES -> {
                org.setFilled(true);
                orgService.save(org);
                sendService.send(Utils.org(org.getChatId(), "\uD83D\uDCE8 Konteynerlar to'lgankigi haqidagi ma'lumotlar dispetcherga yuborildi.", true), "sendMessage");

                OrgInfo orgInfo = org.getOrgInfo();
                StringBuilder sb = new StringBuilder();
                sb.append(org.getOrgInfo().getName()).append("da konteynerlar toldi ");
                List<Map<String, Object>> entities = new ArrayList<>();
                String share = "📥";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", share.length(),
                                "url", "https://t.me/" + botUsername + "?start=share_" + orgInfo.getId().trim()
                        )
                );
                sb.append(share);
                sendService.send(
                        Utils.textEntity(adminChatId, sb.toString(), entities),
                        "sendMessage"
                );
            }
            case NO -> sendService.send(
                    Utils.org(org.getChatId(), "\uD83D\uDCCC Jarayon o‘zgarishsiz qoldirildi.", false),
                    "sendMessage");
        }
    }
}
