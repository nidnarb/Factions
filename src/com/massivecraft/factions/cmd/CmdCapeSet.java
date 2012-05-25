package com.massivecraft.guilds.cmd;

import java.net.URL;

import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.util.RelationUtil;

public class CmdCapeSet extends CapeCommand
{
	
	public CmdCapeSet()
	{
		this.aliases.add("set");
		this.requiredArgs.add("url");
		this.permission = Permission.CAPE_SET.node;
	}
	
	@Override
	public void perform()
	{
		String newCape = this.argAsString(0);
		
		if (isUrlValid(newCape))
		{
			capeguild.setCape(newCape);
			SpoutFeatures.updateCape(capeguild, null);
			msg("<h>%s <i>set the cape of <h>%s<i> to \"<h>%s<i>\".", RelationUtil.describeThatToMe(fme, fme, true), capeguild.describeTo(fme), newCape);
			capeguild.msg("<h>%s <i>set the cape of <h>%s<i> to \"<h>%s<i>\".", RelationUtil.describeThatToMe(fme, capeguild, true), capeguild.describeTo(capeguild), newCape);
		}
		else
		{
			msg("<i>\"<h>%s<i>\" is not a valid URL.", newCape);
		}
	}
	
	public static boolean isUrlValid(String urlString)
	{
		try
		{
			new URL(urlString);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
