package uz.samtuit.maroqandObod.bot;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.User;
import uz.samtuit.maroqandObod.model.UserRole;
import uz.samtuit.maroqandObod.service.EventService;
import uz.samtuit.maroqandObod.service.UserService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventChecker {

    private final Dotenv dotenv = Dotenv.load();
    private final Long adminId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));

    private final EventService eventService;
    private final UserService userService;
    private final AdminStat adminStat;
    private final SendService sendService;

    @Scheduled(cron = "30 0 17 * * MON-SAT", zone = "Asia/Tashkent")
    public void checkEvents() {
        Long count = eventService.count();
        if (count == 0) return;
        String text = "⚠️ " + count + "ta tashkilotda chiqindi bor! ⚠️";
        sendService.send(Utils.text(adminId, text), "sendMessage");
    }

    @Scheduled(cron = "0 30 7 1 * *", zone = "Asia/Tashkent")
    public void updateStat() {
        List<User> users = userService.findAll();
        for (User user : users) {
            int[] s = user.getS();
            UserRole role = user.getRole();
            //TODO:teginmay tursammikan
            if (role == UserRole.USER) {
                s[0] = s[1];
            } else {
                s[0] = s[3];
                s[1] = s[4];
                s[2] = s[5];
            }
            user.setS(s);
            userService.save(user);
        }
        adminStat.handle();
    }
}
