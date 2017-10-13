package com.desiremc.hcf.commands.region.modify;

import com.desiremc.core.api.command.ValidCommand;
import com.desiremc.core.parsers.IntegerParser;
import com.desiremc.core.session.Rank;
import com.desiremc.core.validators.IntegerSizeValidator;
import com.desiremc.hcf.HCFCore;
import com.desiremc.hcf.api.LangHandler;
import com.desiremc.hcf.parser.RegionParser;
import com.desiremc.hcf.session.Region;
import com.desiremc.hcf.session.RegionHandler;
import org.bukkit.command.CommandSender;

public class RegionModifyDistanceCommand extends ValidCommand
{

    private static final LangHandler LANG = HCFCore.getLangHandler();

    public RegionModifyDistanceCommand()
    {
        super("distance", "Change the view distance of a region.", Rank.ADMIN, new String[] { "region", "distance" });

        addParser(new RegionParser(), "region");
        addParser(new IntegerParser(), "distance");

        addValidator(new IntegerSizeValidator(1, HCFCore.getConfigHandler().getInteger("barrier.max-distance")), "distance");
    }

    @Override
    public void validRun(CommandSender sender, String label, Object... args)
    {
        Region r = (Region) args[0];
        int distance = (Integer) args[1];
        int oldDistance = r.getViewDistance();
        r.setViewDistance(distance);
        RegionHandler.getInstance().save(r);
        
        LANG.sendRenderMessage(sender, "region.changed", "{change}", "distance", "{old}", String.valueOf(oldDistance), "{new}", String.valueOf(distance));

    }

}
