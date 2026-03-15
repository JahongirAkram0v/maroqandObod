package uz.samtuit.maroqandObod.bot;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import uz.samtuit.maroqandObod.model.*;
import uz.samtuit.maroqandObod.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

@Component
@RequiredArgsConstructor
public class BotUserService {

    private final Dotenv dotenv = Dotenv.load();
    private final Long adminId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");

    private final SendService sendService;
    private final UserService userService;

    public void handle(User user, Message message) {
        UserState state = user.getState();

        switch (state) {
            case READY -> {
                if (!message.hasText()) return;
                String text = message.getText();
                if (text.equals("/start")) {
                    sendService.send(Utils.text(user.getChatId(), START_TEXT,
                            List.of(List.of(Map.of("text", FULL)))
                    ), "sendMessage");
                    return;
                }
                if (!text.equals(FULL)) return;
                sendService.send(Utils.agreement(user.getChatId(), AGREEMENT), "sendMessage");
                user.setState(UserState.CHOOSE);
                userService.save(user);
            }
            case CHOOSE -> {
                if (!message.hasText()) return;
                String text = message.getText();
                if (text.equals(YES)) {
                    user.setState(UserState.IMAGE);
                    userService.save(user);
                    sendService.send(Utils.remove(user.getChatId(), IMAGE_TEXT), "sendMessage");
                }
                else if (text.equals(NO)) {
                    user.setState(UserState.READY);
                    userService.save(user);
                    sendService.send(Utils.text(user.getChatId(), NO_TEXT,
                            List.of(List.of(Map.of("text", FULL)))
                    ), "sendMessage");
                }
            }
            case IMAGE -> {
                if (!message.hasPhoto()) return;
                String imageId = message.getPhoto().getLast().getFileId();

                Event event = new Event();
                event.setImageId(imageId);
                user.setEvent(event);
                //TODO: bu yerni ham o'ylab ko'raman
                if (user.getRole() == UserRole.USER) {
                    user.setState(UserState.LOCATION);
                    userService.save(user);
                    sendService.send(Utils.text(user.getChatId(), LOC_TEXT,
                            List.of(List.of(Map.of("text", LOCATION_ORG,"request_location", true)))
                    ), "sendMessage");
                    return;
                }
                user.setState(UserState.COUNT);
                userService.save(user);
                sendService.send(Utils.text(user.getChatId(), COUNT_TEXT), "sendMessage");
            }
            case COUNT -> {
                if (!message.hasText()) return;
                String text = message.getText();
                try {
                    int containerCount = Integer.parseInt(text);
                    if (containerCount > 0 && containerCount < 10) {
                        Optional<Event> optionalEvent = userService.findEventByUserId(user.getId());
                        if (optionalEvent.isEmpty()) {
                            sendService.send(Utils.text(user.getChatId(), NOT_FOUND_ERROR), "sendMessage");
                            return;
                        }
                        Event event = optionalEvent.get();
                        event.setCount(containerCount);
                        user.setState(UserState.LOCATION);
                        userService.save(user);
                        sendService.send(Utils.text(user.getChatId(), LOC_TEXT,
                                List.of(List.of(Map.of("text", LOCATION_ORG,"request_location", true)))
                        ), "sendMessage");
                    } else {
                        sendService.send(Utils.text(user.getChatId(), COUNT_ERROR), "sendMessage");
                    }
                } catch (NumberFormatException e) {
                    sendService.send(Utils.text(user.getChatId(), COUNT_ERROR), "sendMessage");
                }
            }
            case LOCATION -> {
                if (!message.hasLocation()) return;
                Location location = message.getLocation();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Optional<Event> optionalEvent = userService.findEventByUserId(user.getId());
                if (optionalEvent.isEmpty()) {
                    sendService.send(Utils.text(user.getChatId(), NOT_FOUND_ERROR), "sendMessage");
                    return;
                }
                Event event = optionalEvent.get();
                event.setLatitude(latitude);
                event.setLongitude(longitude);
                event.markCreatedNow();
                user.setState(UserState.FULL);
                userService.save(user);
                sendService.send(Utils.remove(user.getChatId(), DIS_TEXT), "sendMessage");

                StringBuilder sb = new StringBuilder();
                List<Map<String, Object>> entities = new ArrayList<>();
                Optional<String> optionalName = userService.findUserNameById(user.getId());
                if (optionalName.isEmpty()) {
                    sendService.send(Utils.text(user.getChatId(), NOT_FOUND_ERROR), "sendMessage");
                    return;
                }
                sb.append(optionalName.get()).append(" konteynerlar to'ldi ");
                String share = "📥";
                entities.add(
                        Map.of(
                                "type", "text_link",
                                "offset", sb.length(),
                                "length", share.length(),
                                "url", "https://t.me/" + botUsername + "?start=share_" + user.getId()
                        )
                );
                sb.append(share);
                sendService.send(Utils.textEntity(adminId, sb.toString(), entities), "sendMessage");
            }
        }

    }
}
