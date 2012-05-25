package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.struct.Rel;

public class CmdRelationEnemy extends FRelationCommand
{
	public CmdRelationEnemy()
	{
		aliases.add("enemy");
		targetRelation = Rel.ENEMY;
	}
}
