package com.massivecraft.guilds.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.guild;

public class FPlayerLeaveEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	private PlayerLeaveReason reason;
	FPlayer FPlayer;
	guild guild;
	boolean cancelled = false;

	public enum PlayerLeaveReason
	{
		KICKED, DISBAND, RESET, JOINOTHER, LEAVE
	}

	public FPlayerLeaveEvent(FPlayer p, guild f, PlayerLeaveReason r)
	{
		FPlayer = p;
		guild = f;
		reason = r;
	}

	public HandlerList getHandlers() 
	{
		return handlers;
	}

	public static HandlerList getHandlerList() 
	{
		return handlers;
	}
	
	public PlayerLeaveReason getReason() 
	{
		return reason;
	}
	
	public FPlayer getFPlayer()
	{
		return FPlayer;
	}
	
	public guild getguild()
	{
		return guild;
	}

	@Override
	public boolean isCancelled() 
	{
		return cancelled;
	}

	@Override
	public void setCancelled(boolean c) 
	{
		if (reason == PlayerLeaveReason.DISBAND || reason == PlayerLeaveReason.RESET)
		{
			cancelled = false;
			return;
		}
		cancelled = c;
	}
}