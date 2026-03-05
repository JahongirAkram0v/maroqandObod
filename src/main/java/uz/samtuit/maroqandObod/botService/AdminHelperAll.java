package uz.samtuit.maroqandObod.botService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.samtuit.maroqandObod.model.User;
import uz.samtuit.maroqandObod.model.UserInfo;
import uz.samtuit.maroqandObod.model.UserState;
import uz.samtuit.maroqandObod.service.UserInfoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uz.samtuit.maroqandObod.config.NameConfig.NOT_FOUND_ORG;

@Component
@RequiredArgsConstructor
public class AdminHelperAll {

    private final Dotenv dotenv = Dotenv.load();
    private final String botUsername = dotenv.get("TELEGRAM_BOT_USERNAME");
    private final Long adminId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));

    private final UserInfoService userInfoService;
    private final SendService sendService;

    public void handle() {
        List<UserInfo> userInfos = userInfoService.findAll();
        //sort
        StringBuilder sb = new StringBuilder();
        List<Map<String, Object>> entities = new ArrayList<>();

        if (userInfos.isEmpty()) {
            sendService.send(Utils.text(adminId, NOT_FOUND_ORG),"sendMessage");
            return;
        }

        String title = "Tashkilotlar ro‘yxati";
        sb.append(title).append("\n\n");
        entities.add(Map.of(
                "type", "bold",
                "offset", 0,
                "length", title.length()
        ));

        for (UserInfo userInfo : userInfos) {
            appendUserLine(sb, entities, userInfo);
        }

        sendService.send(
                Utils.textEntity(adminId, sb.toString(), entities),
                "sendMessage"
        );
    }

    private void appendUserLine(
            StringBuilder sb,
            List<Map<String, Object>> entities,
            UserInfo userInfo
    ) {

        String userInfoId = userInfo.getId();
        sb.append(userInfo.getLogin());

        Optional<User> optionalUser = userInfoService.findUserByUserInfoId(userInfoId);
        if (optionalUser.isEmpty()) {
            sb.append("⚠️");
            controller(sb, entities, userInfoId);
            return;
        }
        User user = optionalUser.get();

        if (!user.isAuth()) {
            sb.append(" ⚠️ ");
            controller(sb, entities, userInfoId);
            return;
        }
        sb.append("     ");

        String userId = user.getId();
        boolean isFilled = user.getState() == UserState.FULL;

        String status = (isFilled ? " ♻️ " : " \uD83D\uDDD1 ");
        sb.append(status);

        if (isFilled) {
            String share = "📥";
            entities.add(
                    Map.of(
                            "type", "text_link",
                            "offset", sb.length(),
                            "length", share.length(),
                            "url", "https://t.me/" + botUsername + "?start=share_" + userId
                    )
            );
            sb.append(share).append(" • ");

            String done = "☑️";
            entities.add(
                    Map.of(
                            "type", "text_link",
                            "offset", sb.length(),
                            "length", done.length(),
                            "url", "https://t.me/" + botUsername + "?start=done_" + userId
                    )
            );
            sb.append(done).append(" • ");
        }

        String stat = "\uD83D\uDCCA";
        entities.add(
                Map.of(
                        "type", "text_link",
                        "offset", sb.length(),
                        "length", stat.length(),
                        "url", "https://t.me/" + botUsername + "?start=stat_" + userId
                )
        );
        sb.append(stat).append(" • ");

        controller(sb, entities, userInfoId);
    }

    private void controller(StringBuilder sb, List<Map<String, Object>> entities, String id) {
        String edit = "✏️";
        entities.add(
                Map.of(
                        "type", "text_link",
                        "offset", sb.length(),
                        "length", edit.length(),
                        "url", "https://t.me/" + botUsername + "?start=edit_" + id
                )
        );
        sb.append(edit).append("\n");
    }

}
