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

    public static Map<String, Object> textEntity(Long id, String text, List<Map<String, Object>> entities) {
        return Map.of(
                "chat_id", id,
                "text", text,
                "entities", entities,
                "disable_web_page_preview", true
        );
    }

    public static Map<String, Object> text(Long id, String text, List<List<Map<String, Object>>> keyboard) {
        return Map.of(
                "chat_id", id,
                "text", text,
                "reply_markup", Map.of(
                        "keyboard", keyboard,
                        "resize_keyboard", true,
                        "one_time_keyboard", false
                )
        );
    }

    public static Map<String, Object> removeKeyboard(Long id, String text) {
        return Map.of(
                "chat_id", id,
                "text", text,
                "reply_markup", Map.of(
                        "remove_keyboard", true
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
        ));
    }

    public static Map<String, Object> location(Long id, String text) {
        return text(id, text, List.of(
                        List.of(
                                Map.of(
                                        "text", "📍 Joylashuvni yuborish",
                                        "request_location", true
                                )
                        )
        ));
    }

    public static Map<String, Object> adminKeyboard(Long id, String text) {
        return text(id, text, List.of(
                        List.of(
                                Map.of(
                                        "text", "\uD83D\uDCCB Tashkilotlar ro‘yxati"
                                ),
                                Map.of(
                                        "text", "\uD83D\uDDC2 Tashkilot qo‘shish"
                                )
                        )
        ));
    }

    public static Map<String, Object> orgKeyboard(Long id, String text, boolean isFilled) {
        return text(id, text, List.of(
                        List.of(
                                Map.of(
                                        "text", isFilled ? "to'la" : "bo'sh"
                                )
                        )
        ));
    }

    public static Map<String, Object> agreementKeyboard(Long id, String text) {
        return text(id, text, List.of(
                        List.of(
                                Map.of(
                                        "text", "Yes"
                                ),
                                Map.of(
                                        "text", "No"
                                )
                        )
        ));
    }

}
