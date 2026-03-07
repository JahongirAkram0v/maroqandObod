package uz.samtuit.maroqandObod;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final MyBot myBot;

    @PostMapping("/webhook")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return myBot.consumeUpdate(update);
    }
}