package com.massivecraft.guilds.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Board;
import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.event.LandUnclaimEvent;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.FLocation;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;

public class CmdUnclaim extends FCommand
{
	public CmdUnclaim()
	{
		this.aliases.add("unclaim");
		this.aliases.add("declaim");
		
		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.UNCLAIM.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		FLocation flocation = new FLocation(fme);
		guild otherguild = Board.getguildAt(flocation);

		if ( ! FPerm.TERRITORY.has(sender, otherguild, true)) return;

		LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(flocation, otherguild, fme);
		Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
		if(unclaimEvent.isCancelled()) return;
	
		//String moneyBack = "<i>";
		if (Econ.shouldBeUsed())
		{
			double refund = Econ.calculateClaimRefund(myguild.getLandRounded());
			
			if(Conf.bankEnabled && Conf.bankguildPaysLandCosts)
			{
				if ( ! Econ.modifyMoney(myguild, refund, "to unclaim this land", "for unclaiming this land")) return;
			}
			else
			{
				if ( ! Econ.modifyMoney(fme      , refund, "to unclaim this land", "for unclaiming this land")) return;
			}
		}

		Board.removeAt(flocation);
		SpoutFeatures.updateTerritoryDisplayLoc(flocation);
		myguild.msg("%s<i> unclaimed some land.", fme.describeTo(myguild, true));

		if (Conf.logLandUnclaims)
			P.p.log(fme.getName()+" unclaimed land at ("+flocation.getCoordString()+") from the guild: "+otherguild.getTag());
	}
	
}
