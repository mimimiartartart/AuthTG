package org.ezhik.authtgem.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ezhik.authtgem.IPManager;

public class OnQuitEvent implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!FreezerEvent.isFreeze(event.getPlayer())) {
            String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
            IPManager.addIP(ip);
        }
    }
}
