package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;

public class CmdAutoClaim extends FCommand
{
	public CmdAutoClaim()
	{
		super();
		this.aliases.add("autoclaim");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("guild", "your");
		
		this.permission = Permission.AUTOCLAIM.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}

	@Override
	public void perform()
	{
		guild forguild = this.argAsguild(0, myguild);
		if (forguild == null || forguild == fme.getAutoClaimFor())
		{
			fme.setAutoClaimFor(null);
			msg("<i>Auto-claiming of land disabled.");
			return;
		}
		
		if ( ! FPerm.TERRITORY.has(fme, forguild, true)) return;
		
		fme.setAutoClaimFor(forguild);
		
		msg("<i>Now auto-claiming land for <h>%s<i>.", forguild.describeTo(fme));
		fme.attemptClaim(forguild, me.getLocation(), true);
	}
	
}