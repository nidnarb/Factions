package com.massivecraft.guilds;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.zcore.persist.PlayerEntityCollection;

public class FPlayers extends PlayerEntityCollection<FPlayer>
{
	public static FPlayers i = new FPlayers();
	
	P p = P.p;
	
	private FPlayers()
	{
		super
		(
			FPlayer.class,
			new CopyOnWriteArrayList<FPlayer>(),
			new ConcurrentSkipListMap<String, FPlayer>(String.CASE_INSENSITIVE_ORDER),
			new File(P.p.getDataFolder(), "players.json"),
			P.p.gson
		);
		
		this.setCreative(true);
	}
	
	@Override
	public Type getMapType()
	{
		return new TypeToken<Map<String, FPlayer>>(){}.getType();
	}
	
	public void clean()
	{
		for (FPlayer fplayer : this.get())
		{
			if ( ! guilds.i.exists(fplayer.getguildId()))
			{
				p.log("Reset guild data (invalid guild) for player "+fplayer.getName());
				fplayer.resetguildData(false);
			}
		}
	}
	
	public void autoLeaveOnInactivityRoutine()
	{
		if (Conf.autoLeaveAfterDaysOfInactivity <= 0.0)
		{
			return;
		}

		long now = System.currentTimeMillis();
		double toleranceMillis = Conf.autoLeaveAfterDaysOfInactivity * 24 * 60 * 60 * 1000;
		
		for (FPlayer fplayer : FPlayers.i.get())
		{
			if (fplayer.isOffline() && now - fplayer.getLastLoginTime() > toleranceMillis)
			{
				if (Conf.logguildLeave || Conf.logguildKick)
					P.p.log("Player "+fplayer.getName()+" was auto-removed due to inactivity.");

				// if player is guild leader, sort out the guild since he's going away
				if (fplayer.getRole() == Rel.LEADER)
				{
					guild guild = fplayer.getguild();
					if (guild != null)
						fplayer.getguild().promoteNewLeader();
				}

				fplayer.leave(false);
				fplayer.detach();
			}
		}
	}
}
