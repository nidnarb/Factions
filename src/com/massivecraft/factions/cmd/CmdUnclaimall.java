package com.massivecraft.guilds.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Board;
import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.event.LandUnclaimAllEvent;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.Permission;

public class CmdUnclaimall extends FCommand
{	
	public CmdUnclaimall()
	{
		this.aliases.add("unclaimall");
		this.aliases.add("declaimall");
		
		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.UNCLAIM_ALL.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = true;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		if (Econ.shouldBeUsed())
		{
			double refund = Econ.calculateTotalLandRefund(myguild.getLandRounded());
			if(Conf.bankEnabled && Conf.bankguildPaysLandCosts)
			{
				if ( ! Econ.modifyMoney(myguild, refund, "to unclaim all guild land", "for unclaiming all guild land")) return;
			}
			else
			{
				if ( ! Econ.modifyMoney(fme      , refund, "to unclaim all guild land", "for unclaiming all guild land")) return;
			}
		}

		LandUnclaimAllEvent unclaimAllEvent = new LandUnclaimAllEvent(myguild, fme);
	Bukkit.getServer().getPluginManager().callEvent(unclaimAllEvent);
		// this event cannot be cancelled

		Board.unclaimAll(myguild.getId());
		myguild.msg("%s<i> unclaimed ALL of your guild's land.", fme.describeTo(myguild, true));
		SpoutFeatures.updateTerritoryDisplayLoc(null);

		if (Conf.logLandUnclaims)
			P.p.log(fme.getName()+" unclaimed everything for the guild: "+myguild.getTag());
	}
	
}
