package com.massivecraft.guilds.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.guild;

public class guildRenameEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();

	private boolean cancelled;
	private FPlayer fplayer;
	private guild guild;
	private String tag;

	public guildRenameEvent(FPlayer sender, String newTag) 
	{
		fplayer = sender;
		guild = sender.getguild();
		tag = newTag;
		this.cancelled = false;
	}

	public guild getguild()
	{
		return(guild);
	}

	public FPlayer getFPlayer()
	{
		return(fplayer);
	}

	public Player getPlayer()
	{
		return(fplayer.getPlayer());
	}

	public String getOldguildTag()
	{
		return(guild.getTag());
	}

	public String getguildTag()
	{
		return(tag);
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
