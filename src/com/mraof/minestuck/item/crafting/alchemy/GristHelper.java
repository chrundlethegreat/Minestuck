package com.mraof.minestuck.item.crafting.alchemy;

import com.mraof.minestuck.MinestuckConfig;
import com.mraof.minestuck.editmode.EditData;
import com.mraof.minestuck.editmode.ServerEditHandler;
import com.mraof.minestuck.network.skaianet.SburbConnection;
import com.mraof.minestuck.network.skaianet.SkaianetHandler;
import com.mraof.minestuck.util.Debug;
import com.mraof.minestuck.util.IdentifierHandler;
import com.mraof.minestuck.util.IdentifierHandler.PlayerIdentifier;
import com.mraof.minestuck.world.storage.PlayerData;
import com.mraof.minestuck.world.storage.PlayerSavedData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.*;
import java.util.Map.Entry;

public class GristHelper {
	private static Random random = new Random();
	private static final boolean SHOULD_OUTPUT_GRIST_CHANGES = MinestuckConfig.showGristChanges.get();
	
	public static HashMap<GristType, ArrayList<GristType>> secondaryGristMap;	//TODO Consider if these instead should be defined when grist types are registered

	static
	{
		secondaryGristMap = new HashMap<>();
		for(GristType type : GristTypes.values())
			secondaryGristMap.put(type, new ArrayList<>());
		secondaryGristMap.get(GristType.AMBER).add(GristType.RUST);
		secondaryGristMap.get(GristType.AMBER).add(GristType.SULFUR);
		secondaryGristMap.get(GristType.AMETHYST).add(GristType.QUARTZ);
		secondaryGristMap.get(GristType.AMETHYST).add(GristType.GARNET);
		secondaryGristMap.get(GristType.CAULK).add(GristType.IODINE);
		secondaryGristMap.get(GristType.CAULK).add(GristType.CHALK);
		secondaryGristMap.get(GristType.CHALK).add(GristType.SHALE);
		secondaryGristMap.get(GristType.CHALK).add(GristType.MARBLE);
		secondaryGristMap.get(GristType.COBALT).add(GristType.RUBY);
		secondaryGristMap.get(GristType.COBALT).add(GristType.AMETHYST);
		secondaryGristMap.get(GristType.GARNET).add(GristType.RUBY);
		secondaryGristMap.get(GristType.GARNET).add(GristType.GOLD);
		secondaryGristMap.get(GristType.IODINE).add(GristType.AMBER);
		secondaryGristMap.get(GristType.IODINE).add(GristType.CHALK);
		secondaryGristMap.get(GristType.MARBLE).add(GristType.CAULK);
		secondaryGristMap.get(GristType.MARBLE).add(GristType.AMETHYST);
		secondaryGristMap.get(GristType.MERCURY).add(GristType.COBALT);
		secondaryGristMap.get(GristType.MERCURY).add(GristType.RUST);
		secondaryGristMap.get(GristType.QUARTZ).add(GristType.MARBLE);
		secondaryGristMap.get(GristType.QUARTZ).add(GristType.URANIUM);
		secondaryGristMap.get(GristType.RUBY).add(GristType.QUARTZ);
		secondaryGristMap.get(GristType.RUBY).add(GristType.DIAMOND);
		secondaryGristMap.get(GristType.RUST).add(GristType.SHALE);
		secondaryGristMap.get(GristType.RUST).add(GristType.GARNET);
		secondaryGristMap.get(GristType.SHALE).add(GristType.MERCURY);
		secondaryGristMap.get(GristType.SHALE).add(GristType.TAR);
		secondaryGristMap.get(GristType.SULFUR).add(GristType.IODINE);
		secondaryGristMap.get(GristType.SULFUR).add(GristType.TAR);
		secondaryGristMap.get(GristType.TAR).add(GristType.AMBER);
		secondaryGristMap.get(GristType.TAR).add(GristType.COBALT);
		
		secondaryGristMap.get(GristType.URANIUM).add(GristType.DIAMOND);
		secondaryGristMap.get(GristType.DIAMOND).add(GristType.GOLD);
		secondaryGristMap.get(GristType.GOLD).add(GristType.URANIUM);
	}

	
	/**
	 * Returns a random grist type. Used for creating randomly aligned underlings.
	 */
	public static GristType getPrimaryGrist()
	{
		while (true)
		{
			GristType randGrist = GristType.MARBLE;//GristType.values().get(random.nextInt(GristType.values().size()));
			if (randGrist.getRarity() > random.nextFloat() && randGrist != GristType.ARTIFACT)
				return randGrist;
		}
	}
	
	/**
	 * Returns a secondary grist type based on primary grist
	 */
	public static GristType getSecondaryGrist(GristType primary)
	{
		if(secondaryGristMap.get(primary).size() != 0 && random.nextInt(secondaryGristMap.get(primary).size() * 2) != 0)
			return secondaryGristMap.get(primary).get(random.nextInt(secondaryGristMap.get(primary).size()));
		else return primary;
	}

	
	/**
	 * Returns a GristSet representing the drops from an underling, given the underling's type and a static loot multiplier.
	 */
	public static GristSet getRandomDrop(GristType primary, double multiplier)
	{
		if(primary == null)
		{
			Debug.warn("Got an underling grist drop call with a null grist type. (multiplier:"+multiplier+")");
			return null;
		}
		
		GristSet set = new GristSet();
		set.addGrist(GristType.BUILD, (int)(2*multiplier + random.nextDouble()*18*multiplier));
		set.addGrist(primary, (int)(1*multiplier + random.nextDouble()*9*multiplier));
		set.addGrist(getSecondaryGrist(primary), (int)(0.5*multiplier + random.nextDouble()*4*multiplier));
		return set;
		
	}
	
	/**
	 * A shortened statement to obtain a certain grist count.
	 */
	public static long getGrist(World world, PlayerIdentifier player, GristType type)
	{
		return PlayerSavedData.getData(player, world).getGristCache().getGrist(type);
	}
	
	public static boolean canAfford(ServerPlayerEntity player, GristSet cost)
	{
		return canAfford(PlayerSavedData.getData(player).getGristCache(), cost);
	}
	
	public static boolean canAfford(World world, PlayerIdentifier player, GristSet cost)
	{
		return canAfford(PlayerSavedData.getData(player, world).getGristCache(), cost);
	}
	
	public static boolean canAfford(GristSet base, GristSet cost) {
		if (base == null || cost == null) {return false;}
		Map<GristType, Long> reqs = cost.getMap();
		
		if (reqs != null) {
			for (Entry<GristType, Long> pairs : reqs.entrySet())
			{
				GristType type = pairs.getKey();
				long need = pairs.getValue();
				long have = base.getGrist(type);

				if (need > have) return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Uses the encoded version of the username!
	 */
	public static void decrease(World world, PlayerIdentifier player, GristSet set)
	{
		increase(world, player, set.copy().scale(-1));
	}
	
	/**
	 * This method will probably be used somewhere in the future.
	 */
	public static long getGristValue(GristSet set) {	//TODO potentially duplicate code here, in GristSet.getValue and in AlchemyRecipes.onAlchemizedItem
		long i = 0;
		for(GristType type : GristTypes.values()) {
			if(type.equals(GristType.BUILD))
				i += set.getGrist(type);
			else if(type.getRarity() == 0.0F)
				i += set.getGrist(type)*15;
			else i += set.getGrist(type)*type.getPower();
		}
		return i;
	}
	
	public static void increase(World world, PlayerIdentifier player, GristSet set)
	{
		Objects.requireNonNull(world);
		Objects.requireNonNull(player);
		Objects.requireNonNull(set);
		PlayerData data = PlayerSavedData.getData(player, world);
		NonNegativeGristSet newCache = new NonNegativeGristSet(data.getGristCache());
		newCache.addGrist(set);
		data.setGristCache(newCache);
	}
	
	private static void notify(MinecraftServer server, PlayerIdentifier player, ITextComponent type, long difference, String action)
	{
		if(SHOULD_OUTPUT_GRIST_CHANGES)
		{
			if (player != null)
			{
				ServerPlayerEntity client = player.getPlayer(server);
				if(client != null)
				{
					//"true" sends the message to the action bar (like bed messages), while "false" sends it to the chat.
					client.sendStatusMessage(new TranslationTextComponent("You " + action + " " + difference + " " + type + " grist."), true);//TODO Translation
				}
			}
		}
	}
	
	private static void notifyEditPlayer(MinecraftServer server, PlayerIdentifier player, ITextComponent type, long difference, String action)
	{
		SburbConnection sc = SkaianetHandler.get(server).getActiveConnection(player);
		if(sc == null)
			return;
		
		EditData ed = ServerEditHandler.getData(server, sc);
		if(ed == null)
			return;
		
		notify(server, IdentifierHandler.encode(ed.getEditor()), type, difference, action);
	}
}