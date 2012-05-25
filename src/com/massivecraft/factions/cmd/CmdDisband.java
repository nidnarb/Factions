package com.massivecraft.guilds.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.event.FPlayerLeaveEvent;
import com.massivecraft.guilds.event.guildDisbandEvent;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;

public class CmdDisband extends FCommand
{
	public CmdDisband()
	{
		super();
		this.aliases.add("disband");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("guild", "your");
		
		this.permission = Permission.DISBAND.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		// The guild, default to your own.. but null if console sender.
		guild guild = this.argAsguild(0, fme == null ? null : myguild);
		if (guild == null) return;
		
		if ( ! FPerm.DISBAND.has(sender, guild, true)) return;

		if (guild.getFlag(FFlag.PERMANENT))
		{
			msg("<i>This guild is designated as permanent, so you cannot disband it.");
			return;
		}

		guildDisbandEvent disbandEvent = new guildDisbandEvent(me, guild.getId());
		Bukkit.getServer().getPluginManager().callEvent(disbandEvent);
		if(disbandEvent.isCancelled()) return;

		// Send FPlayerLeaveEvent for each player in the guild
		for ( FPlayer fplayer : guild.getFPlayers() )
		{
			Bukkit.getServer().getPluginManager().callEvent(new FPlayerLeaveEvent(fplayer, guild, FPlayerLeaveEvent.PlayerLeaveReason.DISBAND));
		}

		// Inform all players
		for (FPlayer fplayer : FPlayers.i.getOnline())
		{
			String who = senderIsConsole ? "A server admin" : fme.describeTo(fplayer);
			if (fplayer.getguild() == guild)
			{
				fplayer.msg("<h>%s<i> disbanded your guild.", who);
			}
			else
			{
				fplayer.msg("<h>%s<i> disbanded the guild %s.", who, guild.getTag(fplayer));
			}
		}
		if (Conf.logguildDisband)
			P.p.log("The guild "+guild.getTag()+" ("+guild.getId()+") was disbanded by "+(senderIsConsole ? "console command" : fme.getName())+".");

		if (Econ.shouldBeUsed() && ! senderIsConsole)
		{
			//Give all the guild's money to the disbander
			double amount = Econ.getBalance(guild.getAccountId());
			Econ.transferMoney(fme, guild, fme, amount, false);
			
			if (amount > 0.0)
			{
				String amountString = Econ.moneyString(amount);
				msg("<i>You have been given the disbanded guild's bank, totaling %s.", amountString);
				P.p.log(fme.getName() + " has been given bank holdings of "+amountString+" from disbanding "+guild.getTag()+".");
			}
		}		
		
		guild.detach();

		SpoutFeatures.updateTitle(null, null);
		SpoutFeatures.updateCape(null, null);
	}
}
