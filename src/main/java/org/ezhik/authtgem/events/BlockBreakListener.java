package org.ezhik.authtgem.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent; // Импортируем правильное событие

public class BlockBreakListener implements Listener { // Переименовали класс для ясности
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) { // Используем BlockBreakEvent
        if (FreezerEvent.isFreeze(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}