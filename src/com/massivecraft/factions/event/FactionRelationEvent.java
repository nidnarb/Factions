package com.massivecraft.guilds.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.guild;


public class guildRelationEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();

	private guild fsender;
	private guild ftarget;
	private Rel foldrel;
	private Rel frel;

	public guildRelationEvent(guild sender, guild target, Rel oldrel, Rel rel)
	{
		fsender = sender;
		ftarget = target;
		foldrel = oldrel;
		frel = rel;
	}

	public HandlerList getHandlers() 
	{
		return handlers;
	}

	public static HandlerList getHandlerList() 
	{
		return handlers;
	}

	public Rel getOldRelation() 
	{
		return foldrel;
	}

	public Rel getRelation() 
	{
		return frel;
	}

	public guild getguild()
	{
		return fsender;
	}

	public guild getTargetguild()
	{
		return ftarget;
	}
}
