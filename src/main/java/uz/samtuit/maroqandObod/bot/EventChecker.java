package uz.samtuit.maroqandObod.bot;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.service.EventService;

@Component
@RequiredArgsConstructor
public class EventChecker {

    private final Dotenv dotenv = Dotenv.load();
    private final Long adminId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));

    private final EventService eventService;
    private final SendService sendService;

    @Scheduled(cron = "30 0 17 * * MON-SAT", zone = "Asia/Tashkent")
    public void checkEvents() {
        Long count = eventService.count();
        if (count == 0) return;
        String text = "⚠️ " + count + "ta tashkilotda chiqindi bor! ⚠️";
        sendService.send(Utils.text(adminId, text), "sendMessage");
    }
}
