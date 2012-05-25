package com.massivecraft.guilds;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.Location;
import org.bukkit.Material;

import com.massivecraft.guilds.adapters.FFlagTypeAdapter;
import com.massivecraft.guilds.adapters.FPermTypeAdapter;
import com.massivecraft.guilds.adapters.LocationTypeAdapter;
import com.massivecraft.guilds.adapters.RelTypeAdapter;
import com.massivecraft.guilds.cmd.*;
import com.massivecraft.guilds.integration.capi.CapiFeatures;
import com.massivecraft.guilds.integration.Econ;
import com.massivecraft.guilds.integration.EssentialsFeatures;
import com.massivecraft.guilds.integration.HerochatFeatures;
import com.massivecraft.guilds.integration.LWCFeatures;
import com.massivecraft.guilds.integration.SpoutFeatures;
import com.massivecraft.guilds.integration.Worldguard;
import com.massivecraft.guilds.listeners.guildsBlockListener;
import com.massivecraft.guilds.listeners.guildsChatListener;
import com.massivecraft.guilds.listeners.guildsEntityListener;
import com.massivecraft.guilds.listeners.guildsExploitListener;
import com.massivecraft.guilds.listeners.guildsAppearanceListener;
import com.massivecraft.guilds.listeners.guildsPlayerListener;
import com.massivecraft.guilds.listeners.guildsServerListener;
import com.massivecraft.guilds.struct.FFlag;
import com.massivecraft.guilds.struct.FPerm;
import com.massivecraft.guilds.struct.Rel;
import com.massivecraft.guilds.struct.TerritoryAccess;
import com.massivecraft.guilds.util.AutoLeaveTask;
import com.massivecraft.guilds.util.LazyLocation;
import com.massivecraft.guilds.zcore.MPlugin;
import com.massivecraft.guilds.zcore.util.TextUtil;

import com.google.gson.GsonBuilder;


public class P extends MPlugin
{
	// Our single plugin instance
	public static P p;
	
	// Listeners
	public final guildsPlayerListener playerListener;
	public final guildsChatListener chatListener;
	public final guildsEntityListener entityListener;
	public final guildsExploitListener exploitListener;
	public final guildsBlockListener blockListener;
	public final guildsServerListener serverListener;
	public final guildsAppearanceListener appearanceListener;
	
	// Persistance related
	private boolean locked = false;
	public boolean getLocked() {return this.locked;}
	public void setLocked(boolean val) {this.locked = val; this.setAutoSave(val);}
	private Integer AutoLeaveTask = null;
	
	// Commands
	public FCmdRoot cmdBase;
	public CmdAutoHelp cmdAutoHelp;
	
	public P()
	{
		p = this;
		this.playerListener = new guildsPlayerListener(this);
		this.chatListener = new guildsChatListener(this);
		this.entityListener = new guildsEntityListener(this);
		this.exploitListener = new guildsExploitListener();
		this.blockListener = new guildsBlockListener(this);
		this.serverListener = new guildsServerListener(this);
		this.appearanceListener = new guildsAppearanceListener(this);
	}


	@Override
	public void onEnable()
	{
		if ( ! preEnable()) return;
		this.loadSuccessful = false;

		// Load Conf from disk
		Conf.load();
		FPlayers.i.loadFromDisc();
		guilds.i.loadFromDisc();
		Board.load();
		
		// Add Base Commands
		this.cmdAutoHelp = new CmdAutoHelp();
		this.cmdBase = new FCmdRoot();
		this.getBaseCommands().add(cmdBase);

		EssentialsFeatures.setup();
		SpoutFeatures.setup();
		Econ.setup();
		CapiFeatures.setup();
		HerochatFeatures.setup();
		LWCFeatures.setup();
		
		if(Conf.worldGuardChecking)
		{
			Worldguard.init(this);
		}

		// start up task which runs the autoLeaveAfterDaysOfInactivity routine
		startAutoLeaveTask(false);

		// Register Event Handlers
		getServer().getPluginManager().registerEvents(this.playerListener, this);
		getServer().getPluginManager().registerEvents(this.chatListener, this);
		getServer().getPluginManager().registerEvents(this.entityListener, this);
		getServer().getPluginManager().registerEvents(this.exploitListener, this);
		getServer().getPluginManager().registerEvents(this.blockListener, this);
		getServer().getPluginManager().registerEvents(this.serverListener, this);
		getServer().getPluginManager().registerEvents(this.appearanceListener, this);

		// since some other plugins execute commands directly through this command interface, provide it
		this.getCommand(this.refCommand).setExecutor(this);

		postEnable();
		this.loadSuccessful = true;
	}
	
	@Override
	public GsonBuilder getGsonBuilder()
	{
		return new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
		.registerTypeAdapter(LazyLocation.class, new LocationTypeAdapter())
		.registerTypeAdapter(TerritoryAccess.class, new TerritoryAccess())
		.registerTypeAdapter(Rel.class, new RelTypeAdapter())
		.registerTypeAdapter(FPerm.class, new FPermTypeAdapter())
		.registerTypeAdapter(FFlag.class, new FFlagTypeAdapter());
	}

	@Override
	public void onDisable()
	{
		// only save data if plugin actually completely loaded successfully
		if (this.loadSuccessful)
		{
			Board.save();
			Conf.save();
		}
		EssentialsFeatures.unhookChat();
		if (AutoLeaveTask != null)
		{
			this.getServer().getScheduler().cancelTask(AutoLeaveTask);
			AutoLeaveTask = null;
		}
		super.onDisable();
	}

	public void startAutoLeaveTask(boolean restartIfRunning)
	{
		if (AutoLeaveTask != null)
		{
			if ( ! restartIfRunning) return;
			this.getServer().getScheduler().cancelTask(AutoLeaveTask);
		}

		if (Conf.autoLeaveRoutineRunsEveryXMinutes > 0.0)
		{
			long ticks = (long)(20 * 60 * Conf.autoLeaveRoutineRunsEveryXMinutes);
			AutoLeaveTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new AutoLeaveTask(), ticks, ticks);
		}
	}

	@Override
	public void postAutoSave()
	{
		Board.save();
		Conf.save();
	}

	@Override
	public boolean logPlayerCommands()
	{
		return Conf.logPlayerCommands;
	}

	@Override
	public boolean handleCommand(CommandSender sender, String commandString, boolean testOnly)
	{
		if (sender instanceof Player && guildsPlayerListener.preventCommand(commandString, (Player)sender)) return true;

		return super.handleCommand(sender, commandString, testOnly);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split)
	{
		// if bare command at this point, it has already been handled by MPlugin's command listeners
		if (split == null || split.length == 0) return true;

		// otherwise, needs to be handled; presumably another plugin directly ran the command
		String cmd = Conf.baseCommandAliases.isEmpty() ? "/f" : "/" + Conf.baseCommandAliases.get(0);
		return handleCommand(sender, cmd + " " + TextUtil.implode(Arrays.asList(split), " "), false);
	}



	// -------------------------------------------- //
	// Functions for other plugins to hook into
	// -------------------------------------------- //

	// This value will be updated whenever new hooks are added
	public int hookSupportVersion()
	{
		return 3;
	}

	// If another plugin is handling insertion of chat tags, this should be used to notify guilds
	public void handleguildTagExternally(boolean notByguilds)
	{
		Conf.chatTagHandledByAnotherPlugin = notByguilds;
	}

	// Simply put, should this chat event be left for guilds to handle? For now, that means players with guild Chat
	// enabled or use of the guilds f command without a slash; combination of isPlayerguildChatting() and isguildsCommand()
	
	
	public boolean shouldLetguildsHandleThisChat(PlayerChatEvent event)
	{
		if (event == null) return false;
		return (isPlayerguildChatting(event.getPlayer()) || isguildsCommand(event.getMessage()));
	}

	// Does player have guild Chat enabled? If so, chat plugins should preferably not do channels,
	// local chat, or anything else which targets individual recipients, so guild Chat can be done
	/**
	 * @deprecated  As of release 1.8, there is no built in guild chat.
	 */
	public boolean isPlayerguildChatting(Player player)
	{
		return false;
	}

	// Is this chat message actually a guilds command, and thus should be left alone by other plugins?
	public boolean isguildsCommand(String check)
	{
		if (check == null || check.isEmpty()) return false;
		return this.handleCommand(null, check, true);
	}

	// Get a player's guild tag (guild name), mainly for usage by chat plugins for local/channel chat
	public String getPlayerguildTag(Player player)
	{
		return getPlayerguildTagRelation(player, null);
	}

	// Same as above, but with relation (enemy/neutral/ally) coloring potentially added to the tag
	public String getPlayerguildTagRelation(Player speaker, Player listener)
	{
		String tag = "~";

		if (speaker == null)
			return tag;

		FPlayer me = FPlayers.i.get(speaker);
		if (me == null)
			return tag;

		// if listener isn't set, or config option is disabled, give back uncolored tag
		if (listener == null || !Conf.chatParseTagsColored) {
			tag = me.getChatTag().trim();
		} else {
			FPlayer you = FPlayers.i.get(listener);
			if (you == null)
				tag = me.getChatTag().trim();
			else  // everything checks out, give the colored tag
				tag = me.getChatTag(you).trim();
		}
		if (tag.isEmpty())
			tag = "~";

		return tag;
	}

	// Get a player's title within their guild, mainly for usage by chat plugins for local/channel chat
	public String getPlayerTitle(Player player)
	{
		if (player == null)
			return "";

		FPlayer me = FPlayers.i.get(player);
		if (me == null)
			return "";

		return me.getTitle().trim();
	}

	// Get a list of all guild tags (names)
	public Set<String> getguildTags()
	{
		Set<String> tags = new HashSet<String>();
		for (guild guild : guilds.i.get())
		{
			tags.add(guild.getTag());
		}
		return tags;
	}

	// Get a list of all players in the specified guild
	public Set<String> getPlayersInguild(String guildTag)
	{
		Set<String> players = new HashSet<String>();
		guild guild = guilds.i.getByTag(guildTag);
		if (guild != null)
		{
			for (FPlayer fplayer : guild.getFPlayers())
			{
				players.add(fplayer.getName());
			}
		}
		return players;
	}

	// Get a list of all online players in the specified guild
	public Set<String> getOnlinePlayersInguild(String guildTag)
	{
		Set<String> players = new HashSet<String>();
		guild guild = guilds.i.getByTag(guildTag);
		if (guild != null)
		{
			for (FPlayer fplayer : guild.getFPlayersWhereOnline(true))
			{
				players.add(fplayer.getName());
			}
		}
		return players;
	}

	// check if player is allowed to build/destroy in a particular location
	public boolean isPlayerAllowedToBuildHere(Player player, Location location)
	{
		return guildsBlockListener.playerCanBuildDestroyBlock(player, location.getBlock(), "", true);
	}

	// check if player is allowed to interact with the specified block (doors/chests/whatever)
	public boolean isPlayerAllowedToInteractWith(Player player, Block block)
	{
		return guildsPlayerListener.canPlayerUseBlock(player, block, true);
	}

	// check if player is allowed to use a specified item (flint&steel, buckets, etc) in a particular location
	public boolean isPlayerAllowedToUseThisHere(Player player, Location location, Material material)
	{
		return guildsPlayerListener.playerCanUseItemHere(player, location, material, true);
	}
}
