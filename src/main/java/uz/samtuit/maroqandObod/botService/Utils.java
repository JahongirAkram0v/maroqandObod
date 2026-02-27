package uz.samtuit.maroqandObod.botService;

import java.util.List;
import java.util.Map;

public class Utils {

    public static Map<String, Object> error(String callbackQueryId, String error) {
        return Map.of(
                "callback_query_id", callbackQueryId,
                "text", error,
                "show_alert", true
        );
    }

    public static Map<String, Object> text(Long id, String text) {
        return Map.of(
                "chat_id", id,
                "text", text
        );
    }

    public static Map<String, Object> text(Long id, String text, List<List<Map<String, Object>>> keyboard) {
        return Map.of(
                "chat_id", id,
                "text", text,
                "reply_markup", Map.of("reply_keyboard", keyboard)
        );
    }

    public static Map<String, Object> contact(Long id, String text) {
        return Map.of(
                "chat_id", id,
                "text", text,
                "reply_markup", Map.of(
                        "keyboard", List.of(
                                List.of(
                                        Map.of(
                                                "text", "📞 Kontaktni yuborish",
                                                "request_contact", true
                                        )
                                )
                        ),
                        "resize_keyboard", true,
                        "one_time_keyboard", true
                )
        );
    }

    public static Map<String, Object> location(Long id, String text) {
        return Map.of(
                "chat_id", id,
                "text", text,
                "reply_markup", Map.of(
                        "keyboard", List.of(
                                List.of(
                                        Map.of(
                                                "text", "📍 Joylashuvni yuborish",
                                                "request_location", true
                                        )
                                )
                        ),
                        "resize_keyboard", true,
                        "one_time_keyboard", true
                )
        );
    }

}
