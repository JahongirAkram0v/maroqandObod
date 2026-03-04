package uz.samtuit.maroqandObod.botService;

import java.util.List;
import java.util.Map;

import static uz.samtuit.maroqandObod.config.NameConfig.*;

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

    public static Map<String, Object> remove(Long id, String text) {
        return Map.of(
                "chat_id", id,
                "text", text,
                "reply_markup", Map.of(
                        "remove_keyboard", true
                )
        );
    }

    public static Map<String, Object> photo(Long id, String fileId, String text) {
        return Map.of(
                "chat_id", id,
                "photo", fileId,
                "caption", text
        );
    }

    public static Map<String, Object> location(Long id, Double latitude, Double longitude) {
        return Map.of(
                "chat_id", id,
                "latitude", latitude,
                "longitude", longitude
        );
    }

    public static Map<String, Object> admin(Long id, String text) {
        return text(id, text, List.of(
                        List.of(
                                Map.of("text", ALL_ORG),
                                Map.of("text", ADD_ORG)
                        )
        ));
    }

    public static Map<String, Object> agreement(Long id, String text) {
        return text(id, text, List.of(
                        List.of(
                                Map.of("text", YES),
                                Map.of("text", NO)
                        )
        ));
    }

}
