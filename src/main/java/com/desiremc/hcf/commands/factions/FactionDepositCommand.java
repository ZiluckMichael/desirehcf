package com.desiremc.hcf.commands.factions;

import java.util.List;

import com.desiremc.core.api.newcommands.CommandArgument;
import com.desiremc.core.api.newcommands.CommandArgumentBuilder;
import com.desiremc.core.parsers.StringParser;
import com.desiremc.core.session.Achievement;
import com.desiremc.core.utils.StringUtils;
import com.desiremc.hcf.DesireHCF;
import com.desiremc.hcf.api.commands.FactionValidCommand;
import com.desiremc.hcf.session.FSession;
import com.desiremc.hcf.session.faction.Faction;
import com.desiremc.hcf.validators.SenderHasAmountMoney;
import com.desiremc.hcf.validators.SenderHasFactionValidator;

import net.minecraft.util.com.google.common.primitives.Doubles;

public class FactionDepositCommand extends FactionValidCommand
{
    public FactionDepositCommand()
    {
        super("deposit", "Deposit money to your faction.", true, new String[] {"d"});

        addSenderValidator(new SenderHasFactionValidator());

        addArgument(CommandArgumentBuilder.createBuilder(String.class)
                .setName("amount")
                .setParser(new StringParser())
                .addValidator(new SenderHasAmountMoney())
                .build());
    }

    @Override
    public void validFactionRun(FSession sender, String[] label, List<CommandArgument<?>> arguments)
    {
        Faction faction = sender.getFaction();
        String arg = (String) arguments.get(0).getValue();
        double amount;

        if (arg.equalsIgnoreCase("all"))
        {
            amount = sender.getBalance();
        }
        else
        {
            amount = Doubles.tryParse(arg);
        }


        faction.depositBalance(amount);
        sender.withdrawBalance(amount);

        if (faction.getBalance() >= 30000)
        {
            for (FSession fSession : faction.getMembers())
            {
                fSession.awardAchievement(Achievement.WEALTHY, true);
            }
        }

        faction.addLog(DesireHCF.getLangHandler().renderMessage("factions.deposit", false, false, "{player}", sender.getName(), "{amount}", StringUtils.doubleFormat(amount)));

        faction.save();
        sender.save();

        faction.broadcast(DesireHCF.getLangHandler().renderMessage("factions.deposit", true, false, "{player}", sender.getName(), "{amount}", StringUtils.doubleFormat(amount)));
    }
}