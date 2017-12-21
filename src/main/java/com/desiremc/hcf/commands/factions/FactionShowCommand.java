package com.desiremc.hcf.commands.factions;

import com.desiremc.core.api.newcommands.CommandArgument;
import com.desiremc.core.api.newcommands.CommandArgumentBuilder;
import com.desiremc.hcf.DesireHCF;
import com.desiremc.hcf.api.commands.FactionValidCommand;
import com.desiremc.hcf.parsers.FactionParser;
import com.desiremc.hcf.session.FSession;
import com.desiremc.hcf.session.faction.Faction;

import java.util.List;

public class FactionShowCommand extends FactionValidCommand
{

    public FactionShowCommand()
    {
        super("show", "Show information about your faction.", true, new String[] {"who", "info"});

        addArgument(CommandArgumentBuilder.createBuilder(Faction.class)
                .setName("faction")
                .setParser(new FactionParser())
                .setOptional()
                .setAllowsConsole()
                .build());
    }

    @Override
    public void validFactionRun(FSession sender, String[] label, List<CommandArgument<?>> arguments)
    {
        if (!arguments.get(0).hasValue() && !sender.hasFaction())
        {
            DesireHCF.getLangHandler().sendRenderMessage(sender.getSession(), "factions.no_faction");
            return;
        }

        Faction faction = arguments.get(0).hasValue() ? (Faction) arguments.get(0).getValue() : sender.getFaction();

        List<String> values = DesireHCF.getLangHandler().getStringList("factions.who");

        for (String value : values)
        {
            if (value.contains("{description}") && faction.getDescription() == null)
            {
                continue;
            }
            sender.sendMessage(processPlaceholders(value, faction));
        }
    }

    private String processPlaceholders(String value, Faction faction)
    {
        String balance;

        if (faction.getBalance() % 1 == 0)
        {
            balance = Integer.toString((int) faction.getBalance());
        }
        else
        {
            balance = Double.toString(faction.getBalance());
        }

        value = DesireHCF.getLangHandler().renderString(value,
                "{faction}", faction.getName(),
                "{online}", faction.getOnlineMembers().size(),
                "{max}", faction.getMemberSize(),
                "{description}", faction.getDescription(),
                "{leader}", faction.getLeader().getName(),
                "{leader_kills}", faction.getLeader().getTotalKills(),
                "{kills}", faction.getTotalKills(),
                "{balance}", balance,
                "{dtr}", faction.getDTR());

        StringBuilder sb = new StringBuilder();
        for (FSession member : faction.getMembers())
        {
            if (member == faction.getLeader())
            {
                continue;
            }
            if (member.isOnline())
            {
                sb.append("§a");
            }
            else
            {
                sb.append("§c");
            }
            sb.append(member.getName() + "§e, ");
        }
        sb.setLength(sb.length() - 2);
        value = value.replace("{members}", sb.toString());

        return value;
    }

}
