package com.massivecraft.guilds.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.zcore.util.TextUtil;

public class CmdShow extends FCommand
{
	public CmdShow()
	{
		this.aliases.add("show");
		this.aliases.add("who");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("guild", "your");
		
		this.permission = Permission.SHOW.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}

	@Override
	public void perform()
	{
		guild guild = myguild;
		if (this.argIsSet(0))
		{
			guild = this.argAsguild(0);
			if (guild == null) return;
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostShow, "to show guild information", "for showing guild information")) return;

		Collection<FPlayer> admins = guild.getFPlayersWhereRole(Rel.LEADER);
		Collection<FPlayer> mods = guild.getFPlayersWhereRole(Rel.OFFICER);
		Collection<FPlayer> normals = guild.getFPlayersWhereRole(Rel.MEMBER);
		
		msg(p.txt.titleize(guild.getTag(fme)));
		msg("<a>Description: <i>%s", guild.getDescription());
		
		// Display important flags
		// TODO: Find the non default flags, and display them instead.
		if (guild.getFlag(FFlag.PERMANENT))
		{
			msg("<a>This guild is permanent - remaining even with no members.");
		}
		
		if (guild.getFlag(FFlag.PEACEFUL))
		{
			msg("<a>This guild is peaceful - in truce with everyone.");
		}
		
		msg("<a>Joining: <i>"+(guild.getOpen() ? "no invitation is needed" : "invitation is required"));

		double powerBoost = guild.getPowerBoost();
		String boost = (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? " (bonus: " : " (penalty: ") + powerBoost + ")";
		msg("<a>Land / Power / Maxpower: <i> %d/%d/%d %s", guild.getLandRounded(), guild.getPowerRounded(), guild.getPowerMaxRounded(), boost);

		// show the land value
		if (Econ.shouldBeUsed())
		{
			double value = Econ.calculateTotalLandValue(guild.getLandRounded());
			double refund = value * Conf.econClaimRefundMultiplier;
			if (value > 0)
			{
				String stringValue = Econ.moneyString(value);
				String stringRefund = (refund > 0.0) ? (" ("+Econ.moneyString(refund)+" depreciated)") : "";
				msg("<a>Total land value: <i>" + stringValue + stringRefund);
			}
			
			//Show bank contents
			if(Conf.bankEnabled)
			{
				msg("<a>Bank contains: <i>"+Econ.moneyString(Econ.getBalance(guild.getAccountId())));
			}
		}

		String sepparator = p.txt.parse("<i>")+", ";
		
		// List the relations to other guilds
		Map<Rel, List<String>> relationTags = guild.getguildTagsPerRelation(fme);
		
		if (guild.getFlag(FFlag.PEACEFUL))
		{
			sendMessage(p.txt.parse("<a>In Truce with:<i> *everyone*"));
		}
		else
		{
			sendMessage(p.txt.parse("<a>In Truce with: ") + TextUtil.implode(relationTags.get(Rel.TRUCE), sepparator));
		}
		
		sendMessage(p.txt.parse("<a>Allied to: ") + TextUtil.implode(relationTags.get(Rel.ALLY), sepparator));
		sendMessage(p.txt.parse("<a>Enemies: ") + TextUtil.implode(relationTags.get(Rel.ENEMY), sepparator));
		
		// List the members...
		List<String> memberOnlineNames = new ArrayList<String>();
		List<String> memberOfflineNames = new ArrayList<String>();
		
		for (FPlayer follower : admins)
		{
			if (follower.isOnlineAndVisibleTo(me))
			{
				memberOnlineNames.add(follower.getNameAndTitle(fme));
			}
			else
			{
				memberOfflineNames.add(follower.getNameAndTitle(fme));
			}
		}
		
		for (FPlayer follower : mods)
		{
			if (follower.isOnlineAndVisibleTo(me))
			{
				memberOnlineNames.add(follower.getNameAndTitle(fme));
			}
			else
			{
				memberOfflineNames.add(follower.getNameAndTitle(fme));
			}
		}
		
		for (FPlayer follower : normals)
		{
			if (follower.isOnlineAndVisibleTo(me))
			{
				memberOnlineNames.add(follower.getNameAndTitle(fme));
			}
			else
			{
				memberOfflineNames.add(follower.getNameAndTitle(fme));
			}
		}
		
		sendMessage(p.txt.parse("<a>Members online: ") + TextUtil.implode(memberOnlineNames, sepparator));
		sendMessage(p.txt.parse("<a>Members offline: ") + TextUtil.implode(memberOfflineNames, sepparator));
	}
	
}
