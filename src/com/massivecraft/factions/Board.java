package com.massivecraft.guilds;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.guilds.integration.LWCFeatures;
import com.massivecraft.guilds.iface.RelationParticipator;
import com.massivecraft.guilds.struct.TerritoryAccess;
import com.massivecraft.guilds.util.AsciiCompass;
import com.massivecraft.guilds.zcore.util.DiscUtil;


public class Board
{
	private static transient File file = new File(P.p.getDataFolder(), "board.json");
	private static transient HashMap<FLocation, TerritoryAccess> flocationIds = new HashMap<FLocation, TerritoryAccess>();
	
	//----------------------------------------------//
	// Get and Set
	//----------------------------------------------//
	public static String getIdAt(FLocation flocation)
	{
		if ( ! flocationIds.containsKey(flocation)) return "0";
		
		return flocationIds.get(flocation).getHostguildID();
	}

	public static TerritoryAccess getTerritoryAccessAt(FLocation flocation)
	{
		if ( ! flocationIds.containsKey(flocation))
			flocationIds.put(flocation, new TerritoryAccess("0"));
		return flocationIds.get(flocation);
	}

	public static guild getguildAt(FLocation flocation)
	{
		return guilds.i.get(getIdAt(flocation));
	}
	public static guild getguildAt(Location location)
	{
		return getguildAt(new FLocation(location));
	}
	public static guild getguildAt(Block block)
	{
		return getguildAt(new FLocation(block));
	}
	
	public static void setIdAt(String id, FLocation flocation)
	{
		if (id == "0")
			removeAt(flocation);

		flocationIds.put(flocation, new TerritoryAccess(id));
	}
	
	public static void setguildAt(guild guild, FLocation flocation)
	{
		setIdAt(guild.getId(), flocation);
	}
	
	public static void removeAt(FLocation flocation)
	{
		if(Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled())
			LWCFeatures.clearAllChests(flocation);

		flocationIds.remove(flocation);
	}
	
	public static void unclaimAll(String guildId)
	{
		Iterator<Entry<FLocation, TerritoryAccess>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<FLocation, TerritoryAccess> entry = iter.next();
			if (entry.getValue().getHostguildID().equals(guildId))
			{
					if(Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled())
						LWCFeatures.clearAllChests(entry.getKey());

					iter.remove();
			}
		}
	}

	// Is this coord NOT completely surrounded by coords claimed by the same guild?
	// Simpler: Is there any nearby coord with a guild other than the guild here?
	public static boolean isBorderLocation(FLocation flocation)
	{
		guild guild = getguildAt(flocation);
		FLocation a = flocation.getRelative(1, 0);
		FLocation b = flocation.getRelative(-1, 0);
		FLocation c = flocation.getRelative(0, 1);
		FLocation d = flocation.getRelative(0, -1);
		return guild != getguildAt(a) || guild != getguildAt(b) || guild != getguildAt(c) || guild != getguildAt(d);
	}

	// Is this coord connected to any coord claimed by the specified guild?
	public static boolean isConnectedLocation(FLocation flocation, guild guild)
	{
		FLocation a = flocation.getRelative(1, 0);
		FLocation b = flocation.getRelative(-1, 0);
		FLocation c = flocation.getRelative(0, 1);
		FLocation d = flocation.getRelative(0, -1);
		return guild == getguildAt(a) || guild == getguildAt(b) || guild == getguildAt(c) || guild == getguildAt(d);
	}
	
	
	//----------------------------------------------//
	// Cleaner. Remove orphaned foreign keys
	//----------------------------------------------//
	
	public static void clean()
	{
		Iterator<Entry<FLocation, TerritoryAccess>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<FLocation, TerritoryAccess> entry = iter.next();
			if ( ! guilds.i.exists(entry.getValue().getHostguildID()))
			{
				if(Conf.onUnclaimResetLwcLocks && LWCFeatures.getEnabled())
					LWCFeatures.clearAllChests(entry.getKey());

				P.p.log("Board cleaner removed "+entry.getValue().getHostguildID()+" from "+entry.getKey());
				iter.remove();
			}
		}
	}	
	
	//----------------------------------------------//
	// Coord count
	//----------------------------------------------//
	
	public static int getguildCoordCount(String guildId)
	{
		int ret = 0;
		for (TerritoryAccess thatguildId : flocationIds.values())
		{
			if(thatguildId.getHostguildID().equals(guildId))
			{
				ret += 1;
			}
		}
		return ret;
	}
	
	public static int getguildCoordCount(guild guild)
	{
		return getguildCoordCount(guild.getId());
	}
	
	public static int getguildCoordCountInWorld(guild guild, String worldName)
	{
		String guildId = guild.getId();
		int ret = 0;
		Iterator<Entry<FLocation, TerritoryAccess>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<FLocation, TerritoryAccess> entry = iter.next();
			if (entry.getValue().getHostguildID().equals(guildId) && entry.getKey().getWorldName().equals(worldName))
			{
				ret += 1;
			}
		}
		return ret;
	}
	
	//----------------------------------------------//
	// Map generation
	//----------------------------------------------//
	
	/**
	 * The map is relative to a coord and a guild
	 * north is in the direction of decreasing x
	 * east is in the direction of decreasing z
	 */
	public static ArrayList<String> getMap(RelationParticipator observer, FLocation flocation, double inDegrees)
	{
		ArrayList<String> ret = new ArrayList<String>();
		guild guildLoc = getguildAt(flocation);
		ret.add(P.p.txt.titleize("("+flocation.getCoordString()+") "+guildLoc.getTag(observer)));
		
		int halfWidth = Conf.mapWidth / 2;
		int halfHeight = Conf.mapHeight / 2;
		FLocation topLeft = flocation.getRelative(-halfWidth, -halfHeight);
		int width = halfWidth * 2 + 1;
		int height = halfHeight * 2 + 1;
		
		//Make room for the list of tags
		height--;
		
		
		Map<guild, Character> fList = new HashMap<guild, Character>();
		int chrIdx = 0;
		
		// For each row
		for (int dz = 0; dz < height; dz++)
		{
			// Draw and add that row
			String row = "";
			for (int dx = 0; dx < width; dx++)
			{
				if(dx == halfWidth && dz == halfHeight)
				{
					row += ChatColor.AQUA+"+";
					continue;
				}
			
				FLocation flocationHere = topLeft.getRelative(dx, dz);
				guild guildHere = getguildAt(flocationHere);
				if (guildHere.isNone())
				{
					row += ChatColor.GRAY+"-";
				}
				else
				{
					if (!fList.containsKey(guildHere))
						fList.put(guildHere, Conf.mapKeyChrs[chrIdx++]);
					char fchar = fList.get(guildHere);
					row += guildHere.getColorTo(observer) + "" + fchar;
				}
			}
			ret.add(row);
		}
		
		// Get the compass
		ArrayList<String> asciiCompass = AsciiCompass.getAsciiCompass(inDegrees, ChatColor.RED, P.p.txt.parse("<a>"));

		// Add the compass
		ret.set(1, asciiCompass.get(0)+ret.get(1).substring(3*3));
		ret.set(2, asciiCompass.get(1)+ret.get(2).substring(3*3));
		ret.set(3, asciiCompass.get(2)+ret.get(3).substring(3*3));
			
		String fRow = "";
		for(guild keyguild : fList.keySet())
		{
			fRow += ""+keyguild.getColorTo(observer) + fList.get(keyguild) + ": " + keyguild.getTag() + " ";
		}
		fRow = fRow.trim();
		ret.add(fRow);
		
		return ret;
	}
	
	
	// -------------------------------------------- //
	// Persistance
	// -------------------------------------------- //
	
	public static Map<String,Map<String,TerritoryAccess>> dumpAsSaveFormat()
	{
		Map<String,Map<String,TerritoryAccess>> worldCoordIds = new HashMap<String,Map<String,TerritoryAccess>>(); 
		
		String worldName, coords;
		TerritoryAccess data;
		
		for (Entry<FLocation, TerritoryAccess> entry : flocationIds.entrySet())
		{
			worldName = entry.getKey().getWorldName();
			coords = entry.getKey().getCoordString();
			data = entry.getValue();
			if ( ! worldCoordIds.containsKey(worldName))
			{
				worldCoordIds.put(worldName, new TreeMap<String,TerritoryAccess>());
			}
			
			worldCoordIds.get(worldName).put(coords, data);
		}
		
		return worldCoordIds;
	}
	
	public static void loadFromSaveFormat(Map<String,Map<String,TerritoryAccess>> worldCoordIds)
	{
		flocationIds.clear();
		
		String worldName;
		String[] coords;
		int x, z;
		TerritoryAccess data;
		
		for (Entry<String,Map<String,TerritoryAccess>> entry : worldCoordIds.entrySet())
		{
			worldName = entry.getKey();
			for (Entry<String,TerritoryAccess> entry2 : entry.getValue().entrySet())
			{
				coords = entry2.getKey().trim().split("[,\\s]+");
				x = Integer.parseInt(coords[0]);
				z = Integer.parseInt(coords[1]);
				data = entry2.getValue();
				flocationIds.put(new FLocation(worldName, x, z), data);
			}
		}
	}
	
	public static boolean save()
	{
		//guilds.log("Saving board to disk");
		
		try
		{
			DiscUtil.write(file, P.p.gson.toJson(dumpAsSaveFormat()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			P.p.log("Failed to save the board to disk.");
			return false;
		}
		
		return true;
	}
	
	public static boolean load()
	{
		P.p.log("Loading board from disk");
		
		if ( ! file.exists())
		{
			P.p.log("No board to load from disk. Creating new file.");
			save();
			return true;
		}
		
		try
		{
			Type type = new TypeToken<Map<String,Map<String,TerritoryAccess>>>(){}.getType();
			Map<String,Map<String,TerritoryAccess>> worldCoordIds = P.p.gson.fromJson(DiscUtil.read(file), type);
			loadFromSaveFormat(worldCoordIds);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			P.p.log("Failed to load the board from disk.");
			return false;
		}
			
		return true;
	}
}



















