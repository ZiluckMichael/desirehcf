package com.desiremc.hcf.handler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.desiremc.core.utils.BukkitUtils;
import com.desiremc.hcf.DesireHCF;
import com.desiremc.hcf.barrier.TagHandler;

public class EndHandler implements Listener
{

    private static EndHandler instance;
    private Location endExit;
    private Location endSpawn;

    public EndHandler()
    {
        instance = this;
    }

    public static EndHandler getInstance()
    {
        return instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleportMonitor(PlayerTeleportEvent event)
    {
        Bukkit.broadcastMessage("From: " + event.getFrom().getWorld().getEnvironment().name());
        Bukkit.broadcastMessage("To: " + event.getTo().getWorld().getEnvironment().name());

        if (!event.isCancelled() && TagHandler.isTagged(event.getPlayer()))
        {
            TagHandler.setLastValidLocation(event.getPlayer().getUniqueId(), event.getTo());
        }
        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END && event.getFrom().getWorld().getEnvironment() != World.Environment.THE_END)
        {
            event.setTo(getEndSpawn());
        }
        else if (event.getTo().getWorld().getEnvironment() != World.Environment.THE_END && event.getFrom().getWorld().getEnvironment() == World.Environment.THE_END)
        {
            event.setTo(getEndExit());
        }
    }

    public Location getEndExit()
    {
        if (endExit == null)
        {
            endExit = BukkitUtils.toLocation(DesireHCF.getConfigHandler().getString("endexit"));
        }
        return endExit;
    }

    public Location getEndSpawn()
    {
        if (endSpawn == null)
        {
            endSpawn = BukkitUtils.toLocation(DesireHCF.getConfigHandler().getString("endspawn"));
        }
        return endSpawn;
    }
}
