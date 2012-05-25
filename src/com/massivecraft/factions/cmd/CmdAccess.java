package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Board;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.FLocation;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.TerritoryAccess;
import com.massivecraft.guilds.zcore.util.TextUtil;


public class CmdAccess extends FCommand
{
	public CmdAccess()
	{
		super();
		this.aliases.add("access");
		
		this.optionalArgs.put("view|p|g|player|guild", "view");
		this.optionalArgs.put("name", "you");
		
		this.setHelpShort("view or grant access for the claimed territory you are in");

		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		String type = this.argAsString(0);
		type = (type == null) ? "" : type.toLowerCase();
		FLocation loc = new FLocation(me.getLocation());

		TerritoryAccess territory = Board.getTerritoryAccessAt(loc);
		guild locguild = territory.getHostguild();
		boolean accessAny = Permission.ACCESS_ANY.has(sender, false);

		if (type.isEmpty() || type.equals("view"))
		{
			if ( ! accessAny && ! Permission.ACCESS_VIEW.has(sender, true)) return;
			if ( ! accessAny && ! territory.doesHostguildMatch(fme))
			{
				msg("<b>This territory isn't controlled by your guild, so you can't view the access list.");
				return;
			}
			showAccessList(territory, locguild);
			return;
		}

		if ( ! accessAny && ! Permission.ACCESS.has(sender, true)) return;
		if ( ! accessAny && ! FPerm.ACCESS.has(fme, locguild, true)) return;

		boolean doPlayer = true;
		if (type.equals("g") || type.equals("guild"))
		{
			doPlayer = false;
		}
		else if (!type.equals("p") && !type.equals("player"))
		{
			msg("<b>You must specify \"p\" or \"player\" to indicate a player or \"g\" or \"guild\" to indicate a guild.");
			msg("<b>ex. /g access p SomePlayer  -or-  /g access g Someguild");
			msg("<b>Alternately, you can use the command with nothing (or \"view\") specified to simply view the access list.");
			return;
		}

		String target = "";
		boolean added;

		if (doPlayer)
		{
			FPlayer targetPlayer = this.argAsBestFPlayerMatch(1, fme);
			if (targetPlayer == null) return;
			added = territory.toggleFPlayer(targetPlayer);
			target = "Player \""+targetPlayer.getName()+"\"";
		}
		else
		{
			guild targetguild = this.argAsguild(1, myguild);
			if (targetguild == null) return;
			added = territory.toggleguild(targetguild);
			target = "Guild \""+targetguild.getTag()+"\"";
		}

		msg("<i>%s has been %s<i> the access list for this territory.", target, TextUtil.parseColor(added ? "<lime>added to" : "<rose>removed from"));
		SpoutFeatures.updateAccessInfoLoc(loc);
		showAccessList(territory, locguild);
	}

	private void showAccessList(TerritoryAccess territory, guild locguild)
	{
		msg("<i>Host guild %s has %s<i> in this territory.", locguild.getTag(), TextUtil.parseColor(territory.isHostguildAllowed() ? "<lime>normal access" : "<rose>restricted access"));

		String players = territory.fplayerList();
		String guilds = territory.guildList();

		if (guilds.isEmpty())
			msg("No guilds have been explicitly granted access.");
		else
			msg("guilds with explicit access: " + guilds);

		if (players.isEmpty())
			msg("No players have been explicitly granted access.");
		else
			msg("Players with explicit access: " + players);
	}
}
