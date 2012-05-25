package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;

public class CmdOfficer extends FCommand
{
	
	public CmdOfficer()
	{
		super();
		this.aliases.add("officer");
		
		this.requiredArgs.add("player name");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.OFFICER.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		FPlayer you = this.argAsBestFPlayerMatch(0);
		if (you == null) return;

		boolean permAny = Permission.OFFICER_ANY.has(sender, false);
		guild targetguild = you.getguild();

		if (targetguild != myguild && !permAny)
		{
			msg("%s<b> is not a member in your guild.", you.describeTo(fme, true));
			return;
		}
		
		if (fme != null && fme.getRole() != Rel.LEADER && !permAny)
		{
			msg("<b>You are not the guild leader.");
			return;
		}

		if (you == fme && !permAny)
		{
			msg("<b>The target player musn't be yourself.");
			return;
		}

		if (you.getRole() == Rel.LEADER)
		{
			msg("<b>The target player is a guild leader. Demote them first.");
			return;
		}

		if (you.getRole() == Rel.OFFICER)
		{
			// Revoke
			you.setRole(Rel.MEMBER);
			targetguild.msg("%s<i> is no longer officer in your guild.", you.describeTo(targetguild, true));
			msg("<i>You have removed officer status from %s<i>.", you.describeTo(fme, true));
		}
		else
		{
			// Give
			you.setRole(Rel.OFFICER);
			targetguild.msg("%s<i> was promoted to officer in your guild.", you.describeTo(targetguild, true));
			msg("<i>You have promoted %s<i> to officer.", you.describeTo(fme, true));
		}
	}
	
}
