package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.P;
import com.massivecraft.guilds.struct.Permission;


public class CmdVersion extends FCommand
{
	public CmdVersion()
	{
		this.aliases.add("version");
		
		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.VERSION.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}

	@Override
	public void perform()
	{
		msg("<i>You are running "+P.p.getDescription().getFullName());
	}
}
