package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.zcore.util.TextUtil;

public class CmdTitle extends FCommand
{
	public CmdTitle()
	{
		this.aliases.add("title");
		
		this.requiredArgs.add("player");
		this.optionalArgs.put("title", "");
		
		this.permission = Permission.TITLE.node;
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
		
		args.remove(0);
		String title = TextUtil.implode(args, " ");
		
		if ( ! canIAdministerYou(fme, you)) return;

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostTitle, "to change a players title", "for changing a players title")) return;

		you.setTitle(title);
		
		// Inform
		myguild.msg("%s<i> changed a title: %s", fme.describeTo(myguild, true), you.describeTo(myguild, true));

		if (Conf.spoutguildTitlesOverNames)
		{
			SpoutFeatures.updateTitle(me, null);
		}
	}
	
}
