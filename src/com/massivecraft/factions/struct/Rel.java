package com.massivecraft.guilds.struct;

import org.bukkit.ChatColor;

import com.massivecraft.guilds.Conf;

public enum Rel
{
	LEADER   (70, "your guild leader", "your guild leader", "", ""),
	OFFICER  (60, "an officer in your guild", "officers in your guild", "", ""),
	MEMBER   (50, "a member in your guild", "members in your guild", "your guild", "your guilds"),
	ALLY     (40, "an ally", "allies", "an allied guild", "allied guilds"),
	TRUCE    (30, "someone in truce with you", "those in truce with you", "a guild in truce", "guilds in truce"),
	NEUTRAL  (20, "someone neutral to you", "those neutral to you", "a neutral guild", "neutral guilds"),
	ENEMY    (10, "an enemy", "enemies", "an enemy guild", "enemy guilds"),
	;
	
	private final int value;
	private final String descPlayerOne;
	public String getDescPlayerOne() { return this.descPlayerOne; }
	
	private final String descPlayerMany;
	public String getDescPlayerMany() { return this.descPlayerMany; }
	
	private final String descguildOne;
	public String getDescguildOne() { return this.descguildOne; }
	
	private final String descguildMany;
	public String getDescguildMany() { return this.descguildMany; }
	
	private Rel(final int value, final String descPlayerOne, final String descPlayerMany, final String descguildOne, final String descguildMany)
	{
		this.value = value;
		this.descPlayerOne = descPlayerOne;
		this.descPlayerMany = descPlayerMany;
		this.descguildOne = descguildOne;
		this.descguildMany = descguildMany;
	}
	
	public static Rel parse(String str)
	{
		if (str == null || str.length() < 1) return null;
		
		str = str.toLowerCase();
		
		// These are to allow conversion from the old system.
		if (str.equals("admin"))
		{
			return LEADER;
		}
		
		if (str.equals("moderator"))
		{
			return OFFICER;
		}
		
		if (str.equals("normal"))
		{
			return MEMBER;
		}
		
		// This is how we check: Based on first char.
		char c = str.charAt(0);
		if (c == 'l') return LEADER;
		if (c == 'o') return OFFICER;
		if (c == 'm') return MEMBER;
		if (c == 'a') return ALLY;
		if (c == 't') return TRUCE;
		if (c == 'n') return NEUTRAL;
		if (c == 'e') return ENEMY;
		return null;
	}
	
	public boolean isAtLeast(Rel rel)
	{
		return this.value >= rel.value;
	}
	
	public boolean isAtMost(Rel rel)
	{
		return this.value <= rel.value;
	}
	
	public boolean isLessThan(Rel rel)
	{
		return this.value < rel.value;
	}
	
	public boolean isMoreThan(Rel rel)
	{
		return this.value > rel.value;
	}
	
	public ChatColor getColor()
	{
		if (this.isAtLeast(MEMBER))
			return Conf.colorMember;
		else if (this == ALLY)
			return Conf.colorAlly;
		else if (this == NEUTRAL)
			return Conf.colorNeutral;
		else if (this == TRUCE)
			return Conf.colorTruce;
		else
			return Conf.colorEnemy;
	}
	
	public String getPrefix()
	{
		if (this == LEADER)
		{
			return Conf.prefixLeader;
		} 
		
		if (this == OFFICER)
		{
			return Conf.prefixOfficer;
		}
		
		return "";
	}
	
	// TODO: ADD TRUCE!!!!
	// TODO.... or remove it...
	public double getRelationCost()
	{
		if (this == ENEMY)
			return Conf.econCostEnemy;
		else if (this == ALLY)
			return Conf.econCostAlly;
		else if (this == TRUCE)
			return Conf.econCostTruce;
		else
			return Conf.econCostNeutral;
	}
}
