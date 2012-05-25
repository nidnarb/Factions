package com.massivecraft.guilds.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.event.FPlayerJoinEvent;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.util.RelationUtil;

public class CmdLeader extends FCommand
{	
	public CmdLeader()
	{
		super();
		this.aliases.add("leader");
		
		this.requiredArgs.add("player");
		this.optionalArgs.put("guild", "your");
		
		this.permission = Permission.LEADER.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		FPlayer newLeader = this.argAsBestFPlayerMatch(0);
		if (newLeader == null) return;
		
		guild targetguild = this.argAsguild(1, myguild);
		if (targetguild == null) return;
		
		FPlayer targetguildCurrentLeader = targetguild.getFPlayerLeader();
		
		// We now have fplayer and the target guild
		if (this.senderIsConsole || fme.hasAdminMode() || Permission.LEADER_ANY.has(sender, false))
		{
			// Do whatever you wish
		}
		else
		{
			// Follow the standard rules
			if (fme.getRole() != Rel.LEADER || targetguild != myguild)
			{
				sender.sendMessage(p.txt.parse("<b>You must be leader of the guild to %s.", this.getHelpShort()));
				return;
			}
			
			if (newLeader.getguild() != myguild)
			{
				msg("%s<i> is not a member in the guild.", newLeader.describeTo(fme, true));
				return;
			}
			
			if (newLeader == fme)
			{
				msg("<b>The target player musn't be yourself.");
				return;
			}
		}

		// only perform a FPlayerJoinEvent when newLeader isn't actually in the guild
		if (newLeader.getguild() != targetguild)
		{
			FPlayerJoinEvent event = new FPlayerJoinEvent(FPlayers.i.get(me),targetguild,FPlayerJoinEvent.PlayerJoinReason.LEADER);
			Bukkit.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) return;
		}

		// if target player is currently leader, demote and replace him
		if (targetguildCurrentLeader == newLeader)
		{
			targetguild.promoteNewLeader();
			msg("<i>You have demoted %s<i> from the position of guild leader.", newLeader.describeTo(fme, true));
			newLeader.msg("<i>You have been demoted from the position of guild leader by %s<i>.", senderIsConsole ? "a server admin" : fme.describeTo(newLeader, true));
			return;
		}

		// Perform the switching
		if (targetguildCurrentLeader != null)
		{
			targetguildCurrentLeader.setRole(Rel.OFFICER);
		}
		newLeader.setguild(targetguild);
		newLeader.setRole(Rel.LEADER);
		msg("<i>You have promoted %s<i> to the position of guild leader.", newLeader.describeTo(fme, true));
		
		// Inform all players
		for (FPlayer fplayer : FPlayers.i.getOnline())
		{
			fplayer.msg("%s<i> gave %s<i> the leadership of %s<i>.", senderIsConsole ? "A server admin" : RelationUtil.describeThatToMe(fme, fplayer, true), newLeader.describeTo(fplayer), targetguild.describeTo(fplayer));
		}
	}
}
