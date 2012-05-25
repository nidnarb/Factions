package com.massivecraft.guilds.cmd;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.guilds;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.event.FPlayerJoinEvent;
import com.massivecraft.guilds.event.guildCreateEvent;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;

public class CmdCreate extends FCommand
{
	public CmdCreate()
	{
		super();
		this.aliases.add("create");
		
		this.requiredArgs.add("guild tag");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.CREATE.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		String tag = this.argAsString(0);
		
		if (fme.hasguild())
		{
			msg("<b>You must leave your current guild first.");
			return;
		}
		
		if (guilds.i.isTagTaken(tag))
		{
			msg("<b>That tag is already in use.");
			return;
		}
		
		ArrayList<String> tagValidationErrors = guilds.validateTag(tag);
		if (tagValidationErrors.size() > 0)
		{
			sendMessage(tagValidationErrors);
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
		if ( ! canAffordCommand(Conf.econCostCreate, "to create a new guild")) return;

		// trigger the guild creation event (cancellable)
		guildCreateEvent createEvent = new guildCreateEvent(me, tag);
		Bukkit.getServer().getPluginManager().callEvent(createEvent);
		if(createEvent.isCancelled()) return;
		
		// then make 'em pay (if applicable)
		if ( ! payForCommand(Conf.econCostCreate, "to create a new guild", "for creating a new guild")) return;

		guild guild = guilds.i.create();

		// TODO: Why would this even happen??? Auto increment clash??
		if (guild == null)
		{
			msg("<b>There was an internal error while trying to create your guild. Please try again.");
			return;
		}

		// finish setting up the guild
	guild.setTag(tag);

	// trigger the guild join event for the creator
	FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(FPlayers.i.get(me),guild,FPlayerJoinEvent.PlayerJoinReason.CREATE);
	Bukkit.getServer().getPluginManager().callEvent(joinEvent);
	// join event cannot be cancelled or you'll have an empty guild

	// finish setting up the FPlayer
		fme.setRole(Rel.LEADER);
		fme.setguild(guild);

		for (FPlayer follower : FPlayers.i.getOnline())
		{
			follower.msg("%s<i> created a new guild %s", fme.describeTo(follower, true), guild.getTag(follower));
		}
		
		msg("<i>You should now: %s", p.cmdBase.cmdDescription.getUseageTemplate());

		if (Conf.logguildCreate)
			P.p.log(fme.getName()+" created a new guild: "+tag);
	}
	
}
