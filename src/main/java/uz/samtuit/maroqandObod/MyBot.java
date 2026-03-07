package uz.samtuit.maroqandObod;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.webhook.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;

import uz.samtuit.maroqandObod.botService.*;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.UserService;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

@Component
@RequiredArgsConstructor
public class MyBot implements TelegramWebhookBot {

    private final Dotenv dotenv = Dotenv.load();
    private final String botWebhookPath = dotenv.get("TELEGRAM_BOT_WEBHOOK_PATH");
    private final Long adminId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));
    //
    private final OffsetTime startTime = OffsetTime.of(8, 0, 0, 0, ZoneOffset.of("+05:00"));
    private final OffsetTime endTime = OffsetTime.of(17, 0, 0, 0, ZoneOffset.of("+05:00"));

    private final UserService userService;
    private final AuthService authService;

    private final AdminService adminService;//
    private final BotAdminService botAdminService;

    private final SendService sendService;
    private final BotUserService botUserService;

    @Override
    public BotApiMethod<?> consumeUpdate(Update update) {

        if (update.hasMyChatMember()) {
            ChatMemberUpdated chatMemberUpdated = update.getMyChatMember();
            String newStatus = chatMemberUpdated.getNewChatMember().getStatus();
            Long chatId = chatMemberUpdated.getChat().getId();
            Optional<User> optionalUser = userService.findByChatId(chatId);
            if (optionalUser.isEmpty()) return null;
            User user = optionalUser.get();
            UserState state = newStatus.equals("kicked") ? UserState.BLOCK : UserState.READY;
            user.setState(state);
            userService.save(user);
        }
        if (!update.hasMessage()) return null;

        Message message = update.getMessage();
        Long chatId = message.getChatId();
        String text = message.getText();

        if (adminId.equals(chatId)) {
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
            //delete
            if (message.hasText() && text.equals("/start")) {
                sendService.send(Utils.text(chatId, START_TEXT), "sendMessage");
                sendService.send(Utils.text(chatId, AUTH_VALID), "sendMessage");
                return null;
            }

            authService.auth(user, message);
            return null;
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.of("+05:00"));
        int day = now.getDayOfWeek().getValue();
        OffsetTime time = now.toOffsetTime();
        if (day < 7 && time.isAfter(startTime) && time.isBefore(endTime)) {
            botUserService.handle(user, message);
        } else {
            sendService.send(Utils.text(chatId, TIME_TEXT), "sendMessage");
        }

        return null;
    }

    @Override
    public String getBotPath() {
        return botWebhookPath;
    }

    @Override
    public void runDeleteWebhook() {

    }

    @Override
    public void runSetWebhook() {

    }
}