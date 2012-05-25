package com.massivecraft.guilds.cmd;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.guilds;
import com.massivecraft.guilds.P;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.zcore.MCommand;


public abstract class FCommand extends MCommand<P>
{
	public boolean disableOnLock;
	
	public FPlayer fme;
	public guild myguild;
	public boolean senderMustBeMember;
	public boolean senderMustBeOfficer;
	public boolean senderMustBeLeader;
	
	public boolean isMoneyCommand;
	
	public FCommand()
	{
		super(P.p);
		
		// Due to safety reasons it defaults to disable on lock.
		disableOnLock = true;
		
		// The money commands must be disabled if money should not be used.
		isMoneyCommand = false;
		
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void execute(CommandSender sender, List<String> args, List<MCommand<?>> commandChain)
	{
		if (sender instanceof Player)
		{
			this.fme = FPlayers.i.get((Player)sender);
			this.myguild = this.fme.getguild();
		}
		else
		{
			this.fme = null;
			this.myguild = null;
		}
		super.execute(sender, args, commandChain);
	}
	
	@Override
	public boolean isEnabled()
	{
		if (p.getLocked() && this.disableOnLock)
		{
			msg("<b>guilds was locked by an admin. Please try again later.");
			return false;
		}
		
		if (this.isMoneyCommand && ! Conf.econEnabled)
		{
			msg("<b>guild economy features are disabled on this server.");
			return false;
		}
		
		if (this.isMoneyCommand && ! Conf.bankEnabled)
		{
			msg("<b>The guild bank system is disabled on this server.");
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean validSenderType(CommandSender sender, boolean informSenderIfNot)
	{
		boolean superValid = super.validSenderType(sender, informSenderIfNot);
		if ( ! superValid) return false;
		
		if ( ! (this.senderMustBeMember || this.senderMustBeOfficer || this.senderMustBeLeader)) return true;
		
		if ( ! (sender instanceof Player)) return false;
		
		FPlayer fplayer = FPlayers.i.get((Player)sender);
		
		if ( ! fplayer.hasguild())
		{
			sender.sendMessage(p.txt.parse("<b>You are not member of any guild."));
			return false;
		}
		
		if (this.senderMustBeOfficer && ! fplayer.getRole().isAtLeast(Rel.OFFICER))
		{
			sender.sendMessage(p.txt.parse("<b>Only guild moderators can %s.", this.getHelpShort()));
			return false;
		}
		
		if (this.senderMustBeLeader && ! fplayer.getRole().isAtLeast(Rel.LEADER))
		{
			sender.sendMessage(p.txt.parse("<b>Only guild admins can %s.", this.getHelpShort()));
			return false;
		}
			
		return true;
	}
	
	// -------------------------------------------- //
	// Assertions
	// -------------------------------------------- //

	public boolean assertHasguild()
	{
		if (me == null) return true;
		
		if ( ! fme.hasguild())
		{
			sendMessage("You are not member of any guild.");
			return false;
		}
		return true;
	}

	public boolean assertMinRole(Rel role)
	{
		if (me == null) return true;
		
		if (fme.getRole().isLessThan(role))
		{
			msg("<b>You <h>must be "+role+"<b> to "+this.getHelpShort()+".");
			return false;
		}
		return true;
	}
	
	// -------------------------------------------- //
	// Argument Readers
	// -------------------------------------------- //
	
	// FPLAYER ======================
	public FPlayer strAsFPlayer(String name, FPlayer def, boolean msg)
	{
		FPlayer ret = def;
		
		if (name != null)
		{
			FPlayer fplayer = FPlayers.i.get(name);
			if (fplayer != null)
			{
				ret = fplayer;
			}
		}
		
		if (msg && ret == null)
		{
			this.msg("<b>No player \"<p>%s<b>\" could be found.", name);			
		}
		
		return ret;
	}
	public FPlayer argAsFPlayer(int idx, FPlayer def, boolean msg)
	{
		return this.strAsFPlayer(this.argAsString(idx), def, msg);
	}
	public FPlayer argAsFPlayer(int idx, FPlayer def)
	{
		return this.argAsFPlayer(idx, def, true);
	}
	public FPlayer argAsFPlayer(int idx)
	{
		return this.argAsFPlayer(idx, null);
	}
	
	// BEST FPLAYER MATCH ======================
	public FPlayer strAsBestFPlayerMatch(String name, FPlayer def, boolean msg)
	{
		FPlayer ret = def;
		
		if (name != null)
		{
			FPlayer fplayer = FPlayers.i.getBestIdMatch(name);
			if (fplayer != null)
			{
				ret = fplayer;
			}
		}
		
		if (msg && ret == null)
		{
			this.msg("<b>No player match found for \"<p>%s<b>\".", name);
		}
		
		return ret;
	}
	public FPlayer argAsBestFPlayerMatch(int idx, FPlayer def, boolean msg)
	{
		return this.strAsBestFPlayerMatch(this.argAsString(idx), def, msg);
	}
	public FPlayer argAsBestFPlayerMatch(int idx, FPlayer def)
	{
		return this.argAsBestFPlayerMatch(idx, def, true);
	}
	public FPlayer argAsBestFPlayerMatch(int idx)
	{
		return this.argAsBestFPlayerMatch(idx, null);
	}
	
	// guild ======================
	public guild strAsguild(String name, guild def, boolean msg)
	{
		guild ret = def;
		
		if (name != null)
		{
			guild guild = null;
			
			// First we try an exact match
			if (guild == null)
			{
				guild = guilds.i.getByTag(name);
			}
			
			// Next we match guild tags
			if (guild == null)
			{
				guild = guilds.i.getBestTagMatch(name);
			}
				
			// Next we match player names
			if (guild == null)
			{
				FPlayer fplayer = FPlayers.i.getBestIdMatch(name);
				if (fplayer != null)
				{
					guild = fplayer.getguild();
				}
			}
			
			if (guild != null)
			{
				ret = guild;
			}
		}
		
		if (msg && ret == null)
		{
			this.msg("<b>The guild or player \"<p>%s<b>\" could not be found.", name);
		}
		
		return ret;
	}
	public guild argAsguild(int idx, guild def, boolean msg)
	{
		return this.strAsguild(this.argAsString(idx), def, msg);
	}
	public guild argAsguild(int idx, guild def)
	{
		return this.argAsguild(idx, def, true);
	}
	public guild argAsguild(int idx)
	{
		return this.argAsguild(idx, null);
	}
	
	// guild FLAG ======================
	public FFlag strAsguildFlag(String name, FFlag def, boolean msg)
	{
		FFlag ret = def;
		
		if (name != null)
		{
			FFlag flag = FFlag.parse(name);
			if (flag != null)
			{
				ret = flag;
			}
		}
		
		if (msg && ret == null)
		{
			this.msg("<b>The guild-flag \"<p>%s<b>\" could not be found.", name);
		}
		
		return ret;
	}
	public FFlag argAsguildFlag(int idx, FFlag def, boolean msg)
	{
		return this.strAsguildFlag(this.argAsString(idx), def, msg);
	}
	public FFlag argAsguildFlag(int idx, FFlag def)
	{
		return this.argAsguildFlag(idx, def, true);
	}
	public FFlag argAsguildFlag(int idx)
	{
		return this.argAsguildFlag(idx, null);
	}
	
	// guild PERM ======================
	public FPerm strAsguildPerm(String name, FPerm def, boolean msg)
	{
		FPerm ret = def;
		
		if (name != null)
		{
			FPerm perm = FPerm.parse(name);
			if (perm != null)
			{
				ret = perm;
			}
		}
		
		if (msg && ret == null)
		{
			this.msg("<b>The guild-perm \"<p>%s<b>\" could not be found.", name);
		}
		
		return ret;
	}
	public FPerm argAsguildPerm(int idx, FPerm def, boolean msg)
	{
		return this.strAsguildPerm(this.argAsString(idx), def, msg);
	}
	public FPerm argAsguildPerm(int idx, FPerm def)
	{
		return this.argAsguildPerm(idx, def, true);
	}
	public FPerm argAsguildPerm(int idx)
	{
		return this.argAsguildPerm(idx, null);
	}
	
	// guild REL ======================
	public Rel strAsRel(String name, Rel def, boolean msg)
	{
		Rel ret = def;
		
		if (name != null)
		{
			Rel perm = Rel.parse(name);
			if (perm != null)
			{
				ret = perm;
			}
		}
		
		if (msg && ret == null)
		{
			this.msg("<b>The role \"<p>%s<b>\" could not be found.", name);
		}
		
		return ret;
	}
	public Rel argAsRel(int idx, Rel def, boolean msg)
	{
		return this.strAsRel(this.argAsString(idx), def, msg);
	}
	public Rel argAsRel(int idx, Rel def)
	{
		return this.argAsRel(idx, def, true);
	}
	public Rel argAsRel(int idx)
	{
		return this.argAsRel(idx, null);
	}
	
	// -------------------------------------------- //
	// Commonly used logic
	// -------------------------------------------- //
	
	public boolean canIAdministerYou(FPlayer i, FPlayer you)
	{
		if ( ! i.getguild().equals(you.getguild()))
		{
			i.sendMessage(p.txt.parse("%s <b>is not in the same guild as you.",you.describeTo(i, true)));
			return false;
		}
		
		if (i.getRole().isMoreThan(you.getRole()) || i.getRole().equals(Rel.LEADER) )
		{
			return true;
		}
		
		if (you.getRole().equals(Rel.LEADER))
		{
			i.sendMessage(p.txt.parse("<b>Only the guild admin can do that."));
		}
		else if (i.getRole().equals(Rel.OFFICER))
		{
			if ( i == you )
			{
				return true; //Moderators can control themselves
			}
			else
			{
				i.sendMessage(p.txt.parse("<b>Moderators can't control each other..."));
			}
		}
		else
		{
			i.sendMessage(p.txt.parse("<b>You must be a guild moderator to do that."));
		}
		
		return false;
	}
	
	// if economy is enabled and they're not on the bypass list, make 'em pay; returns true unless person can't afford the cost
	public boolean payForCommand(double cost, String toDoThis, String forDoingThis)
	{
		if ( ! Econ.shouldBeUsed() || this.fme == null || cost == 0.0 || fme.hasAdminMode()) return true;

		if(Conf.bankEnabled && Conf.bankguildPaysCosts && fme.hasguild())
			return Econ.modifyMoney(myguild, -cost, toDoThis, forDoingThis);
		else
			return Econ.modifyMoney(fme, -cost, toDoThis, forDoingThis);
	}

	// like above, but just make sure they can pay; returns true unless person can't afford the cost
	public boolean canAffordCommand(double cost, String toDoThis)
	{
		if ( ! Econ.shouldBeUsed() || this.fme == null || cost == 0.0 || fme.hasAdminMode()) return true;

		if(Conf.bankEnabled && Conf.bankguildPaysCosts && fme.hasguild())
			return Econ.hasAtLeast(myguild, -cost, toDoThis);
		else
			return Econ.hasAtLeast(fme, -cost, toDoThis);
	}
}
