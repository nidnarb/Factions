package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FLocation;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.util.SpiralTask;


public class CmdClaim extends FCommand
{
	
	public CmdClaim()
	{
		super();
		this.aliases.add("claim");
		
		//this.requiredArgs.add("");
		this.optionalArgs.put("guild", "your");
		this.optionalArgs.put("radius", "1");
		
		this.permission = Permission.CLAIM.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		// Read and validate input
		final guild forguild = this.argAsguild(0, myguild);
		int radius = this.argAsInt(1, 1);

		if (radius < 1)
		{
			msg("<b>If you specify a radius, it must be at least 1.");
			return;
		}

		if (radius < 2)
		{
			// single chunk
			fme.attemptClaim(forguild, me.getLocation(), true);
		}
		else
		{
			// radius claim
			new SpiralTask(new FLocation(me), radius)
			{
				private int failCount = 0;
				private final int limit = Conf.radiusClaimFailureLimit - 1;

				@Override
				public boolean work()
				{
					boolean success = fme.attemptClaim(forguild, this.currentLocation(), true);
					if (success)
						failCount = 0;
					else if ( ! success && failCount++ >= limit)
					{
						this.stop();
						return false;
					}

					return true;
				}
			};
		}
	}
}
