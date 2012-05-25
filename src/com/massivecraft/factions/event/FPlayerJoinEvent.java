package com.massivecraft.guilds.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.guild;

public class FPlayerJoinEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();

	FPlayer fplayer;
	guild guild;
	PlayerJoinReason reason;
	boolean cancelled = false;
	public enum PlayerJoinReason
	{
		CREATE, LEADER, COMMAND
	}
	public FPlayerJoinEvent(FPlayer fp, guild f, PlayerJoinReason r)
	{ 
		fplayer = fp;
		guild = f;
		reason = r;
	}

	public FPlayer getFPlayer()
	{
		return fplayer;
	}
	public guild getguild()
	{
		return guild;
	}
	public PlayerJoinReason getReason()
	{
		return reason;	
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
		cancelled = c;
	}
}