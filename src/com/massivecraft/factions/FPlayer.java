package com.massivecraft.guilds;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.massivecraft.guilds.event.FPlayerLeaveEvent;
import com.massivecraft.guilds.event.LandClaimEvent;
import com.massivecraft.guilds.iface.EconomyParticipator;
import com.massivecraft.guilds.iface.RelationParticipator;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.integration.LWCFeatures;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.integration.Worldguard;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.util.RelationUtil;
import com.massivecraft.guilds.zcore.persist.PlayerEntity;


/**
 * Logged in players always have exactly one FPlayer instance.
 * Logged out players may or may not have an FPlayer instance. They will always have one if they are part of a guild.
 * This is because only players with a guild are saved to disk (in order to not waste disk space).
 * 
 * The FPlayer is linked to a minecraft player using the player name.
 * 
 * The same instance is always returned for the same player.
 * This means you can use the == operator. No .equals method necessary.
 */

// TODO: The players are saved in non order.
public class FPlayer extends PlayerEntity implements EconomyParticipator
{	
	// FIELD: lastStoodAt
	private transient FLocation lastStoodAt = new FLocation(); // Where did this player stand the last time we checked?
	public FLocation getLastStoodAt() { return this.lastStoodAt; }
	public void setLastStoodAt(FLocation flocation) { this.lastStoodAt = flocation; }
	
	// FIELD: guildId
	private String guildId;
	public guild getguild() { if(this.guildId == null) {return null;} return guilds.i.get(this.guildId); }
	public String getguildId() { return this.guildId; }
	public boolean hasguild() { return ! guildId.equals("0"); }
	public void setguild(guild guild)
	{
		guild oldguild = this.getguild();
		if (oldguild != null) oldguild.removeFPlayer(this);
		guild.addFPlayer(this);
		this.guildId = guild.getId();
		SpoutFeatures.updateTitle(this, null);
		SpoutFeatures.updateTitle(null, this);
	}
	
	// FIELD: role
	private Rel role;
	public Rel getRole() { return this.role; }
	public void setRole(Rel role) { this.role = role; SpoutFeatures.updateTitle(this, null); }
	
	// FIELD: title
	private String title;
	public String getTitle() { return this.title; }
	public void setTitle(String title) { this.title = title; }
	
	// FIELD: power
	private double power;

	// FIELD: powerBoost
	// special increase/decrease to min and max power for this player
	private double powerBoost;
	public double getPowerBoost() { return this.powerBoost; }
	public void setPowerBoost(double powerBoost) { this.powerBoost = powerBoost; }

	// FIELD: lastPowerUpdateTime
	private long lastPowerUpdateTime;
	
	// FIELD: lastLoginTime
	private long lastLoginTime;
	
	// FIELD: mapAutoUpdating
	private transient boolean mapAutoUpdating;
	public void setMapAutoUpdating(boolean mapAutoUpdating) { this.mapAutoUpdating = mapAutoUpdating; }
	public boolean isMapAutoUpdating() { return mapAutoUpdating; }
	
	// FIELD: autoClaimEnabled
	private transient guild autoClaimFor;
	public guild getAutoClaimFor() { return autoClaimFor; }
	public void setAutoClaimFor(guild guild) { this.autoClaimFor = guild; }
		
	private transient boolean hasAdminMode = false;
	public boolean hasAdminMode() { return this.hasAdminMode; }
	public void setHasAdminMode(boolean val) { this.hasAdminMode = val; }
	
	// FIELD: loginPvpDisabled
	private transient boolean loginPvpDisabled;
	
	// FIELD: account
	public String getAccountId() { return this.getId(); }
	
	// -------------------------------------------- //
	// Construct
	// -------------------------------------------- //
	
	// GSON need this noarg constructor.
	public FPlayer()
	{
		this.resetguildData(false);
		this.power = Conf.powerPlayerStarting;
		this.lastPowerUpdateTime = System.currentTimeMillis();
		this.lastLoginTime = System.currentTimeMillis();
		this.mapAutoUpdating = false;
		this.autoClaimFor = null;
		this.loginPvpDisabled = (Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0) ? true : false;
		this.powerBoost = 0.0;

		if ( ! Conf.newPlayerStartingguildID.equals("0") && guilds.i.exists(Conf.newPlayerStartingguildID))
		{
			this.guildId = Conf.newPlayerStartingguildID;
		}
	}
	
	public final void resetguildData(boolean doSpoutUpdate)
	{
		if (this.guildId != null && guilds.i.exists(this.guildId)) // Avoid infinite loop! TODO: I think that this is needed is a sign we need to refactor.
		{
			guild currentguild = this.getguild();
			if (currentguild != null)
			{
				currentguild.removeFPlayer(this);
			}
		}

		this.guildId = "0"; // The default neutral guild
		this.role = Rel.MEMBER;
		this.title = "";
		this.autoClaimFor = null;

		if (doSpoutUpdate)
		{
			SpoutFeatures.updateTitle(this, null);
			SpoutFeatures.updateTitle(null, this);
			SpoutFeatures.updateCape(this.getPlayer(), null);
		}
	}
	
	public void resetguildData()
	{
		this.resetguildData(true);
	}
	
	// -------------------------------------------- //
	// Getters And Setters
	// -------------------------------------------- //
	
	public long getLastLoginTime()
	{
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime)
	{
		losePowerFromBeingOffline();
		this.lastLoginTime = lastLoginTime;
		this.lastPowerUpdateTime = lastLoginTime;
		if (Conf.noPVPDamageToOthersForXSecondsAfterLogin > 0)
		{
			this.loginPvpDisabled = true;
		}
	}

	public boolean hasLoginPvpDisabled()
	{
		if (!loginPvpDisabled)
		{
			return false;
		}
		if (this.lastLoginTime + (Conf.noPVPDamageToOthersForXSecondsAfterLogin * 1000) < System.currentTimeMillis())
		{
			this.loginPvpDisabled = false;
			return false;
		}
		return true;
	}
	
	//----------------------------------------------//
	// Title, Name, guild Tag and Chat
	//----------------------------------------------//
	public String getName()
	{
		return getId();
	}
	
	public String getTag()
	{
		if ( ! this.hasguild())
		{
			return "";
		}
		return this.getguild().getTag();
	}
	
	// Base concatenations:
	
	public String getNameAndSomething(String something)
	{
		String ret = this.role.getPrefix();
		if (something.length() > 0) {
			ret += something+" ";
		}
		ret += this.getName();
		return ret;
	}
	
	public String getNameAndTitle()
	{
		return this.getNameAndSomething(this.getTitle());
	}
	
	public String getNameAndTag()
	{
		return this.getNameAndSomething(this.getTag());
	}
	
	// Colored concatenations:
	// These are used in information messages
	
	public String getNameAndTitle(guild guild)
	{
		return this.getColorTo(guild)+this.getNameAndTitle();
	}
	public String getNameAndTitle(FPlayer fplayer)
	{
		return this.getColorTo(fplayer)+this.getNameAndTitle();
	}
	
	// Chat Tag: 
	// These are injected into the format of global chat messages.
	
	public String getChatTag()
	{
		if ( ! this.hasguild()) {
			return "";
		}
		
		return String.format(Conf.chatTagFormat, this.role.getPrefix()+this.getTag());
	}
	
	// Colored Chat Tag
	public String getChatTag(guild guild)
	{
		if ( ! this.hasguild()) {
			return "";
		}
		
		return this.getRelationTo(guild).getColor()+getChatTag();
	}
	
	public String getChatTag(FPlayer fplayer)
	{
		if ( ! this.hasguild())
		{
			return "";
		}
		
		return this.getColorTo(fplayer)+getChatTag();
	}
	
	// -------------------------------
	// Relation and relation colors
	// -------------------------------
	
	@Override
	public String describeTo(RelationParticipator observer, boolean ucfirst)
	{
		return RelationUtil.describeThatToMe(this, observer, ucfirst);
	}
	
	@Override
	public String describeTo(RelationParticipator observer)
	{
		return RelationUtil.describeThatToMe(this, observer);
	}
	
	@Override
	public Rel getRelationTo(RelationParticipator observer)
	{
		return RelationUtil.getRelationOfThatToMe(this, observer);
	}
	
	@Override
	public Rel getRelationTo(RelationParticipator observer, boolean ignorePeaceful)
	{
		return RelationUtil.getRelationOfThatToMe(this, observer, ignorePeaceful);
	}
	
	public Rel getRelationToLocation()
	{
		return Board.getguildAt(new FLocation(this)).getRelationTo(this);
	}
	
	@Override
	public ChatColor getColorTo(RelationParticipator observer)
	{
		return RelationUtil.getColorOfThatToMe(this, observer);
	}
	
	//----------------------------------------------//
	// Health
	//----------------------------------------------//
	public void heal(int amnt)
	{
		Player player = this.getPlayer();
		if (player == null)
		{
			return;
		}
		player.setHealth(player.getHealth() + amnt);
	}
	
	
	//----------------------------------------------//
	// Power
	//----------------------------------------------//
	public double getPower()
	{
		this.updatePower();
		return this.power;
	}
	
	protected void alterPower(double delta)
	{
		this.power += delta;
		if (this.power > this.getPowerMax())
			this.power = this.getPowerMax();
		else if (this.power < this.getPowerMin())
			this.power = this.getPowerMin();
	}
	
	public double getPowerMax()
	{
		return Conf.powerPlayerMax + this.powerBoost;
	}
	
	public double getPowerMin()
	{
		return Conf.powerPlayerMin + this.powerBoost;
	}
	
	public int getPowerRounded()
	{
		return (int) Math.round(this.getPower());
	}
	
	public int getPowerMaxRounded()
	{
		return (int) Math.round(this.getPowerMax());
	}
	
	public int getPowerMinRounded()
	{
		return (int) Math.round(this.getPowerMin());
	}
	
	protected void updatePower()
	{
		if (this.isOffline())
		{
			losePowerFromBeingOffline();
			if (!Conf.powerRegenOffline)
			{
				return;
			}
		}
		long now = System.currentTimeMillis();
		long millisPassed = now - this.lastPowerUpdateTime;
		this.lastPowerUpdateTime = now;

		Player thisPlayer = this.getPlayer();
		if (thisPlayer != null && thisPlayer.isDead()) return;  // don't let dead players regain power until they respawn

		int millisPerMinute = 60*1000;		
		double powerPerMinute = Conf.powerPerMinute;
		if(Conf.scaleNegativePower && this.power < 0)
		{
			powerPerMinute += (Math.sqrt(Math.abs(this.power)) * Math.abs(this.power)) / Conf.scaleNegativeDivisor;
		}
		this.alterPower(millisPassed * powerPerMinute / millisPerMinute);
		
	}

	protected void losePowerFromBeingOffline()
	{
		if (Conf.powerOfflineLossPerDay > 0.0 && this.power > Conf.powerOfflineLossLimit)
		{
			long now = System.currentTimeMillis();
			long millisPassed = now - this.lastPowerUpdateTime;
			this.lastPowerUpdateTime = now;

			double loss = millisPassed * Conf.powerOfflineLossPerDay / (24*60*60*1000);
			if (this.power - loss < Conf.powerOfflineLossLimit)
			{
				loss = this.power;
			}
			this.alterPower(-loss);
		}
	}
	
	public void onDeath()
	{
		this.updatePower();
		this.alterPower(-Conf.powerPerDeath);
	}
	
	//----------------------------------------------//
	// Territory
	//----------------------------------------------//
	public boolean isInOwnTerritory()
	{
		return Board.getguildAt(new FLocation(this)) == this.getguild();
	}
	
	/*public boolean isInOthersTerritory()
	{
		guild guildHere = Board.getguildAt(new FLocation(this));
		return guildHere != null && guildHere.isNormal() && guildHere != this.getguild();
	}*/

	/*public boolean isInAllyTerritory()
	{
		return Board.getguildAt(new FLocation(this)).getRelationTo(this) == Rel.ALLY;
	}*/

	/*public boolean isInNeutralTerritory()
	{
		return Board.getguildAt(new FLocation(this)).getRelationTo(this) == Rel.NEUTRAL;
	}*/

	public boolean isInEnemyTerritory()
	{
		return Board.getguildAt(new FLocation(this)).getRelationTo(this) == Rel.ENEMY;
	}

	public void sendguildHereMessage()
	{
		if (SpoutFeatures.updateTerritoryDisplay(this))
		{
			return;
		}
		guild guildHere = Board.getguildAt(new FLocation(this));
		String msg = P.p.txt.parse("<i>")+" ~ "+guildHere.getTag(this);
		if (guildHere.getDescription().length() > 0)
		{
			msg += " - "+guildHere.getDescription();
		}
		this.sendMessage(msg);
	}
	
	// -------------------------------
	// Actions
	// -------------------------------
	
	public void leave(boolean makePay)
	{
		guild myguild = this.getguild();
		makePay = makePay && Econ.shouldBeUsed() && ! this.hasAdminMode();

		if (myguild == null)
		{
			resetguildData();
			return;
		}

		boolean perm = myguild.getFlag(FFlag.PERMANENT);
		
		if (!perm && this.getRole() == Rel.LEADER && myguild.getFPlayers().size() > 1)
		{
			msg("<b>You must give the admin role to someone else first.");
			return;
		}

		if (!Conf.canLeaveWithNegativePower && this.getPower() < 0)
		{
			msg("<b>You cannot leave until your power is positive.");
			return;
		}

		// if economy is enabled and they're not on the bypass list, make sure they can pay
		if (makePay && ! Econ.hasAtLeast(this, Conf.econCostLeave, "to leave your guild.")) return;

		FPlayerLeaveEvent leaveEvent = new FPlayerLeaveEvent(this,myguild,FPlayerLeaveEvent.PlayerLeaveReason.LEAVE);
		Bukkit.getServer().getPluginManager().callEvent(leaveEvent);
		if (leaveEvent.isCancelled()) return;

		// then make 'em pay (if applicable)
		if (makePay && ! Econ.modifyMoney(this, -Conf.econCostLeave, "to leave your guild.", "for leaving your guild.")) return;

		// Am I the last one in the guild?
		if (myguild.getFPlayers().size() == 1)
		{
			// Transfer all money
			if (Econ.shouldBeUsed())
				Econ.transferMoney(this, myguild, this, Econ.getBalance(myguild.getAccountId()));
		}
		
		if (myguild.isNormal())
		{
			for (FPlayer fplayer : myguild.getFPlayersWhereOnline(true))
			{
				fplayer.msg("%s<i> left %s<i>.", this.describeTo(fplayer, true), myguild.describeTo(fplayer));
			}

			if (Conf.logguildLeave)
				P.p.log(this.getName()+" left the guild: "+myguild.getTag());
		}
		
		this.resetguildData();

		if (myguild.isNormal() && !perm && myguild.getFPlayers().isEmpty())
		{
			// Remove this guild
			for (FPlayer fplayer : FPlayers.i.getOnline())
			{
				fplayer.msg("<i>%s<i> was disbanded.", myguild.describeTo(fplayer, true));
			}

			myguild.detach();
			if (Conf.logguildDisband)
				P.p.log("The guild "+myguild.getTag()+" ("+myguild.getId()+") was disbanded due to the last player ("+this.getName()+") leaving.");
		}
	}

	public boolean canClaimForguildAtLocation(guild forguild, Location location, boolean notifyFailure)
	{
		String error = null;
		FLocation flocation = new FLocation(location);
		guild myguild = getguild();
		guild currentguild = Board.getguildAt(flocation);
		int ownedLand = forguild.getLandRounded();
		
		if (Conf.worldGuardChecking && Worldguard.checkForRegionsInChunk(location))
		{
			// Checks for WorldGuard regions in the chunk attempting to be claimed
			error = P.p.txt.parse("<b>This land is protected");
		}
		else if (Conf.worldsNoClaiming.contains(flocation.getWorldName()))
		{
			error = P.p.txt.parse("<b>Sorry, this world has land claiming disabled.");
		}
		else if (this.hasAdminMode())
		{
			return true;
		}
		else if (forguild == currentguild)
		{
			error = P.p.txt.parse("%s<i> already own this land.", forguild.describeTo(this, true));
		}
		else if ( ! FPerm.TERRITORY.has(this, forguild, true))
		{
			return false;
		}
		else if (forguild.getFPlayers().size() < Conf.claimsRequireMinguildMembers)
		{
			error = P.p.txt.parse("guilds must have at least <h>%s<b> members to claim land.", Conf.claimsRequireMinguildMembers);
		}
		else if (ownedLand >= forguild.getPowerRounded())
		{
			error = P.p.txt.parse("<b>You can't claim more land! You need more power!");
		}
		else if (Conf.claimedLandsMax != 0 && ownedLand >= Conf.claimedLandsMax && ! forguild.getFlag(FFlag.INFPOWER))
		{
			error = P.p.txt.parse("<b>Limit reached. You can't claim more land!");
		}
		else if (currentguild.getRelationTo(forguild).isAtLeast(Rel.TRUCE) && ! currentguild.isNone())
		{
			error = P.p.txt.parse("<b>You can't claim this land due to your relation with the current owner.");
		}
		else if
		(
			Conf.claimsMustBeConnected
			&& ! this.hasAdminMode()
			&& myguild.getLandRoundedInWorld(flocation.getWorldName()) > 0
			&& !Board.isConnectedLocation(flocation, myguild)
			&& (!Conf.claimsCanBeUnconnectedIfOwnedByOtherguild || !currentguild.isNormal())
		)
		{
			if (Conf.claimsCanBeUnconnectedIfOwnedByOtherguild)
				error = P.p.txt.parse("<b>You can only claim additional land which is connected to your first claim or controlled by another guild!");
			else
				error = P.p.txt.parse("<b>You can only claim additional land which is connected to your first claim!");
		}
		else if (currentguild.isNormal())
		{
			if ( ! currentguild.hasLandInflation())
			{
				 // TODO more messages WARN current guild most importantly
				error = P.p.txt.parse("%s<i> owns this land and is strong enough to keep it.", currentguild.getTag(this));
			}
			else if ( ! Board.isBorderLocation(flocation))
			{
				error = P.p.txt.parse("<b>You must start claiming land at the border of the territory.");
			}
		}
		
		if (notifyFailure && error != null)
		{
			msg(error);
		}
		return error == null;
	}
	
	public boolean attemptClaim(guild forguild, Location location, boolean notifyFailure)
	{
		// notifyFailure is false if called by auto-claim; no need to notify on every failure for it
		// return value is false on failure, true on success
		
		FLocation flocation = new FLocation(location);
		guild currentguild = Board.getguildAt(flocation);
		
		int ownedLand = forguild.getLandRounded();
		
		if ( ! this.canClaimForguildAtLocation(forguild, location, notifyFailure)) return false;
		
		// TODO: Add flag no costs??
		// if economy is enabled and they're not on the bypass list, make sure they can pay
		boolean mustPay = Econ.shouldBeUsed() && ! this.hasAdminMode();
		double cost = 0.0;
		EconomyParticipator payee = null;
		if (mustPay)
		{
			cost = Econ.calculateClaimCost(ownedLand, currentguild.isNormal());

			if (Conf.econClaimUnconnectedFee != 0.0 && forguild.getLandRoundedInWorld(flocation.getWorldName()) > 0 && !Board.isConnectedLocation(flocation, forguild))
				cost += Conf.econClaimUnconnectedFee;

			if(Conf.bankEnabled && Conf.bankguildPaysLandCosts && this.hasguild())
				payee = this.getguild();
			else
				payee = this;

			if ( ! Econ.hasAtLeast(payee, cost, "to claim this land")) return false;
		}

		LandClaimEvent claimEvent = new LandClaimEvent(flocation, forguild, this);
		Bukkit.getServer().getPluginManager().callEvent(claimEvent);
		if(claimEvent.isCancelled()) return false;

		// then make 'em pay (if applicable)
		if (mustPay && ! Econ.modifyMoney(payee, -cost, "to claim this land", "for claiming this land")) return false;

		if (LWCFeatures.getEnabled() && forguild.isNormal() && Conf.onCaptureResetLwcLocks)
			LWCFeatures.clearOtherChests(flocation, this.getguild());

		// announce success
		Set<FPlayer> informTheseFPlayers = new HashSet<FPlayer>();
		informTheseFPlayers.add(this);
		informTheseFPlayers.addAll(forguild.getFPlayersWhereOnline(true));
		for (FPlayer fp : informTheseFPlayers)
		{
			fp.msg("<h>%s<i> claimed land for <h>%s<i> from <h>%s<i>.", this.describeTo(fp, true), forguild.describeTo(fp), currentguild.describeTo(fp));
		}
		
		Board.setguildAt(forguild, flocation);
		SpoutFeatures.updateTerritoryDisplayLoc(flocation);

		if (Conf.logLandClaims)
			P.p.log(this.getName()+" claimed land at ("+flocation.getCoordString()+") for the guild: "+forguild.getTag());

		return true;
	}
	
	// -------------------------------------------- //
	// Persistance
	// -------------------------------------------- //
	
	@Override
	public boolean shouldBeSaved()
	{
		if (this.hasguild()) return true;
		if (this.getPowerRounded() != this.getPowerMaxRounded() && this.getPowerRounded() != (int) Math.round(Conf.powerPlayerStarting)) return true;
		return false;
	}
	
	public void msg(String str, Object... args)
	{
		this.sendMessage(P.p.txt.parse(str, args));
	}
}