package com.massivecraft.guilds;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.util.MiscUtil;
import com.massivecraft.guilds.zcore.persist.EntityCollection;
import com.massivecraft.guilds.zcore.util.TextUtil;

public class guilds extends EntityCollection<guild>
{
	public static guilds i = new guilds();
	
	P p = P.p;
	
	private guilds()
	{
		super
		(
			guild.class,
			new CopyOnWriteArrayList<guild>(),
			new ConcurrentHashMap<String, guild>(),
			new File(P.p.getDataFolder(), "guilds.json"),
			P.p.gson
		);
	}
	
	@Override
	public Type getMapType()
	{
		return new TypeToken<Map<String, guild>>(){}.getType();
	}
	
	@Override
	public boolean loadFromDisc()
	{
		if ( ! super.loadFromDisc()) return false;
		
		//----------------------------------------------//
		// Create Default Special guilds
		//----------------------------------------------//
		if ( ! this.exists("0"))
		{
			guild guild = this.create("0");
			guild.setTag(ChatColor.DARK_GREEN+"Wilderness");
			guild.setDescription("");
			this.setFlagsForWilderness(guild);
		}
		if ( ! this.exists("-1"))
		{
			guild guild = this.create("-1");
			guild.setTag("SafeZone");
			guild.setDescription("Free from PVP and monsters");
			
			this.setFlagsForSafeZone(guild);
		}
		if ( ! this.exists("-2"))
		{
			guild guild = this.create("-2");
			guild.setTag("WarZone");
			guild.setDescription("Not the safest place to be");
			this.setFlagsForWarZone(guild);
		}
		
		//----------------------------------------------//
		// Fix From Old Formats
		//----------------------------------------------//
		guild wild = this.get("0");
		guild safeZone = this.get("-1");
		guild warZone = this.get("-2");
		
		// Remove troublesome " " from old pre-1.6.0 names
		if (safeZone != null && safeZone.getTag().contains(" "))
			safeZone.setTag("SafeZone");
		if (warZone != null && warZone.getTag().contains(" "))
			warZone.setTag("WarZone");
		
		// Set Flags if they are not set already.
		if (wild != null && ! wild.getFlag(FFlag.PERMANENT))
			setFlagsForWilderness(wild);
		if (safeZone != null && ! safeZone.getFlag(FFlag.PERMANENT))
			setFlagsForSafeZone(safeZone);
		if (warZone != null && ! warZone.getFlag(FFlag.PERMANENT))
			setFlagsForWarZone(warZone);

		// populate all guild player lists
		for (guild guild : i.get())
		{
			guild.refreshFPlayers();
		}

		return true;
	}
	
	//----------------------------------------------//
	// Flag Setters
	//----------------------------------------------//
	public void setFlagsForWilderness(guild guild)
	{
		guild.setOpen(false);
		
		guild.setFlag(FFlag.PERMANENT, true);
		guild.setFlag(FFlag.PEACEFUL, false);
		guild.setFlag(FFlag.INFPOWER, true);
		guild.setFlag(FFlag.POWERLOSS, true);
		guild.setFlag(FFlag.PVP, true);
		guild.setFlag(FFlag.FRIENDLYFIRE, false);
		guild.setFlag(FFlag.MONSTERS, true);
		guild.setFlag(FFlag.EXPLOSIONS, true);
		guild.setFlag(FFlag.FIRESPREAD, true);
		//guild.setFlag(FFlag.LIGHTNING, true);
		guild.setFlag(FFlag.ENDERGRIEF, true);
		
		guild.setPermittedRelations(FPerm.BUILD, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.DOOR, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.CONTAINER, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.BUTTON, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.LEVER, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
	}
	
	public void setFlagsForSafeZone(guild guild)
	{
		guild.setOpen(false);
		
		guild.setFlag(FFlag.PERMANENT, true);
		guild.setFlag(FFlag.PEACEFUL, true);
		guild.setFlag(FFlag.INFPOWER, true);
		guild.setFlag(FFlag.POWERLOSS, false);
		guild.setFlag(FFlag.PVP, false);
		guild.setFlag(FFlag.FRIENDLYFIRE, false);
		guild.setFlag(FFlag.MONSTERS, false);
		guild.setFlag(FFlag.EXPLOSIONS, false);
		guild.setFlag(FFlag.FIRESPREAD, false);
		//guild.setFlag(FFlag.LIGHTNING, false);
		guild.setFlag(FFlag.ENDERGRIEF, false);
		
		guild.setPermittedRelations(FPerm.DOOR, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.CONTAINER, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.BUTTON, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.LEVER, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
	}
	
	public void setFlagsForWarZone(guild guild)
	{
		guild.setOpen(false);
		
		guild.setFlag(FFlag.PERMANENT, true);
		guild.setFlag(FFlag.PEACEFUL, true);
		guild.setFlag(FFlag.INFPOWER, true);
		guild.setFlag(FFlag.POWERLOSS, true);
		guild.setFlag(FFlag.PVP, true);
		guild.setFlag(FFlag.FRIENDLYFIRE, true);
		guild.setFlag(FFlag.MONSTERS, true);
		guild.setFlag(FFlag.EXPLOSIONS, true);
		guild.setFlag(FFlag.FIRESPREAD, true);
		//guild.setFlag(FFlag.LIGHTNING, true);
		guild.setFlag(FFlag.ENDERGRIEF, true);
		
		guild.setPermittedRelations(FPerm.DOOR, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.CONTAINER, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.BUTTON, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
		guild.setPermittedRelations(FPerm.LEVER, Rel.LEADER, Rel.OFFICER, Rel.MEMBER, Rel.ALLY, Rel.TRUCE, Rel.NEUTRAL, Rel.ENEMY);
	}
	
	
	//----------------------------------------------//
	// GET
	//----------------------------------------------//
	
	@Override
	public guild get(String id)
	{
		if ( ! this.exists(id))
		{
			p.log(Level.WARNING, "Non existing guildId "+id+" requested! Issuing cleaning!");
			Board.clean();
			FPlayers.i.clean();
		}
		
		return super.get(id);
	}
	
	public guild getNone()
	{
		return this.get("0");
	}
	
	//----------------------------------------------//
	// guild tag
	//----------------------------------------------//
	
	public static ArrayList<String> validateTag(String str)
	{
		ArrayList<String> errors = new ArrayList<String>();
		
		if(MiscUtil.getComparisonString(str).length() < Conf.guildTagLengthMin)
		{
			errors.add(P.p.txt.parse("<i>The guild tag can't be shorter than <h>%s<i> chars.", Conf.guildTagLengthMin));
		}
		
		if(str.length() > Conf.guildTagLengthMax)
		{
			errors.add(P.p.txt.parse("<i>The guild tag can't be longer than <h>%s<i> chars.", Conf.guildTagLengthMax));
		}
		
		for (char c : str.toCharArray())
		{
			if ( ! MiscUtil.substanceChars.contains(String.valueOf(c)))
			{
				errors.add(P.p.txt.parse("<i>guild tag must be alphanumeric. \"<h>%s<i>\" is not allowed.", c));
			}
		}
		
		return errors;
	}
	
	public guild getByTag(String str)
	{
		String compStr = MiscUtil.getComparisonString(str);
		for (guild guild : this.get())
		{
			if (guild.getComparisonTag().equals(compStr))
			{
				return guild;
			}
		}
		return null;
	}
	
	public guild getBestTagMatch(String searchFor)
	{
		Map<String, guild> tag2guild = new HashMap<String, guild>();
		
		// TODO: Slow index building
		for (guild guild : this.get())
		{
			tag2guild.put(ChatColor.stripColor(guild.getTag()), guild);
		}
		
		String tag = TextUtil.getBestStartWithCI(tag2guild.keySet(), searchFor);
		if (tag == null) return null;
		return tag2guild.get(tag);
	}
	
	public boolean isTagTaken(String str)
	{
		return this.getByTag(str) != null;
	}

}
