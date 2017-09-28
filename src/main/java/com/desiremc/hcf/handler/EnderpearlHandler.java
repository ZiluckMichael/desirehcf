package com.desiremc.hcf.handler;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.desiremc.core.scoreboard.EntryRegistry;
import com.desiremc.hcf.HCFCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.desiremc.core.DesireCore;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.bukkit.scheduler.BukkitRunnable;

public class EnderpearlHandler implements Listener
{

    private static int TIMER;

    private Cache<UUID, Long> history;

    public EnderpearlHandler()
    {
        TIMER = DesireCore.getConfigHandler().getInteger("enderpearl.time");
        history = CacheBuilder.newBuilder().expireAfterWrite(TIMER, TimeUnit.SECONDS).removalListener(new RemovalListener<UUID, Long>()
        {

            @Override
            public void onRemoval(RemovalNotification<UUID, Long> entry)
            {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null)
                {
                    DesireCore.getLangHandler().sendString(p, "enderpearl.ended");
                    EntryRegistry.getInstance().removeValue(p, DesireCore.getLangHandler().getString("enderpearl.scoreboard"));
                }
            }
        }).build();

        Bukkit.getScheduler().runTaskTimerAsynchronously(HCFCore.getInstance(), new Runnable()
        {
            @Override
            public void run()
            {
                for (UUID uuid : history.asMap().keySet())
                {
                    Player p = Bukkit.getPlayer(uuid);
                    EntryRegistry.getInstance().setValue(p, DesireCore.getLangHandler().getString("enderpearl.scoreboard"),
                            String.valueOf(TIMER - ((System.currentTimeMillis() - history.getIfPresent(uuid)) / 1000)));
                }
            }
        }, 0, 10);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();

        if (!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
        {
            return;
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.ENDER_PEARL)
        {
            UUID uuid = p.getUniqueId();
            Long time = history.getIfPresent(uuid);

            if (time == null)
            {
                history.put(uuid, System.currentTimeMillis());
            } else
            {
                e.setCancelled(true);
                DesireCore.getLangHandler().sendRenderMessage(p, "enderpearl.message", "{time}", String.valueOf(TIMER - ((System.currentTimeMillis() - time) / 1000)));
            }
        }
    }

}
