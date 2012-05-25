package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.struct.Rel;

public class CmdRelationAlly extends FRelationCommand
{
	public CmdRelationAlly()
	{
		aliases.add("ally");
		targetRelation = Rel.ALLY;
	}
}
