package com.massivecraft.guilds.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.FLocation;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.FPlayer;
import org.bukkit.entity.Player;

public class LandClaimEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;
	private FLocation location;
	private guild guild;
	private FPlayer fplayer;

	public LandClaimEvent(FLocation loc, guild g, FPlayer p)
	{
		cancelled = false;
		location = loc;
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

	public FLocation getLocation()
	{
		return this.location;
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

	@Override
	public boolean isCancelled() 
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean c) 
	{
		this.cancelled = c;
	}
}
