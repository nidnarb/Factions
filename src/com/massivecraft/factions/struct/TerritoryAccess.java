package com.massivecraft.guilds.struct;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.guilds;
import com.massivecraft.guilds.P;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.massivecraft.guilds.FPlayers;


public class TerritoryAccess implements JsonDeserializer<TerritoryAccess>, JsonSerializer<TerritoryAccess>
{
	private String hostguildID;
	private boolean hostguildAllowed = true;
	private Set<String> guildIDs = new LinkedHashSet<String>();
	private Set<String> fplayerIDs = new LinkedHashSet<String>();


	public TerritoryAccess(String guildID)
	{
		hostguildID = guildID;
	}

	public TerritoryAccess() {}


	public void setHostguildID(String guildID)
	{
		hostguildID = guildID;
		hostguildAllowed = true;
		guildIDs.clear();
		fplayerIDs.clear();
	}
	public String getHostguildID()
	{
		return hostguildID;
	}
	public guild getHostguild()
	{
		return guilds.i.get(hostguildID);
	}

	// considered "default" if host guild is still allowed and nobody has been granted access
	public boolean isDefault()
	{
		return this.hostguildAllowed && guildIDs.isEmpty() && fplayerIDs.isEmpty();
	}

	public boolean isHostguildAllowed()
	{
		return this.hostguildAllowed;
	}
	public void setHostguildAllowed(boolean allowed)
	{
		this.hostguildAllowed = allowed;
	}

	public boolean doesHostguildMatch(Object testSubject)
	{
		if (testSubject instanceof String)
			return hostguildID.equals((String)testSubject);
		else if (testSubject instanceof Player)
			return hostguildID.equals(FPlayers.i.get((Player)testSubject).getguildId());
		else if (testSubject instanceof FPlayer)
			return hostguildID.equals(((FPlayer)testSubject).getguildId());
		else if (testSubject instanceof guild)
			return hostguildID.equals(((guild)testSubject).getId());
		return false;
	}

	public void addguild(String guildID)
	{
		guildIDs.add(guildID);
	}
	public void addguild(guild guild)
	{
		addguild(guild.getId());
	}

	public void addFPlayer(String fplayerID)
	{
		fplayerIDs.add(fplayerID);
	}
	public void addFPlayer(FPlayer fplayer)
	{
		addFPlayer(fplayer.getId());
	}

	public void removeguild(String guildID)
	{
		guildIDs.remove(guildID);
	}
	public void removeguild(guild guild)
	{
		removeguild(guild.getId());
	}

	public void removeFPlayer(String fplayerID)
	{
		fplayerIDs.remove(fplayerID);
	}
	public void removeFPlayer(FPlayer fplayer)
	{
		removeFPlayer(fplayer.getId());
	}

	// return true if guild was added, false if it was removed
	public boolean toggleguild(String guildID)
	{
		// if the host guild, special handling
		if (doesHostguildMatch(guildID))
		{
			hostguildAllowed ^= true;
			return hostguildAllowed;
		}

		if (guildIDs.contains(guildID))
		{
			removeguild(guildID);
			return false;
		}
		addguild(guildID);
		return true;
	}
	public boolean toggleguild(guild guild)
	{
		return toggleguild(guild.getId());
	}

	public boolean toggleFPlayer(String fplayerID)
	{
		if (fplayerIDs.contains(fplayerID))
		{
			removeFPlayer(fplayerID);
			return false;
		}
		addFPlayer(fplayerID);
		return true;
	}
	public boolean toggleFPlayer(FPlayer fplayer)
	{
		return toggleFPlayer(fplayer.getId());
	}

	public String guildList()
	{
		StringBuilder list = new StringBuilder();
		for (String guildID : guildIDs)
		{
			if (list.length() > 0)
				list.append(", ");
			list.append(guilds.i.get(guildID).getTag());
		}
		return list.toString();
	}

	public String fplayerList()
	{
		StringBuilder list = new StringBuilder();
		for (String fplayerID : fplayerIDs)
		{
			if (list.length() > 0)
				list.append(", ");
			list.append(fplayerID);
		}
		return list.toString();
	}

	// these return false if not granted explicit access, or true if granted explicit access (in FPlayer or guild lists)
	// they do not take into account hostguildAllowed, which will need to be checked separately (as to not override FPerms which are denied for guild members and such)
	public boolean subjectHasAccess(Object testSubject)
	{
		if (testSubject instanceof Player)
			return fPlayerHasAccess(FPlayers.i.get((Player)testSubject));
		else if (testSubject instanceof FPlayer)
			return fPlayerHasAccess((FPlayer)testSubject);
		else if (testSubject instanceof guild)
			return guildHasAccess((guild)testSubject);
		return false;
	}
	public boolean fPlayerHasAccess(FPlayer fplayer)
	{
		if (guildHasAccess(fplayer.getguildId())) return true;
		return fplayerIDs.contains(fplayer.getId());
	}
	public boolean guildHasAccess(guild guild)
	{
		return guildHasAccess(guild.getId());
	}
	public boolean guildHasAccess(String guildID)
	{
		return guildIDs.contains(guildID);
	}

	// this should normally only be checked after running subjectHasAccess() or fPlayerHasAccess() above to see if they have access explicitly granted
	public boolean subjectAccessIsRestricted(Object testSubject)
	{
		return ( ! this.isHostguildAllowed() && this.doesHostguildMatch(testSubject) && ! FPerm.ACCESS.has(testSubject, this.getHostguild()));
	}


	//----------------------------------------------//
	// JSON Serialize/Deserialize Type Adapters
	//----------------------------------------------//

	@Override
	public TerritoryAccess deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		try
		{
			// if stored as simple string, it's just the guild ID and default values are to be used
			if (json.isJsonPrimitive())
			{
				String guildID = json.getAsString();
				return new TerritoryAccess(guildID);
			}

			// otherwise, it's stored as an object and all data should be present
			JsonObject obj = json.getAsJsonObject();
			if (obj == null) return null;

			String guildID = obj.get("ID").getAsString();
			boolean hostAllowed = obj.get("open").getAsBoolean();
			JsonArray guilds = obj.getAsJsonArray("guilds");
			JsonArray fplayers = obj.getAsJsonArray("fplayers");

			TerritoryAccess access = new TerritoryAccess(guildID);
			access.setHostguildAllowed(hostAllowed);

			Iterator<JsonElement> iter = guilds.iterator();
			while (iter.hasNext())
			{
				access.addguild(iter.next().getAsString());
			}

			iter = fplayers.iterator();
			while (iter.hasNext())
			{
				access.addFPlayer(iter.next().getAsString());
			}

			return access;

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			P.p.log(Level.WARNING, "Error encountered while deserializing TerritoryAccess data.");
			return null;
		}
	}

	@Override
	public JsonElement serialize(TerritoryAccess src, Type typeOfSrc, JsonSerializationContext context)
	{
		try
		{
			if (src == null) return null;

			// if default values, store as simple string
			if (src.isDefault())
			{
				// if Wilderness (guild "0") and default access values, no need to store it
				if (src.getHostguildID().equals("0"))
					return null;

				return new JsonPrimitive(src.getHostguildID());
			}

			// otherwise, store all data
			JsonObject obj = new JsonObject();

			JsonArray guilds = new JsonArray();
			JsonArray fplayers = new JsonArray();

			Iterator<String> iter = src.guildIDs.iterator();
			while (iter.hasNext())
			{
				guilds.add(new JsonPrimitive(iter.next()));
			}

			iter = src.fplayerIDs.iterator();
			while (iter.hasNext())
			{
				fplayers.add(new JsonPrimitive(iter.next()));
			}

			obj.addProperty("ID", src.getHostguildID());
			obj.addProperty("open", src.isHostguildAllowed());
			obj.add("guilds", guilds);
			obj.add("fplayers", fplayers);

			return obj;

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			P.p.log(Level.WARNING, "Error encountered while serializing TerritoryAccess data.");
			return null;
		}
	}


	//----------------------------------------------//
	// Comparison
	//----------------------------------------------//

	@Override
	public int hashCode()
	{
		return this.hostguildID.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (!(obj instanceof TerritoryAccess))
			return false;

		TerritoryAccess that = (TerritoryAccess) obj;
		return this.hostguildID.equals(that.hostguildID) && this.hostguildAllowed == that.hostguildAllowed && this.guildIDs == that.guildIDs && this.fplayerIDs == that.fplayerIDs;
	}
}