package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.struct.Permission;

public class CmdPower extends FCommand
{
	
	public CmdPower()
	{
		super();
		this.aliases.add("power");
		this.aliases.add("pow");
		
		//this.requiredArgs.add("guild tag");
		this.optionalArgs.put("player", "you");
		
		this.permission = Permission.POWER.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		FPlayer target = this.argAsBestFPlayerMatch(0, fme);
		if (target == null) return;
		
		if (target != fme && ! Permission.POWER_ANY.has(sender, true)) return;

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostPower, "to show player power info", "for showing player power info")) return;

		double powerBoost = target.getPowerBoost();
		String boost = (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? " (bonus: " : " (penalty: ") + powerBoost + ")";
		msg("%s<a> - Power / Maxpower: <i>%d / %d %s", target.describeTo(fme, true), target.getPowerRounded(), target.getPowerMaxRounded(), boost);
	}
	
}
