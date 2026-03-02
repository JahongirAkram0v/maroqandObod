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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uz.samtuit.maroqandObod.config.KeyboardNameConfig.CONTACT;

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
                    Admin admin = adminService.findById(chatId)
                            .orElseGet(() -> {
                                Admin a = Admin.builder()
                                        .id(chatId)
                                        .build();
                                adminService.save(a);
                                return a;
                            });
                    if (message.hasText()) {
                        botAdminService.handleAdminMessage(admin, message.getText());
                    }
                }
                return null;
            }

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
                if (!message.hasText()) return null;
                String text = message.getText();
                if (text.equals("/start")) {
                    sendService.send(Utils.text(chatId, "Assalomu alaykum, botga xush kelibsiz!"), "sendMessage");
                    String msg =
                            """
                                    📝 Ro‘yxatdan o‘tish uchun INN va parolni quyidagi formatda kiriting:
                                    
                                    123456789
                                    password
                                    """;
                    sendService.send(Utils.text(chatId, msg), "sendMessage");
                    return null;
                }
                String[] texts = text.split("\n");
                if (texts.length != 2) {
                    sendService.send(Utils.text(
                                    chatId,
                                    "Iltimos, INN va parolni to'g'ri formatda kiriting"),
                            "sendMessage");
                    return null;
                }
                Optional<OrgInfo> newOptionalOrgInfo = orgInfoService.findByInnAndPassword(texts[0], texts[1]);
                if (newOptionalOrgInfo.isEmpty()) {
                    sendService.send(Utils.text(
                                    chatId,
                                    "INN yoki parol noto'g'ri, iltimos qayta urinib ko'ring"),
                            "sendMessage");
                    return null;
                }
                OrgInfo orgInfo = newOptionalOrgInfo.get();
                if (orgInfo.getOrg() == null) {
                    Org org = Org.builder()
                            .orgInfo(orgInfo)
                            .chatId(chatId)
                            .build();
                    orgService.save(org);
                } else {
                    Org org = orgInfo.getOrg();
                    org.setChatId(chatId);
                    orgService.save(org);
                }


                String contactText = "✅ Tasdiqlandi. Iltimos, telefon raqamingizni yuboring yoki" +
                        " “\uD83D\uDCF2 Kontaktni ulashish” tugmasini bosing.";
                sendService.send(Utils.text(chatId, contactText,
                        List.of(List.of(Map.of("text", CONTACT,"request_contact", true))
                )), "sendMessage");
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