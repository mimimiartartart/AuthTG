package org.ezhik.authtgem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TimeUtils {

    // Метод для получения времени, прошедшего с последнего выхода
    public static String getTimeAgo(String lastActivity) {
        if (lastActivity == null) {
            return "Неизвестно";
        }

        try {
            // Преобразуем строку времени в объект Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date lastLoginDate = sdf.parse(lastActivity);

            // Получаем текущее время
            long currentTime = System.currentTimeMillis();

            // Получаем разницу в миллисекундах
            long diffInMillis = currentTime - lastLoginDate.getTime();

            // Преобразуем разницу в более удобный формат (дни, часы, минуты)
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis) - TimeUnit.DAYS.toHours(days);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);

            StringBuilder result = new StringBuilder();

            // Форматируем строку в зависимости от времени
            if (days > 0) {
                result.append(days).append(" d");
                if (hours > 0) result.append(" ").append(hours).append(" h");
            } else if (hours > 0) {
                result.append(hours).append(" h");
                if (minutes > 0) result.append(" ").append(minutes).append(" min");
            } else if (minutes > 0) {
                result.append(minutes).append(" min");
            } else {
                result.append("<1 min");
            }

            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка расчета времени";
        }
    }
}

