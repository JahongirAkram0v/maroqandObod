package uz.samtuit.maroqandObod.bot;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.AuthUserDto;
import uz.samtuit.maroqandObod.service.UserService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminStat {

    private final Dotenv dotenv = Dotenv.load();
    private final Long adminId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));

    private final UserService userService;
    private final SendService sendService;

    public void handle() {
        List<AuthUserDto> users = userService.findAllAuthWithUserInfo();
        StringBuilder sb = new StringBuilder();
        if (users.isEmpty()) {
            return;
        }
        for (AuthUserDto user : users) {
            int[] stat = user.getS();
            sb.append(user.getName()).append(": ");
            sb.append(stat[0]).append(" ").append(stat[1]).append(" ").append(stat[2]);
            sb.append("\n");
        }
        sendService.send(Utils.text(adminId, sb.toString()), "sendMessage");
    }
}
