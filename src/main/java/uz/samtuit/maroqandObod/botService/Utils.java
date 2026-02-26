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
                "reply_markup", Map.of("inline_keyboard", keyboard)
        );
    }

}
