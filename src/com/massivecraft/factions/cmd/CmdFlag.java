package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.Permission;

public class CmdFlag extends FCommand
{
	
	public CmdFlag()
	{
		super();
		this.aliases.add("flag");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("guild", "your");
		this.optionalArgs.put("flag", "all");
		this.optionalArgs.put("yes/no", "read");
		
		this.permission = Permission.FLAG.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		guild guild = myguild;
		if (this.argIsSet(0))
		{
			guild = this.argAsguild(0);
		}
		if (guild == null) return;
		
		if ( ! this.argIsSet(1))
		{
			msg(p.txt.titleize("Flags for " + guild.describeTo(fme, true)));
			for (FFlag flag : FFlag.values())
			{
				msg(flag.getStateInfo(guild.getFlag(flag), true));
			}
			return;
		}
		
		FFlag flag = this.argAsguildFlag(1);
		if (flag == null) return;
		if ( ! this.argIsSet(2))
		{
			msg(p.txt.titleize("Flag for " + guild.describeTo(fme, true)));
			msg(flag.getStateInfo(guild.getFlag(flag), true));
			return;
		}
		
		Boolean targetValue = this.argAsBool(2);
		if (targetValue == null) return;

		// Do the sender have the right to change flags?
		if ( ! Permission.FLAG_SET.has(sender, true)) return;
		
		// Do the change
		msg(p.txt.titleize("Flag for " + guild.describeTo(fme, true)));
		guild.setFlag(flag, targetValue);
		msg(flag.getStateInfo(guild.getFlag(flag), true));
	}
	
}
