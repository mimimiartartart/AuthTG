package org.ezhik.authtgem.events;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.ezhik.authtgem.AuthTGEM;
import org.ezhik.authtgem.LoginManager;
import org.ezhik.authtgem.User;
import org.ezhik.authtgem.IPManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OnJoinEvent implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            Player p = event.getPlayer();


            String ip = p.getAddress().getAddress().getHostAddress();
            // Проверяем, если IP был использован в течение последних 15 минут
            if (IPManager.isRecent(ip)) {
                // Снимаем ограничения (разморозка игрока)
                FreezerEvent.unfreezeplayer(p.getName());
                MuterEvent.unmute(p.getName());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lAuthTG&f&l] &a&lВы повторно подключились с того же IP. Повторная авторизация не требуется."));
                return; // Пропускаем дальнейшую авторизацию
            }
            if (LoginManager.isRecent(p.getName())) {
                FreezerEvent.unfreezeplayer(p.getName());
                MuterEvent.unmute(p.getName());
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lAuthTG&f&l] &a&lВы подтвердили вход в боте. Авторизация не требуется."));
                User user = User.getUser(p.getUniqueId());
                String UserIP = event.getPlayer().getAddress().getAddress().getHostAddress();

                String geoLocation = getGeoLocation(UserIP);
                user.sendLoginQuestion("Вы зашли на аккаунт. IP адрес - " + UserIP + " " + geoLocation + " \nЕсли это были не вы, нажмите кнопку ниже.");
                LoginManager.clearLogin(p.getName());
                return;
            }
            YamlConfiguration userconfig = new YamlConfiguration();
            File file = new File("plugins/Minetelegram/users/" + p.getUniqueId() + ".yml");
            FreezerEvent.freezeplayer(p.getName());
            if (!file.exists()) {
                MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', "&a&lПривяжите аккаунт к Telegram"));
                p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&f&lПривяжите аккаунт"), "к Telegram", 20, 10000000, 0);
                try {
                    userconfig.load(file);
                } catch (IOException e) {
                    System.out.println("Error loading config file: " + e);
                } catch (InvalidConfigurationException e) {
                    System.out.println("Error parsing config file: " + e);
                }
                userconfig.set("playername", p.getName());
                userconfig.set("ChatID", null);
                try {
                    userconfig.save(file);
                } catch (IOException e) {
                    System.out.println("Error saving config file: " + e);
                }
            } else {
                try {
                    userconfig.load(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InvalidConfigurationException e) {
                    throw new RuntimeException(e);
                }
                if (userconfig.getBoolean("active")) {
                    MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', "&a&lПодтвердите вход через Telegram"));
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&f&lПодтвердите вход"), "через Telegram", 20, 10000000, 0);

                    User user = User.getUser(p.getUniqueId());
                    String UserIP = event.getPlayer().getAddress().getAddress().getHostAddress();

                    String geoLocation = getGeoLocation(UserIP);
                    user.sendLoginAccepted("На ваш аккаунт зашли. IP адрес - " + UserIP + " " + geoLocation + " \nЭто были вы?");
                } else {
                    MuterEvent.mute(p.getName(), ChatColor.translateAlternateColorCodes('&', "&a&lПривяжите аккаунт к Telegram"));
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&f&lПривяжите аккаунт"), "к Telegram", 20, 10000000, 0);
                    try {
                        userconfig.load(file);
                    } catch (IOException e) {
                        System.out.println("Error loading config file: " + e);
                    } catch (InvalidConfigurationException e) {
                        System.out.println("Error parsing config file: " + e);
                    }
                    userconfig.set("playername", p.getName());
                    try {
                        userconfig.save(file);
                    } catch (IOException e) {
                        System.out.println("Error saving config file: " + e);
                    }

                }
            }
        } catch (Exception e){
            System.out.println("AuthTG err OnJoin");
        }


    }
    public static String getGeoLocation(String ipAddress) {
        try {
            String url = "http://ip-api.com/json/" + ipAddress + "?fields=status,country,regionName,city";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Парсим JSON-ответ
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            if ("success".equals(jsonResponse.get("status").getAsString())) {
                String country = jsonResponse.get("country").getAsString();
                String region = jsonResponse.get("regionName").getAsString();
                String city = jsonResponse.get("city").getAsString();
                return String.format("(%s, %s, %s)", country, region, city);
            } else {
                return "(Местоположение не определено)";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "(Ошибка при определении местоположения)";
        }
    }
    // Метод для получения внешнего IP
    public static String getExternalIP() {
        try {
            String url = "https://api.ipify.org?format=json"; // Используем ipify API
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Парсим JSON
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            return jsonResponse.get("ip").getAsString(); // Получаем IP
        } catch (Exception e) {
            e.printStackTrace();
            return "Не удалось получить внешний IP";
        }
    }


}