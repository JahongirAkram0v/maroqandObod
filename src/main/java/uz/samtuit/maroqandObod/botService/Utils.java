package uz.samtuit.maroqandObod.botService;

import java.util.List;
import java.util.Map;

public class Utils {

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
                "reply_markup", Map.of(
                        "keyboard", keyboard,
                        "resize_keyboard", true,
                        "one_time_keyboard", true
                )
        );
    }

    public static Map<String, Object> contact(Long id, String text) {
        return text(id, text, List.of(
                    List.of(
                            Map.of(
                                    "text", "📞 Telefon raqamni yuborish",
                                    "request_contact", true
                            )
                    )
                )
        );
    }

    public static Map<String, Object> location(Long id, String text) {
        return text(id, text, List.of(
                        List.of(
                                Map.of(
                                        "text", "📍 Joylashuvni yuborish",
                                        "request_location", true
                                )
                        )
                )
        );
    }

    public static Map<String, Object> adminKeyboard(Long id, String text) {
        return text(id, text, List.of(
                        List.of(
                                Map.of(
                                        "text", "all"
                                ),
                                Map.of(
                                        "text", "add"
                                )
                        )
                )
        );
    }

    public static Map<String, Object> orgKeyboard(Long id, String text, boolean isFilled) {
        return text(id, text, List.of(
                        List.of(
                                Map.of(
                                        "text", isFilled ? "to'la" : "bo'sh"
                                )
                        )
                )
        );
    }

}
