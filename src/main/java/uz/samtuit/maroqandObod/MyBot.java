package uz.samtuit.maroqandObod;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.samtuit.maroqandObod.botService.SingUpService;
import uz.samtuit.maroqandObod.botService.SuperAdminService;
import uz.samtuit.maroqandObod.model.Admin;
import uz.samtuit.maroqandObod.model.Org;
import uz.samtuit.maroqandObod.service.AdminService;
import uz.samtuit.maroqandObod.service.OrgService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MyBot extends TelegramWebhookBot {

    private final Dotenv dotenv = Dotenv.load();
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");
    private final String botWebhookPath = dotenv.get("TELEGRAM_BOT_WEBHOOK_PATH");
    private final String adminId = dotenv.get("TELEGRAM_ADMIN_ID");

    private final OrgService orgService;
    private final AdminService adminService;//
    private final SuperAdminService superAdminService;
    private final SingUpService singUpService;

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
                    superAdminService.handleAdminMessage(admin, text);
                }
            }
            else {
                Optional<Org> optionalOrg = orgService.findByChatId(chatId);
                if (optionalOrg.isPresent()) {
                    Org org = optionalOrg.get();

                    singUpService.checkAndHandleOrgMessage(org, message);
                    //
                     //nimalar qilishi kerak
                    String text = message.getText();

                    switch (text) {
                        case "To'ldi" -> {
                            org.setFilled(true);
                            orgService.save(org);
                            // show message about successful filling and show main menu
                        }
                        case "Tahrir" -> {
                            //boshqa klaviatura yuboraman.
                        }
                        case "phone number" -> {
                            org.setPhoneNumber(null);
                            orgService.save(org);
                            // show request for phone number
                        }
                        case "location" -> {
                            org.setLatitude(null);
                            org.setLongitude(null);
                            orgService.save(org);
                            // show request for location
                        }
                        case "image" -> {
                            org.setImageId(null);
                            orgService.save(org);
                            // show request for image
                        }
                        case "container count" -> {
                            org.setContainerCount(0);
                            orgService.save(org);
                            // show request for container count
                        }
                    }
                }
                else {
                    String text = message.getText();
                    if (text.startsWith("/start")) {
                        //show welcome message
                        return null;
                    }
                    String[] texts = text.split("\n");
                    if (texts.length != 2) {
                        //show error message and welcome message
                        return null;
                    }
                    Optional<Org> newOptionalOrg = orgService.findByInn(texts[0]);
                    if (newOptionalOrg.isEmpty()) {
                        //show error message
                        return null;
                    }
                    Org org = newOptionalOrg.get();
                    if (!org.getPassword().equals(texts[1])) {
                        //show error message
                        return null;
                    }
                    org.setChatId(chatId);
                    orgService.save(org);
                    // welcome message and phone number message
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
