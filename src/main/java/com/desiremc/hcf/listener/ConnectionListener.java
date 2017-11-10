package com.desiremc.hcf.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import com.desiremc.core.DesireCore;
import com.desiremc.core.session.DeathBan;
import com.desiremc.core.session.HCFSession;
import com.desiremc.core.session.HCFSessionHandler;
import com.desiremc.core.utils.DateUtils;

public class ConnectionListener implements Listener
{

    private static final boolean DEBUG = true;
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e)
    {
        if (DEBUG)
        {
            System.out.println("ConnectionListener.onJoin() event fired.");
        }
        HCFSession session = HCFSessionHandler.initializeHCFSession(e.getPlayer().getUniqueId(), true);

        if (session.getSafeTimeLeft() > 0)
        {
            if (DEBUG)
            {
                System.out.println("ConnectionListener.onJoin() safe time > 0.");
            }
            session.getSafeTimer().resume();
        }
    }
    
    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        HCFSession session = HCFSessionHandler.getHCFSession(e.getPlayer().getUniqueId());
        
        HCFSessionHandler.getInstance().save(session);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        HCFSession s = HCFSessionHandler.initializeHCFSession(event.getPlayer().getUniqueId(), false);
        DeathBan ban = s.getActiveDeathBan();
        if (ban != null)
        {
            String reason = DesireCore.getLangHandler().getPrefix() + "\nYou have an active death ban on this server. It ends in " + DateUtils.formatDateDiff(ban.getStartTime() + s.getRank().getDeathBanTime());
            if (ban.getStartTime() + s.getRank().getDeathBanTime() - System.currentTimeMillis() > 3_600_00)
            {
                reason = reason.replaceAll(" ([0-9]{1,2}) (seconds|second)", "");
            }
            event.disallow(Result.KICK_BANNED, reason);
        }
    }

}
