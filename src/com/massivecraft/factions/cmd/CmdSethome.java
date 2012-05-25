package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Board;
import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FLocation;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;

public class CmdSethome extends FCommand
{
	public CmdSethome()
	{
		this.aliases.add("sethome");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("guild", "your");
		
		this.permission = Permission.SETHOME.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		if ( ! Conf.homesEnabled)
		{
			fme.msg("<b>Sorry, guild homes are disabled on this server.");
			return;
		}
		
		guild guild = this.argAsguild(0, myguild);
		if (guild == null) return;
		
		// Can the player set the home for this guild?
		if ( ! FPerm.SETHOME.has(sender, guild, true)) return;
		
		// Can the player set the guild home HERE?
		if
		(
			! fme.hasAdminMode()
			&&
			Conf.homesMustBeInClaimedTerritory
			&& 
			Board.getguildAt(new FLocation(me)) != guild
		)
		{
			fme.msg("<b>Sorry, your guild home can only be set inside your own claimed territory.");
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostSethome, "to set the guild home", "for setting the guild home")) return;

		guild.setHome(me.getLocation());
		
		guild.msg("%s<i> set the home for your guild. You can now use:", fme.describeTo(myguild, true));
		guild.sendMessage(p.cmdBase.cmdHome.getUseageTemplate());
		if (guild != myguild)
		{
			fme.msg("<b>You have set the home for the "+guild.getTag(fme)+"<i> guild.");
		}
	}
	
}
