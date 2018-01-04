package com.desiremc.hcf.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.desiremc.core.fanciful.FancyMessage;
import com.desiremc.core.session.Achievement;
import com.desiremc.core.session.Rank;
import com.desiremc.core.session.Session;
import com.desiremc.core.session.SessionHandler;
import com.desiremc.core.session.SessionSetting;
import com.desiremc.core.utils.ChatUtils;
import com.desiremc.hcf.DesireHCF;
import com.desiremc.hcf.session.FSession;
import com.desiremc.hcf.session.FSessionHandler;
import com.desiremc.hcf.session.faction.Faction;
import com.desiremc.hcf.session.faction.FactionChannel;
import com.desiremc.hcf.util.FactionsUtils;

public class ChatListener implements Listener
{

    private static final boolean DEBUG = false;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (DEBUG)
        {
            System.out.println("ChatListener.onChat() called.");
        }
        Player player = event.getPlayer();
        event.setCancelled(true);
        Session s = SessionHandler.getSession(player);
        if (s.isMuted() != null)
        {
            player.sendMessage(ChatColor.DARK_GRAY + "-----------------------------------------------------");
            player.sendMessage("");
            ChatUtils.sendCenteredMessage(player, DesireHCF.getLangHandler().getPrefix().replace(" ", ""));
            player.sendMessage("");
            ChatUtils.sendCenteredMessage(player, ChatColor.GRAY + "You are muted and " + ChatColor.RED + "CANNOT " + ChatColor.GRAY + "speak!");
            ChatUtils.sendCenteredMessage(player, ChatColor.GRAY + "Visit our rules @ " + ChatColor.YELLOW + "https://desirehcf.net/rules");
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_GRAY + "-----------------------------------------------------");
            return;
        }

        String msg = event.getMessage();

        Faction f = FactionsUtils.getFaction(player);
        FSession fSession = FSessionHandler.getOnlineFSession(player.getUniqueId());

        if (fSession.getChannel() == FactionChannel.FACTION)
        {
            String message = DesireHCF.getLangHandler().renderMessage("factions.chat.faction", false, false, "{name}", player.getName(), "{message}", msg);
            for (FSession member : f.getOnlineMembers())
            {
                member.sendMessage(message);
            }
        }
        else if (fSession.getChannel() == FactionChannel.ALLY)
        {
            String message = DesireHCF.getLangHandler().renderMessage("factions.chat.ally", false, false, "{name}", player.getName(), "{message}", msg);

            for (Faction ally : f.getAllies())
            {
                for (FSession member : ally.getOnlineMembers())
                {
                    member.sendMessage(message);
                }
            }
            for (FSession member : f.getOnlineMembers())
            {
                member.sendMessage(message);
            }
        }
        else
        {
            String parsedMessage = s.getRank().getId() >= Rank.ADMIN.getId() ? ChatColor.translateAlternateColorCodes('&', msg) : msg;

            FancyMessage message = new FancyMessage(f.isWilderness() ? "§8[§b*§8] " : "§8[§b" + f.getName() + "§8] ")
                    .then(s.getRank().getPrefix())
                    .then(s.getName())
                    .color(s.getRank().getMain())
                    .tooltip(FactionsUtils.getMouseoverDetails(f))
                    .command("/f who " + f.getName())
                    .then(": ")
                    .then(parsedMessage)
                    .color(s.getRank().getColor());

            Bukkit.getOnlinePlayers().stream().forEach(p -> message.send(p));
            for (Player p : Bukkit.getOnlinePlayers())
            {
                Session session = SessionHandler.getOnlineSession(p.getUniqueId());
                if (session.getSetting(SessionSetting.CHAT))
                {
                    message.send(p);
                }
            }

            s.awardAchievement(Achievement.HELLO_WORLD, true);
        }
    }

}
