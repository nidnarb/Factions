package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.iface.EconomyParticipator;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.struct.Permission;

import org.bukkit.ChatColor;


public class CmdMoneyTransferPf extends FCommand
{
	public CmdMoneyTransferPf()
	{
		this.aliases.add("pf");
		
		this.requiredArgs.add("amount");
		this.requiredArgs.add("player");
		this.requiredArgs.add("guild");
		
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.MONEY_P2F.node;
		this.setHelpShort("transfer p -> f");
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		double amount = this.argAsDouble(0, 0d);
		EconomyParticipator from = this.argAsBestFPlayerMatch(1);
		if (from == null) return;
		EconomyParticipator to = this.argAsguild(2);
		if (to == null) return;
		
		boolean success = Econ.transferMoney(fme, from, to, amount);

		if (success && Conf.logMoneyTransactions)
			P.p.log(ChatColor.stripColor(P.p.txt.parse("%s transferred %s from the player \"%s\" to the guild \"%s\"", fme.getName(), Econ.moneyString(amount), from.describeTo(null), to.describeTo(null))));
	}
}
