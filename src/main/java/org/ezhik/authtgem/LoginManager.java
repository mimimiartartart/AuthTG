package org.ezhik.authtgem;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoginManager {
    private static final HashMap<String, Long> loginMap = new HashMap<>();
    private static final HashMap<String, Long> loggedMap = new HashMap<>();

    private static final long TIME_LIMIT = 5 * 60 * 1000;
    public static void addToLogin(String playername) {
        loginMap.put(playername, System.currentTimeMillis());
    }
    public static void addToLogged(String playername) {
        loggedMap.put(playername, System.currentTimeMillis());
    }

    public static boolean isRecent(String playername) {
        Long lastTime = loginMap.get(playername);
        if (lastTime == null) {
            return false;
        }
        return (System.currentTimeMillis() - lastTime) <= TIME_LIMIT;
    }

    public static boolean isRecentLogged(String playername) {
        Long lastTime = loggedMap.get(playername);
        if (lastTime == null) {
            return false;
        }
        return true;
    }

    public static void cleanUp() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = loginMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            String playername = entry.getKey();

            if ((currentTime - entry.getValue()) > TIME_LIMIT) {
                iterator.remove();
            }
        }
    }


    public static void clearLogin(String playername) {
        if (isRecent(playername)) {
            addToLogged(playername);
            loginMap.remove(playername);
        }
    }
    public static void clearLogged(String playername) {
        if (isRecentLogged(playername)) {
            loggedMap.remove(playername);
        }
    }
}
