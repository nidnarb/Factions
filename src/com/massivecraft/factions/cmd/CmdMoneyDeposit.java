package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.iface.EconomyParticipator;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.struct.Permission;

import org.bukkit.ChatColor;


public class CmdMoneyDeposit extends FCommand
{
	
	public CmdMoneyDeposit()
	{
		super();
		this.aliases.add("d");
		this.aliases.add("deposit");
		
		this.requiredArgs.add("amount");
		this.optionalArgs.put("guild", "your");
		
		this.permission = Permission.MONEY_DEPOSIT.node;
		this.setHelpShort("deposit money");
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		double amount = this.argAsDouble(0, 0d);
		EconomyParticipator guild = this.argAsguild(1, myguild);
		if (guild == null) return;
		boolean success = Econ.transferMoney(fme, fme, guild, amount);

		if (success && Conf.logMoneyTransactions)
			P.p.log(ChatColor.stripColor(P.p.txt.parse("%s deposited %s in the guild bank: %s", fme.getName(), Econ.moneyString(amount), guild.describeTo(null))));
	}
	
}
