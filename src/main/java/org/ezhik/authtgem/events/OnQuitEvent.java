package org.ezhik.authtgem.events;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ezhik.authtgem.IPManager;
import org.ezhik.authtgem.User;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OnQuitEvent implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!FreezerEvent.isFreeze(event.getPlayer())) {
            String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
            IPManager.addIP(ip);

            // Получаем пользователя
            User user = User.getUser(event.getPlayer().getUniqueId());
            if (user != null) {
                // Получаем текущее время в формате yyyy-MM-dd HH:mm
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String lastActivity = sdf.format(new Date());

                // Загружаем файл пользователя
                File file = new File("plugins/Minetelegram/users/" + user.uuid + ".yml");
                YamlConfiguration userconfig = YamlConfiguration.loadConfiguration(file);

                // Сохраняем время последней активности
                userconfig.set("last_activity", lastActivity);

                // Сохраняем изменения в файл
                try {
                    userconfig.save(file);
                } catch (IOException e) {
                    System.out.println("Error saving config file: " + e);
                }
            }
        }
    }
}
