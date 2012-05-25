package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.struct.Rel;

public class CmdRelationNeutral extends FRelationCommand
{
	public CmdRelationNeutral()
	{
		aliases.add("neutral");
		targetRelation = Rel.NEUTRAL;
	}
}
