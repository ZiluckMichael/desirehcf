package com.desiremc.hcf.handler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.desiremc.core.session.Session;
import com.desiremc.core.session.SessionHandler;
import com.desiremc.core.session.SessionSetting;
import com.desiremc.core.tablist.Entry;
import com.desiremc.core.tablist.Tab;
import com.desiremc.hcf.DesireHCF;
import com.desiremc.hcf.util.FactionsUtils;
import com.massivecraft.factions.FPlayer;

public class TablistHandler implements Listener
{

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event)
    {
        Bukkit.getScheduler().runTaskLater(DesireHCF.getInstance(), new Runnable()
        {

            @Override
            public void run()
            {
                for (Session s : SessionHandler.getInstance().getSessions())
                {
                    if (s.getSetting(SessionSetting.CLASSICTAB))
                    {
                        applyClassic(s.getPlayer());
                    }
                    else
                    {
                        applyFactions(s.getPlayer());
                    }
                }
            }
        }, 5l);
    }

    private void applyClassic(Player player)
    {
        FPlayer user = FactionsUtils.getFPlayer(player);
        if (user == null)
        {
            return;
        }
        int i = 0;
        Tab tab = Tab.getByPlayer(player);
        Entry entry;
        for (FPlayer fp : FactionsUtils.getOnlineFPlayers())
        {
            entry = tab.getEntry(i / 20, i % 20);
            if (entry == null)
            {
                Bukkit.getScheduler().runTaskLater(DesireHCF.getInstance(), new WhyDoTablistsHaveToSuckSoMuch(user, fp, i), 60l);
            }
            else
            {
                entry.setText((fp.getFaction().isNormal() ? FactionsUtils.getRelationshipColor(user.getRelationTo(fp)) : ChatColor.YELLOW) + fp.getPlayer().getName()).setSkin(fp.getPlayer().getName()).send();
            }
            i++;
        }
    }

    private void applyFactions(Player player)
    {
        applyClassic(player);
    }

    private static class WhyDoTablistsHaveToSuckSoMuch implements Runnable
    {
        private FPlayer user;
        private FPlayer fp;
        private int i;

        public WhyDoTablistsHaveToSuckSoMuch(FPlayer user, FPlayer fp, int i)
        {
            this.fp = fp;
            this.user = user;
            this.i = i;
        }

        public void run()
        {
            Tab tab = Tab.getByPlayer(user.getPlayer());
            tab.assemble();
            Entry entry = tab.getEntry(i / 20, i % 20);
            entry.setText((fp.getFaction().isNormal() ? FactionsUtils.getRelationshipColor(user.getRelationTo(fp)) : ChatColor.YELLOW) + fp.getPlayer().getName()).send();
        }
    }

}
