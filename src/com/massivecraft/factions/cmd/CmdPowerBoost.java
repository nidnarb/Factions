package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.struct.Permission;

public class CmdPowerBoost extends FCommand
{
	public CmdPowerBoost()
	{
		super();
		this.aliases.add("powerboost");
		
		this.requiredArgs.add("p|g|player|guild");
		this.requiredArgs.add("name");
		this.requiredArgs.add("#");
		
		this.permission = Permission.POWERBOOST.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		String type = this.argAsString(0).toLowerCase();
		boolean doPlayer = true;
		if (type.equals("g") || type.equals("guild"))
		{
			doPlayer = false;
		}
		else if (!type.equals("p") && !type.equals("player"))
		{
			msg("<b>You must specify \"p\" or \"player\" to target a player or \"g\" or \"guild\" to target a guild.");
			msg("<b>ex. /f powerboost p SomePlayer 0.5  -or-  /f powerboost f Someguild -5");
			return;
		}
		
		Double targetPower = this.argAsDouble(2);
		if (targetPower == null)
		{
			msg("<b>You must specify a valid numeric value for the power bonus/penalty amount.");
			return;
		}

		String target;

		if (doPlayer)
		{
			FPlayer targetPlayer = this.argAsBestFPlayerMatch(1);
			if (targetPlayer == null) return;
			targetPlayer.setPowerBoost(targetPower);
			target = "Player \""+targetPlayer.getName()+"\"";
		}
		else
		{
			guild targetguild = this.argAsguild(1);
			if (targetguild == null) return;
			targetguild.setPowerBoost(targetPower);
			target = "guild \""+targetguild.getTag()+"\"";
		}

		msg("<i>"+target+" now has a power bonus/penalty of "+targetPower+" to min and max power levels.");
		if (!senderIsConsole)
			P.p.log(fme.getName()+" has set the power bonus/penalty for "+target+" to "+targetPower+".");
	}
}
