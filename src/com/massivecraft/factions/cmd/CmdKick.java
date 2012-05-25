package com.massivecraft.guilds.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.event.FPlayerLeaveEvent;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;

public class CmdKick extends FCommand
{
	
	public CmdKick()
	{
		super();
		this.aliases.add("kick");
		
		this.requiredArgs.add("player");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.KICK.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{	
		FPlayer you = this.argAsBestFPlayerMatch(0);
		if (you == null) return;
		
		if (fme == you)
		{
			msg("<b>You cannot kick yourself.");
			msg("<i>You might want to: %s", p.cmdBase.cmdLeave.getUseageTemplate(false));
			return;
		}
		
		if (you.getRole() == Rel.LEADER && !(this.senderIsConsole || fme.hasAdminMode()))
		{
			msg("<b>The leader can not be kicked.");
			return;
		}

		if ( ! Conf.canLeaveWithNegativePower && you.getPower() < 0)
		{
			msg("<b>You cannot kick that member until their power is positive.");
			return;
		}
		
		guild yourguild = you.getguild();

		if (fme != null && ! FPerm.KICK.has(fme, yourguild)) return;

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
		if ( ! canAffordCommand(Conf.econCostKick, "to kick someone from the guild")) return;

		// trigger the leave event (cancellable) [reason:kicked]
		FPlayerLeaveEvent event = new FPlayerLeaveEvent(you, you.getguild(), FPlayerLeaveEvent.PlayerLeaveReason.KICKED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		// then make 'em pay (if applicable)
		if ( ! payForCommand(Conf.econCostKick, "to kick someone from the guild", "for kicking someone from the guild")) return;

		yourguild.msg("%s<i> kicked %s<i> from the guild! :O", fme.describeTo(yourguild, true), you.describeTo(yourguild, true));
		you.msg("%s<i> kicked you from %s<i>! :O", fme.describeTo(you, true), yourguild.describeTo(you));
		if (yourguild != myguild)
		{
			fme.msg("<i>You kicked %s<i> from the guild %s<i>!", you.describeTo(fme), yourguild.describeTo(fme));
		}

		if (Conf.logguildKick)
			P.p.log((senderIsConsole ? "A console command" : fme.getName())+" kicked "+you.getName()+" from the guild: "+yourguild.getTag());

		if (you.getRole() == Rel.LEADER)
			yourguild.promoteNewLeader();

		yourguild.deinvite(you);
		you.resetguildData();
	}
	
}
