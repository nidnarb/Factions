package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.P;
import com.massivecraft.guilds.struct.Permission;

public class CmdCape extends FCommand
{
	public CmdCapeGet cmdCapeGet = new CmdCapeGet();
	public CmdCapeSet cmdCapeSet = new CmdCapeSet();
	public CmdCapeRemove cmdCapeRemove = new CmdCapeRemove();
	
	public CmdCape()
	{
		super();
		this.aliases.add("cape");
		
		this.permission = Permission.CAPE.node;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
		
		this.addSubCommand(this.cmdCapeGet);
		this.addSubCommand(this.cmdCapeSet);
		this.addSubCommand(this.cmdCapeRemove);
	}
	
	@Override
	public void perform()
	{
		this.commandChain.add(this);
		P.p.cmdAutoHelp.execute(this.sender, this.args, this.commandChain);
	}
	
}
