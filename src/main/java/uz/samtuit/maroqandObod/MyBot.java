package uz.samtuit.maroqandObod;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.samtuit.maroqandObod.botService.*;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.UserService;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

@Component
@RequiredArgsConstructor
public class MyBot extends TelegramWebhookBot {

    private final Dotenv dotenv = Dotenv.load();
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");
    private final String botWebhookPath = dotenv.get("TELEGRAM_BOT_WEBHOOK_PATH");
    private final String adminId = dotenv.get("TELEGRAM_ADMIN_ID");

    private final UserService userService;
    private final AuthService authService;

    private final AdminService adminService;//
    private final BotAdminService botAdminService;

    private final SendService sendService;
    private final BotUserService botUserService;

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (!update.hasMessage()) return null;

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();

        if (adminId.equals(chatId.toString())) {
            if (!message.hasText()) return null;

            Admin admin = adminService.findById(chatId)
                        .orElseGet(() -> {
                            Admin a = Admin.builder()
                                    .id(chatId)
                                    .build();
                            adminService.save(a);
                            return a;
                        });
            botAdminService.handle(admin, message.getText());
            return null;
        }

        User user = userService.findByChatId(chatId)
                .orElseGet(() -> {
                    User u = User.builder()
                            .chatId(chatId)
                            .build();
                    userService.save(u);
                    return u;
                });
        if (!user.isAuth()) {
            if (message.hasText() && text.equals("/start")) {
                sendService.send(Utils.text(chatId, START_TEXT), "sendMessage");
                sendService.send(Utils.text(chatId, AUTH_VALID), "sendMessage");
                return null;
            }

            authService.auth(user, message);
            return null;
        }

        botUserService.handle(user, message);

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