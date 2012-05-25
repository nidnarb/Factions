package com.massivecraft.guilds.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.guilds.Conf;
import com.massivecraft.guilds.guild;
import com.massivecraft.guilds.event.guildRelationEvent;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.Permission;
import com.massivecraft.guilds.struct.Rel;

public abstract class FRelationCommand extends FCommand
{
	public Rel targetRelation;
	
	public FRelationCommand()
	{
		super();
		this.requiredArgs.add("guild");
		//this.optionalArgs.put("", "");
		
		this.permission = Permission.RELATION.node;
		this.disableOnLock = true;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = true;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		guild them = this.argAsguild(0);
		if (them == null) return;
		
		/*if ( ! them.isNormal())
		{
			msg("<b>Nope! You can't.");
			return;
		}*/
		
		if (them == myguild)
		{
			msg("<b>Nope! You can't declare a relation to yourself :)");
			return;
		}

		if (myguild.getRelationWish(them) == targetRelation)
		{
			msg("<b>You already have that relation wish set with %s.", them.getTag());
			return;
		}

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(targetRelation.getRelationCost(), "to change a relation wish", "for changing a relation wish")) return;

		// try to set the new relation
		Rel oldRelation = myguild.getRelationTo(them, true);
		myguild.setRelationWish(them, targetRelation);
		Rel currentRelation = myguild.getRelationTo(them, true);

		// if the relation change was successful
		if (targetRelation == currentRelation)
		{
			// trigger the guild relation event
			guildRelationEvent relationEvent = new guildRelationEvent(myguild, them, oldRelation, currentRelation);
			Bukkit.getServer().getPluginManager().callEvent(relationEvent);

			them.msg("%s<i> is now %s.", myguild.describeTo(them, true), targetRelation.getDescguildOne());
			myguild.msg("%s<i> is now %s.", them.describeTo(myguild, true), targetRelation.getDescguildOne());
		}
		// inform the other guild of your request
		else
		{
			them.msg("%s<i> wishes to be %s.", myguild.describeTo(them, true), targetRelation.getColor()+targetRelation.getDescguildOne());
			them.msg("<i>Type <c>/"+Conf.baseCommandAliases.get(0)+" "+targetRelation+" "+myguild.getTag()+"<i> to accept.");
			myguild.msg("%s<i> were informed that you wish to be %s<i>.", them.describeTo(myguild, true), targetRelation.getColor()+targetRelation.getDescguildOne());
		}
		
		// TODO: The ally case should work!!
		//   * this might have to be bumped up to make that happen, & allow ALLY,NEUTRAL only
		if ( targetRelation != Rel.TRUCE && them.getFlag(FFlag.PEACEFUL))
		{
			them.msg("<i>This will have no effect while your guild is peaceful.");
			myguild.msg("<i>This will have no effect while their guild is peaceful.");
		}
		
		if ( targetRelation != Rel.TRUCE && myguild.getFlag(FFlag.PEACEFUL))
		{
			them.msg("<i>This will have no effect while their guild is peaceful.");
			myguild.msg("<i>This will have no effect while your guild is peaceful.");
		}

		SpoutFeatures.updateTitle(myguild, them);
		SpoutFeatures.updateTitle(them, myguild);
		SpoutFeatures.updateTerritoryDisplayLoc(null);
	}
}
