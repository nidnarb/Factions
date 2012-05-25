package com.massivecraft.guilds.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guilds;

public class guildCreateEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
 
	private String guildTag;
	private Player sender;
	private boolean cancelled;
	
	public guildCreateEvent(Player sender, String tag) 
	{
		this.guildTag = tag;
		this.sender = sender;
		this.cancelled = false;
	}
 
	public FPlayer getFPlayer()
	{
		return FPlayers.i.get(sender);
	}
	
	public String getguildId()
	{
		return guilds.i.getNextId();
	}

	public String getguildTag()
	{
		return guildTag;
	}

	public HandlerList getHandlers() 
	{
		return handlers;
	}
 
	public static HandlerList getHandlerList() 
	{
		return handlers;
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