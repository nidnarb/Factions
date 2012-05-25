package com.massivecraft.guilds.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.massivecraft.guilds.Board;
import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FLocation;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.integration.EssentialsFeatures;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.zcore.util.SmokeUtil;


public class CmdHome extends FCommand
{
	
	public CmdHome()
	{
		super();
		this.aliases.add("home");
		
		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.HOME.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		// TODO: Hide this command on help also.
		if ( ! Conf.homesEnabled)
		{
			fme.msg("<b>Sorry, guild homes are disabled on this server.");
			return;
		}

		if ( ! Conf.homesTeleportCommandEnabled)
		{
			fme.msg("<b>Sorry, the ability to teleport to guild homes is disabled on this server.");
			return;
		}
		
		if ( ! myguild.hasHome())
		{
			fme.msg("<b>Your guild does not have a home. " + (fme.getRole().isLessThan(Rel.OFFICER) ? "<i> Ask your leader to:" : "<i>You should:"));
			fme.sendMessage(p.cmdBase.cmdSethome.getUseageTemplate());
			return;
		}
		
		if ( ! Conf.homesTeleportAllowedFromEnemyTerritory && fme.isInEnemyTerritory())
		{
			fme.msg("<b>You cannot teleport to your guild home while in the territory of an enemy guild.");
			return;
		}
		
		if ( ! Conf.homesTeleportAllowedFromDifferentWorld && me.getWorld().getUID() != myguild.getHome().getWorld().getUID())
		{
			fme.msg("<b>You cannot teleport to your guild home while in a different world.");
			return;
		}
		
		guild guild = Board.getguildAt(new FLocation(me.getLocation()));
		Location loc = me.getLocation().clone();
		
		// if player is not in a safe zone or their own guild territory, only allow teleport if no enemies are nearby
		if
		(
			Conf.homesTeleportAllowedEnemyDistance > 0
			&&
			guild.getFlag(FFlag.PVP)
			&&
			(
				! fme.isInOwnTerritory()
				||
				(
					fme.isInOwnTerritory()
					&&
					! Conf.homesTeleportIgnoreEnemiesIfInOwnTerritory
				)
			)
		)
		{
			World w = loc.getWorld();
			double x = loc.getX();
			double y = loc.getY();
			double z = loc.getZ();

			for (Player p : me.getServer().getOnlinePlayers())
			{
				if (p == null || !p.isOnline() || p.isDead() || p == fme || p.getWorld() != w)
					continue;

				FPlayer fp = FPlayers.i.get(p);
				if (fme.getRelationTo(fp) != Rel.ENEMY)
					continue;

				Location l = p.getLocation();
				double dx = Math.abs(x - l.getX());
				double dy = Math.abs(y - l.getY());
				double dz = Math.abs(z - l.getZ());
				double max = Conf.homesTeleportAllowedEnemyDistance;

				// box-shaped distance check
				if (dx > max || dy > max || dz > max)
					continue;

				fme.msg("<b>You cannot teleport to your guild home while an enemy is within " + Conf.homesTeleportAllowedEnemyDistance + " blocks of you.");
				return;
			}
		}

		// if Essentials teleport handling is enabled and available, pass the teleport off to it (for delay and cooldown)
		if (EssentialsFeatures.handleTeleport(me, myguild.getHome())) return;

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostHome, "to teleport to your guild home", "for teleporting to your guild home")) return;

		// Create a smoke effect
		if (Conf.homesTeleportCommandSmokeEffectEnabled)
		{
			List<Location> smokeLocations = new ArrayList<Location>();
			smokeLocations.add(loc);
			smokeLocations.add(loc.add(0, 1, 0));
			smokeLocations.add(myguild.getHome());
			smokeLocations.add(myguild.getHome().clone().add(0, 1, 0));
			SmokeUtil.spawnCloudRandom(smokeLocations, 3f);
		}

		me.teleport(myguild.getHome());
	}
	
}
