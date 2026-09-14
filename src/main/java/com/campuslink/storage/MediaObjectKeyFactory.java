package com.campuslink.storage;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class MediaObjectKeyFactory {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    public String create(MediaScene scene, Long userId, String extension) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        switch (scene) {
            case AVATAR:
                return "avatars/" + userId + "/" + uuid + "." + extension;
            case POST:
                return "posts/" + userId + "/" + LocalDate.now().format(MONTH_FORMATTER) + "/" + uuid + "." + extension;
            case EVENT:
                return "events/" + uuid + "." + extension;
            case PLACE:
                return "places/" + uuid + "." + extension;
            default:
                throw new IllegalArgumentException("Unsupported media scene");
        }
    }
}
