package com.massivecraft.guilds.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.FPlayers;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.guilds;

public class guildDisbandEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;
	private String id;
	private Player sender;

	public guildDisbandEvent(Player sender, String guildId)
	{
		cancelled = false;
		this.sender = sender;
		this.id = guildId;
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
		return guilds.i.get(id);
	}

	public FPlayer getFPlayer()
	{
		return FPlayers.i.get(sender);
	}

	public Player getPlayer()
	{
		return sender;
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
