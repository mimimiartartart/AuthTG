package org.ezhik.authtgem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IPManager {
    // Хэш-карта для хранения IP и времени отключения (в миллисекундах)
    private static final HashMap<String, Long> ipMap = new HashMap<>();

    // Время в миллисекундах (15 минут)
    private static final long TIME_LIMIT = 15 * 60 * 1000;

    // Добавить IP и текущее время
    public static void addIP(String ip) {
        ipMap.put(ip, System.currentTimeMillis());
    }

    // Проверить, есть ли IP в пределах 15 минут
    public static boolean isRecent(String ip) {
        Long lastTime = ipMap.get(ip);
        if (lastTime == null) {
            return false;
        }
        return (System.currentTimeMillis() - lastTime) <= TIME_LIMIT;
    }

    // Очистка устаревших записей
    public static void cleanUp() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = ipMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if ((currentTime - entry.getValue()) > TIME_LIMIT) {
                iterator.remove();
            }
        }
    }
}
