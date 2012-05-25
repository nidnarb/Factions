package com.massivecraft.guilds.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.FPlayer;
import org.bukkit.entity.Player;

public class LandUnclaimAllEvent extends Event
{	
	private static final HandlerList handlers = new HandlerList();

	private guild guild;
	private FPlayer fplayer;

	public LandUnclaimAllEvent(guild g, FPlayer p)
	{
		guild = g;
		fplayer = p;
	}

	public HandlerList getHandlers() 
	{
		return handlers;
	}

	public static HandlerList getHandlerList() 
	{
		return handlers;
	}

	public guild getguild()
	{
		return guild;
	}

	public String getguildId()
	{
		return guild.getId();
	}

	public String getguildTag()
	{
		return guild.getTag();
	}

	public FPlayer getFPlayer()
	{
		return fplayer;
	}

	public Player getPlayer()
	{
		return fplayer.getPlayer();
	}
}
