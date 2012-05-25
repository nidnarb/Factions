package com.massivecraft.guilds.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.event.FPlayerJoinEvent;
import com.massivecraft.guilds.struct.Permission;

public class CmdJoin extends FCommand
{
	public CmdJoin()
	{
		super();
		this.aliases.add("join");
		
		this.requiredArgs.add("guild");
		this.optionalArgs.put("player", "you");
		
		this.permission = Permission.JOIN.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		guild guild = this.argAsguild(0);
		if (guild == null) return;

		FPlayer fplayer = this.argAsBestFPlayerMatch(1, fme, false);
		boolean samePlayer = fplayer == fme;

		if (!samePlayer  && ! Permission.JOIN_OTHERS.has(sender, false))
		{
			msg("<b>You do not have permission to move other players into a guild.");
			return;
		}

		if (guild == fplayer.getguild())
		{
			msg("<b>%s %s already a member of %s", fplayer.describeTo(fme, true), (samePlayer ? "are" : "is"), guild.getTag(fme));
			return;
		}

		if (Conf.guildMemberLimit > 0 && guild.getFPlayers().size() >= Conf.guildMemberLimit)
		{
			msg(" <b>!<white> The guild %s is at the limit of %d members, so %s cannot currently join.", guild.getTag(fme), Conf.guildMemberLimit, fplayer.describeTo(fme, false));
			return;
		}

		if (fplayer.hasguild())
		{
			msg("<b>%s must leave %s current guild first.", fplayer.describeTo(fme, true), (samePlayer ? "your" : "their"));
			return;
		}

		if (!Conf.canLeaveWithNegativePower && fplayer.getPower() < 0)
		{
			msg("<b>%s cannot join a guild with a negative power level.", fplayer.describeTo(fme, true));
			return;
		}

		if( ! (guild.getOpen() || guild.isInvited(fplayer) || fme.hasAdminMode() || Permission.JOIN_ANY.has(sender, false)))
		{
			msg("<i>This guild requires invitation.");
			if (samePlayer)
				guild.msg("%s<i> tried to join your guild.", fplayer.describeTo(guild, true));
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
		if (samePlayer && ! canAffordCommand(Conf.econCostJoin, "to join a guild")) return;

		// trigger the join event (cancellable)
		FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(FPlayers.i.get(me),guild,FPlayerJoinEvent.PlayerJoinReason.COMMAND);
		Bukkit.getServer().getPluginManager().callEvent(joinEvent);
		if (joinEvent.isCancelled()) return;

		// then make 'em pay (if applicable)
		if (samePlayer && ! payForCommand(Conf.econCostJoin, "to join a guild", "for joining a guild")) return;

		fme.msg("<i>%s successfully joined %s.", fplayer.describeTo(fme, true), guild.getTag(fme));

		if (!samePlayer)
			fplayer.msg("<i>%s moved you into the guild %s.", fme.describeTo(fplayer, true), guild.getTag(fplayer));
		guild.msg("<i>%s joined your guild.", fplayer.describeTo(guild, true));

		fplayer.resetguildData();
		fplayer.setguild(guild);
		guild.deinvite(fplayer);

		if (Conf.logguildJoin)
		{
			if (samePlayer)
				P.p.log("%s joined the guild %s.", fplayer.getName(), guild.getTag());
			else
				P.p.log("%s moved the player %s into the guild %s.", fme.getName(), fplayer.getName(), guild.getTag());
		}
	}
}
