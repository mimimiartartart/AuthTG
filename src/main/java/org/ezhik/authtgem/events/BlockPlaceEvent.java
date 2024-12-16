package org.ezhik.authtgem.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BlockPlaceEvent implements Listener {
    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        if (FreezerEvent.isFreeze(event.getPlayer())) event.setCancelled(true);
    }
}
