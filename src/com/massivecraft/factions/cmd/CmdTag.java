package com.massivecraft.guilds.cmd;

import java.util.ArrayList;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.guilds;
import com.massivecraft.guilds.event.guildRenameEvent;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.util.MiscUtil;

public class CmdTag extends FCommand
{
	
	public CmdTag()
	{
		this.aliases.add("tag");
		
		this.requiredArgs.add("new tag");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.TAG.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = true;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		String tag = this.argAsString(0);
		
		// TODO does not first test cover selfcase?
		if (guilds.i.isTagTaken(tag) && ! MiscUtil.getComparisonString(tag).equals(myguild.getComparisonTag()))
		{
			msg("<b>That tag is already taken");
			return;
		}

		ArrayList<String> errors = new ArrayList<String>();
		errors.addAll(guilds.validateTag(tag));
		if (errors.size() > 0)
		{
			sendMessage(errors);
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
		if ( ! canAffordCommand(Conf.econCostTag, "to change the guild tag")) return;

		// trigger the guild rename event (cancellable)
		guildRenameEvent renameEvent = new guildRenameEvent(fme, tag);
		Bukkit.getServer().getPluginManager().callEvent(renameEvent);
		if(renameEvent.isCancelled()) return;

		// then make 'em pay (if applicable)
		if ( ! payForCommand(Conf.econCostTag, "to change the guild tag", "for changing the guild tag")) return;

		String oldtag = myguild.getTag();
		myguild.setTag(tag);

		// Inform
		myguild.msg("%s<i> changed your guild tag to %s", fme.describeTo(myguild, true), myguild.getTag(myguild));
		for (guild guild : guilds.i.get())
		{
			if (guild == myguild)
			{
				continue;
			}
			guild.msg("<i>The guild %s<i> changed their name to %s.", fme.getColorTo(guild)+oldtag, myguild.getTag(guild));
		}

		if (Conf.spoutguildTagsOverNames)
		{
			SpoutFeatures.updateTitle(myguild, null);
		}
	}
	
}
