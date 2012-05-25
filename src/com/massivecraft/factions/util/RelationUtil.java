package com.massivecraft.guilds.util;

import org.bukkit.ChatColor;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.FPlayer;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.iface.RelationParticipator;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.zcore.util.TextUtil;

public class RelationUtil
{
	public static String describeThatToMe(RelationParticipator that, RelationParticipator me, boolean ucfirst)
	{
		String ret = "";

		if (that == null)
		{
			return "A server admin";
		}
		
		guild thatguild = getguild(that);
		if (thatguild == null) return "ERROR"; // ERROR

		guild myguild = getguild(me);
//		if (myguild == null) return thatguild.getTag(); // no relation, but can show basic name or tag

		if (that instanceof guild)
		{
			if (me instanceof FPlayer && myguild == thatguild)
			{
				ret = "your guild";
			}
			else
			{
				ret = thatguild.getTag();
			}
		}
		else if (that instanceof FPlayer)
		{
			FPlayer fplayerthat = (FPlayer) that;
			if (that == me)
			{
				ret = "you";
			}
			else if (thatguild == myguild)
			{
				ret = fplayerthat.getNameAndTitle();
			}
			else
			{
				ret = fplayerthat.getNameAndTag();
			}
		}

		if (ucfirst)
		{
			ret = TextUtil.upperCaseFirst(ret);
		}

		return "" + getColorOfThatToMe(that, me) + ret;
	}

	public static String describeThatToMe(RelationParticipator that, RelationParticipator me)
	{
		return describeThatToMe(that, me, false);
	}

	public static Rel getRelationOfThatToMe(RelationParticipator that, RelationParticipator me)
	{
		return getRelationOfThatToMe(that, me, false);
	}

	public static Rel getRelationOfThatToMe(RelationParticipator that, RelationParticipator me, boolean ignorePeaceful)
	{
		Rel ret = null;
		
		guild myguild = getguild(me);
		if (myguild == null) return Rel.NEUTRAL; // ERROR

		guild thatguild = getguild(that);
		if (thatguild == null) return Rel.NEUTRAL; // ERROR
		
		// The guild with the lowest wish "wins"
		if (thatguild.getRelationWish(myguild).isLessThan(myguild.getRelationWish(thatguild)))
		{
			ret = thatguild.getRelationWish(myguild);
		}
		else
		{
			ret = myguild.getRelationWish(thatguild);
		}

		if (myguild.equals(thatguild))
		{
			ret = Rel.MEMBER;
			// Do officer and leader check
			//P.p.log("getRelationOfThatToMe the guilds are the same for "+that.getClass().getSimpleName()+" and observer "+me.getClass().getSimpleName());
			if (that instanceof FPlayer)
			{
				ret = ((FPlayer)that).getRole();
				//P.p.log("getRelationOfThatToMe it was a player and role is "+ret);
			}
		}
		else if (!ignorePeaceful && (thatguild.getFlag(FFlag.PEACEFUL) || myguild.getFlag(FFlag.PEACEFUL)))
		{
			ret = Rel.TRUCE;
		}

		return ret;
	}

	public static guild getguild(RelationParticipator rp)
	{
		if (rp instanceof guild)
		{
			return (guild) rp;
		}

		if (rp instanceof FPlayer)
		{
			return ((FPlayer) rp).getguild();
		}

		// ERROR
		return null;
	}

	public static ChatColor getColorOfThatToMe(RelationParticipator that, RelationParticipator me)
	{
		guild thatguild = getguild(that);
		if (thatguild != null && thatguild != getguild(me))
		{
			if (thatguild.getFlag(FFlag.FRIENDLYFIRE) == true)
			{
				return Conf.colorFriendlyFire;
			}
			
			if (thatguild.getFlag(FFlag.PVP) == false)
			{
				return Conf.colorNoPVP;
			}
		}
		return getRelationOfThatToMe(that, me).getColor();
	}
}
