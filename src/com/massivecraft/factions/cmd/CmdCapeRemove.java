package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.util.RelationUtil;

public class CmdCapeRemove extends CapeCommand
{
	
	public CmdCapeRemove()
	{
		this.aliases.add("rm");
		this.aliases.add("rem");
		this.aliases.add("remove");
		this.aliases.add("del");
		this.aliases.add("delete");
		this.permission = Permission.CAPE_REMOVE.node;
	}
	
	@Override
	public void perform()
	{
		if (currentCape == null)
		{
			msg("<h>%s <i>has no cape set.", capeguild.describeTo(fme, true));
		}
		else
		{
			capeguild.setCape(null);
			SpoutFeatures.updateCape(capeguild, null);
			msg("<h>%s <i>removed the cape from <h>%s<i>.", RelationUtil.describeThatToMe(fme, fme, true), capeguild.describeTo(fme));
			capeguild.msg("<h>%s <i>removed the cape from <h>%s<i>.", RelationUtil.describeThatToMe(fme, capeguild, true), capeguild.describeTo(capeguild));
		}
	}
}
