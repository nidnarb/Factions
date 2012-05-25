package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.guilds;
import com.massivecraft.guilds.struct.Permission;

public class CmdOpen extends FCommand
{
	public CmdOpen()
	{
		super();
		this.aliases.add("open");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("yes/no", "flip");
		
		this.permission = Permission.OPEN.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = true;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostOpen, "to open or close the guild", "for opening or closing the guild")) return;

		myguild.setOpen(this.argAsBool(0, ! myguild.getOpen()));
		
		String open = myguild.getOpen() ? "open" : "closed";
		
		// Inform
		myguild.msg("%s<i> changed the guild to <h>%s<i>.", fme.describeTo(myguild, true), open);
		for (guild guild : guilds.i.get())
		{
			if (guild == myguild)
			{
				continue;
			}
			guild.msg("<i>The guild %s<i> is now %s", myguild.getTag(guild), open);
		}
	}
	
}
