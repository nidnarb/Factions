package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.struct.Rel;

public class CmdRelationTruce extends FRelationCommand
{
	public CmdRelationTruce()
	{
		aliases.add("truce");
		targetRelation = Rel.TRUCE;
	}
}
