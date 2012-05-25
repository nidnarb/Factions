package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Board;
import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guilds;
import com.massivecraft.guilds.struct.Permission;

public class CmdSaveAll extends FCommand
{
	
	public CmdSaveAll()
	{
		super();
		this.aliases.add("saveall");
		this.aliases.add("save");
		
		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.SAVE.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		FPlayers.i.saveToDisc();
		guilds.i.saveToDisc();
		Board.save();
		Conf.save();
		msg("<i>guilds saved to disk!");
	}
	
}