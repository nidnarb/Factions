package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;

public class CmdInvite extends FCommand
{
	public CmdInvite()
	{
		super();
		this.aliases.add("invite");
		this.aliases.add("inv");
		
		this.requiredArgs.add("player");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.INVITE.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = true;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		FPlayer you = this.argAsBestFPlayerMatch(0);
		if (you == null) return;
		
		if (you.getguild() == myguild)
		{
			msg("%s<i> is already a member of %s", you.getName(), myguild.getTag());
			msg("<i>You might want to: " +  p.cmdBase.cmdKick.getUseageTemplate(false));
			return;
		}

		if (fme != null && ! FPerm.INVITE.has(fme, myguild)) return;
		
		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostInvite, "to invite someone", "for inviting someone")) return;

		myguild.invite(you);
		
		you.msg("%s<i> invited you to %s", fme.describeTo(you, true), myguild.describeTo(you));
		myguild.msg("%s<i> invited %s<i> to your guild.", fme.describeTo(myguild, true), you.describeTo(myguild));
	}
	
}
