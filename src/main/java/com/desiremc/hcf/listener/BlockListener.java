package com.desiremc.hcf.listener;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.desiremc.core.session.Achievement;
import com.desiremc.core.session.Session;
import com.desiremc.core.session.SessionHandler;
import com.desiremc.core.session.SessionSetting;
import com.desiremc.core.utils.ChatUtils;
import com.desiremc.hcf.DesireHCF;
import com.desiremc.hcf.session.FSession;
import com.desiremc.hcf.session.FSessionHandler;

public class BlockListener implements Listener
{

    private final static LinkedList<Material> ALWAYS = new LinkedList<>(Arrays.asList(Material.GOLD_ORE, Material.IRON_ORE));
    private final static LinkedList<Material> NON_SILK = new LinkedList<>(Arrays.asList(Material.EMERALD_ORE, Material.DIAMOND_ORE, Material.COAL_ORE));
    private final static EnumSet<Material> FINDORE = EnumSet.of(Material.DIAMOND_ORE, Material.EMERALD_ORE);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOreBreak(BlockBreakEvent event)
    {
        Player p = event.getPlayer();
        Material type = event.getBlock().getType();
        FSession fSession = FSessionHandler.getOnlineFSession(p.getUniqueId());

        if ((ALWAYS.contains(type) || (!p.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH) && NON_SILK.contains(type))))
        {
            try
            {
                fSession.getCurrentOre().add(type, 1);
            }
            catch (Exception ex)
            {
                ChatUtils.sendStaffMessage(ex, DesireHCF.getInstance());
            }
        }

        if (fSession.getCurrentOre().getDiamondCount() >= 1000)
        {
            fSession.awardAchievement(Achievement.DIAMONDS_1000, true);
        }

        if (!FINDORE.contains(event.getBlock().getType()))
        {
            return;
        }

        if (event.getBlock().hasMetadata("Found"))
        {
            event.getBlock().removeMetadata("Found", DesireHCF.getInstance());
            return;
        }

        Set<Block> vein = getVein(event.getBlock());
        for (Session session : SessionHandler.getOnlineSessions())
        {
            if (session.getSetting(SessionSetting.FINDORE))
            {
                DesireHCF.getLangHandler().sendRenderMessage(session, "findore.notification", false, false,
                        "{player}", p.getName(),
                        "{count}", vein.size(),
                        "{ore}", StringUtils.capitalize(type.name().replaceAll("_", " ").toLowerCase()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event)
    {
        if (FINDORE.contains(event.getBlockPlaced().getType()))
        {
            event.getBlock().setMetadata("Found", new FixedMetadataValue(DesireHCF.getInstance(), ""));
        }

        if (event.getBlockPlaced().getType() == Material.MOB_SPAWNER)
        {
            FSession fSession = FSessionHandler.getOnlineFSession(event.getPlayer().getUniqueId());
            fSession.awardAchievement(Achievement.FIRST_SPAWNER, true);
        }
    }

    private Set<Block> getVein(Block block)
    {
        Set<Block> vein = new HashSet<>();
        vein.add(block);
        getVein(block, vein);
        return vein;
    }

    private void getVein(Block block, Set<Block> vein)
    {
        for (int i = -1; i <= 1; i++)
        {
            for (int j = -1; j <= 1; j++)
            {
                for (int k = -1; k <= 1; k++)
                {
                    if (i == 0 && j == 0 && k == 0)
                    {
                        continue;
                    }
                    Block relative = block.getRelative(i, j, k);
                    if (!vein.contains(relative) && block.getType().equals(relative.getType()))
                    {
                        vein.add(relative);
                        relative.setMetadata("Found", new FixedMetadataValue(DesireHCF.getInstance(), ""));
                        getVein(relative, vein);
                    }
                }
            }
        }
    }
}
