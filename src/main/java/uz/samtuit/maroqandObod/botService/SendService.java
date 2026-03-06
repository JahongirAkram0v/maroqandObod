package uz.samtuit.maroqandObod.botService;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uz.samtuit.maroqandObod.model.User;
import uz.samtuit.maroqandObod.model.UserState;
import uz.samtuit.maroqandObod.service.UserService;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SendService {

    private final Dotenv dotenv = Dotenv.load();
    private final Long adminId = Long.parseLong(dotenv.get("TELEGRAM_ADMIN_ID"));
    private final String url = dotenv.get("TELEGRAM_BASE_URL") + dotenv.get("TELEGRAM_BOT_TOKEN") + "/";
    private final RestTemplate restTemplate = new RestTemplate();

    private final UserService userService;

    public void send(Map<String, Object> answer, String method) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(answer, headers);

        try {
            restTemplate.postForObject(url + method, request, String.class);
        } catch (HttpClientErrorException e) {
            String rb = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 403 && rb.toLowerCase().contains("blocked")) {
                Long chatId = Long.parseLong(answer.get("chat_id").toString());
                Optional<User> optionalUser = userService.findByChatId(chatId);
                if (optionalUser.isEmpty()) return;
                User user = optionalUser.get();
                user.setState(UserState.BLOCK);
                userService.save(user);
                send(Utils.text(adminId, "Botni blok qilgan ekan"), "sendMessage");
            }
        }
    }
}