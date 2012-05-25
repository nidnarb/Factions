package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.guild;

public class CmdMoneyBalance extends FCommand
{
	public CmdMoneyBalance()
	{
		super();
		this.aliases.add("b");
		this.aliases.add("balance");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("guild", "your");
		
		this.permission = Permission.MONEY_BALANCE.node;
		this.setHelpShort("show guild balance");
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		guild guild = myguild;
		if (this.argIsSet(0))
		{
			guild = this.argAsguild(0);
		}
			
		if (guild == null) return;
		if (guild != myguild && ! Permission.MONEY_BALANCE_ANY.has(sender, true)) return;
		
		Econ.sendBalanceInfo(fme, guild);
	}
	
}
