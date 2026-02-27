package uz.samtuit.maroqandObod;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.samtuit.maroqandObod.botService.*;
import uz.samtuit.maroqandObod.model.Admin;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.model.OrgInfo;
import uz.samtuit.maroqandObod.model.OrgState;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.OrgInfoService;
import uz.samtuit.maroqandObod.service.OrgService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyBot extends TelegramWebhookBot {

    private final Dotenv dotenv = Dotenv.load();
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");
    private final String botWebhookPath = dotenv.get("TELEGRAM_BOT_WEBHOOK_PATH");
    private final String adminId = dotenv.get("TELEGRAM_ADMIN_ID");

    private final AdminService adminService;//
    private final BotAdminService botAdminService;

    private final SendService sendService;

    private final OrgService orgService;
    private final OrgInfoService orgInfoService;

    private final SingUpService singUpService;
    private final BotOrgService botOrgService;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            if (adminId.equals(chatId.toString())) {
                if (message.hasText()) {
                    String text = message.getText();
                    Admin admin = adminService.findById(chatId)
                            .orElseGet(() -> {
                                Admin a = Admin.builder()
                                        .id(chatId)
                                        .build();
                                adminService.save(a);
                                return a;
                            });
                    botAdminService.handleAdminMessage(admin, text);
                }
            }
            else {
                Optional<Org> optionalOrg = orgService.findByChatId(chatId);
                if (optionalOrg.isPresent()) {
                    Org org = optionalOrg.get();
                    OrgState orgState = org.getOrgState();

                    if (orgState != OrgState.READY && orgState != OrgState.BLOCKED) {
                        singUpService.handleOrgMessage(org, message);
                        return null;
                    }
                    if (message.hasText()) {
                        botOrgService.handleOrgMessage(org, message.getText());
                    }

                }
                else {
                    //auth
                    String text = message.getText();
                    if (text.equals("/start")) {
                        sendService.send(Utils.contact(
                                        chatId,
                                        "Iltimos, ro'yxatdan o'tish uchun INN va parolni kiriting"),
                                "sendMessage");
                        return null;
                    }
                    String[] texts = text.split("\n");
                    if (texts.length != 2) {
                        sendService.send(Utils.contact(
                                        chatId,
                                        "Iltimos, INN va parolni to'g'ri formatda kiriting"),
                                "sendMessage");
                        return null;
                    }
                    Optional<OrgInfo> newOptionalOrgInfo = orgInfoService.findByInnAndPassword(texts[0], texts[1]);
                    if (newOptionalOrgInfo.isEmpty()) {
                        sendService.send(Utils.contact(
                                        chatId,
                                        "INN yoki parol noto'g'ri, iltimos qayta urinib ko'ring"),
                                "sendMessage");
                        return null;
                    }
                    OrgInfo orgInfo = newOptionalOrgInfo.get();
                    if (orgInfo.getOrg() != null) {
                        sendService.send(Utils.contact(
                                        chatId,
                                        "Bu INN bilan ro'yxatdan o'tilgan, iltimos boshqa INN bilan urinib ko'ring"),
                                "sendMessage");
                        return null;
                    }
                    Org org = Org.builder()
                            .orgInfo(orgInfo)
                            .build();
                    org.setChatId(chatId);
                    orgService.save(org);
                    sendService.send(Utils.contact(
                            chatId,
                            "Siz tasdiqlandingiz, iltimos, telefon raqamingizni yuboring"),
                            "sendMessage");
                }
            }
        }

        return null;
    }

    @Override
    public String getBotPath() {
        return botWebhookPath;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}