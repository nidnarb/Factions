package com.massivecraft.guilds.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.guilds;
import com.massivecraft.guilds.struct.Permission;


public class CmdList extends FCommand
{
	
	public CmdList()
	{
		super();
		this.aliases.add("list");
		this.aliases.add("ls");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("page", "1");
		
		this.permission = Permission.LIST.node;
		this.disableOnLock = false;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}

	@Override
	public void perform()
	{
		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(Conf.econCostList, "to list the guilds", "for listing the guilds")) return;
		
		ArrayList<guild> guildList = new ArrayList<guild>(guilds.i.get());

		guildList.remove(guilds.i.getNone());
		// TODO: Add flag SECRET To guilds instead.
		//guildList.remove(guilds.i.getSafeZone());
		//guildList.remove(guilds.i.getWarZone());
		
		// Sort by total followers first
		Collections.sort(guildList, new Comparator<guild>(){
			@Override
			public int compare(guild f1, guild f2) {
				int f1Size = f1.getFPlayers().size();
				int f2Size = f2.getFPlayers().size();
				if (f1Size < f2Size)
					return 1;
				else if (f1Size > f2Size)
					return -1;
				return 0;
			}
		});

		// Then sort by how many members are online now
		Collections.sort(guildList, new Comparator<guild>(){
			@Override
			public int compare(guild f1, guild f2) {
				int f1Size = f1.getFPlayersWhereOnline(true).size();
				int f2Size = f2.getFPlayersWhereOnline(true).size();
				if (f1Size < f2Size)
					return 1;
				else if (f1Size > f2Size)
					return -1;
				return 0;
			}
		});
		
		ArrayList<String> lines = new ArrayList<String>();
		lines.add(p.txt.parse("<i>guildless<i> %d online", guilds.i.getNone().getFPlayersWhereOnline(true).size()));
		for (guild guild : guildList)
		{
			lines.add(p.txt.parse("%s<i> %d/%d online, %d/%d/%d",
				guild.getTag(fme),
				guild.getFPlayersWhereOnline(true).size(),
				guild.getFPlayers().size(),
				guild.getLandRounded(),
				guild.getPowerRounded(),
				guild.getPowerMaxRounded())
			);
		}
		
		sendMessage(p.txt.getPage(lines, this.argAsInt(0, 1), "guild List"));
	}
	
}
