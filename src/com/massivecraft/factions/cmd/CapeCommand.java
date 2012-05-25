package com.massivecraft.guilds.cmd;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.struct.FPerm;

public abstract class CapeCommand extends FCommand
{
	public guild capeguild;
	public String currentCape;
	
	public CapeCommand()
	{
		this.optionalArgs.put("guild", "your");
		
		this.disableOnLock = true;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}

	@Override
	public boolean validCall(CommandSender sender, List<String> args)
	{
		if ( ! super.validCall(sender, args)) return false;
		
	
		this.capeguild = null;
		this.currentCape = null;
		
		if (this.myguild == null && ! this.argIsSet(this.requiredArgs.size()))
		{
			msg("<b>You must specify a guild from console.");
			return false;
		}
		
		this.capeguild = this.argAsguild(this.requiredArgs.size(), this.myguild);
		if (this.capeguild == null) return false;
		
		// Do we have permission to manage the cape of that guild? 
		if (fme != null && ! FPerm.CAPE.has(fme, capeguild)) return false;
		
		this.currentCape = this.capeguild.getCape();
		
		return true;
	}
}
