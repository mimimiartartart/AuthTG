package org.ezhik.authtgem;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.events.FreezerEvent;
import org.ezhik.authtgem.events.MuterEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CodeCMD implements CommandExecutor {
    public static Map<UUID,String> code = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lAuthTG&f&l] &c&lСинтаксис команды: /code <код из Telegram>"));
        } else {
            Player player = (Player) commandSender;
            YamlConfiguration userconf = new YamlConfiguration();
            File file = new File("plugins/Minetelegram/users/" + player.getUniqueId() + ".yml");
            if (strings[0].equals(code.get(player.getUniqueId()))) {
                try {
                    userconf.load(file);
                } catch (IOException e) {
                    System.out.println("Error loading config file: " + e);
                } catch (InvalidConfigurationException e) {
                    System.out.println("Error parsing config file: " + e);
                }
                userconf.set("active", true);
                code.remove(player.getUniqueId());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lAuthTG&f&l] &a&lУспешная привязка аккаунта"));
                try {
                    userconf.save(file);
                } catch (IOException e) {
                    System.out.println("Error saving config file: " + e);
                }
                User user = User.getUser(player.getUniqueId());
                user.sendMessage("Аккаунт успешно привязан");
                player.resetTitle();
                FreezerEvent.unfreezeplayer(player.getName());
                MuterEvent.unmute(player.getName());

            } else player.sendMessage("&f&l[&b&lAuthTG&f&l] &c&lНеверный код");
        }
        return true;
    }
}
