package com.massivecraft.guilds.cmd;

import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;

public class CmdPerm extends FCommand
{
	
	public CmdPerm()
	{
		super();
		this.aliases.add("perm");
		
		this.optionalArgs.put("guild", "your");
		this.optionalArgs.put("perm", "all");
		this.optionalArgs.put("relation", "read");
		this.optionalArgs.put("yes/no", "read");
		
		this.permission = Permission.PERM.node;
		this.disableOnLock = true;
		
		this.errorOnToManyArgs = false;
		
		senderMustBePlayer = false;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		guild guild = myguild;
		if (this.argIsSet(0))
		{
			guild = this.argAsguild(0);
		}
		if (guild == null) return;
		
		if ( ! this.argIsSet(1))
		{
			msg(p.txt.titleize("Perms for " + guild.describeTo(fme, true)));
			msg(FPerm.getStateHeaders());
			for (FPerm perm : FPerm.values())
			{
				msg(perm.getStateInfo(guild.getPermittedRelations(perm), true));
			}
			return;
		}
		
		FPerm perm = this.argAsguildPerm(1);
		if (perm == null) return;
		if ( ! this.argIsSet(2))
		{
			msg(p.txt.titleize("Perm for " + guild.describeTo(fme, true)));
			msg(FPerm.getStateHeaders());
			msg(perm.getStateInfo(guild.getPermittedRelations(perm), true));
			return;
		}
		
		// Do the sender have the right to change perms for this guild?
		if ( ! FPerm.PERMS.has(sender, guild, true)) return;
		
		Rel rel = this.argAsRel(2);
		if (rel == null) return;
		
		Boolean val = this.argAsBool(3, null);
		if (val == null) return;
		
		// Do the change
		guild.setRelationPermitted(perm, rel, val);
		
		// The following is to make sure the leader always has the right to change perms if that is our goal.
		if (perm == FPerm.PERMS && FPerm.PERMS.getDefault().contains(Rel.LEADER))
		{
			guild.setRelationPermitted(FPerm.PERMS, Rel.LEADER, true);
		}
		
		msg(p.txt.titleize("Perm for " + guild.describeTo(fme, true)));
		msg(FPerm.getStateHeaders());
		msg(perm.getStateInfo(guild.getPermittedRelations(perm), true));
	}
	
}
