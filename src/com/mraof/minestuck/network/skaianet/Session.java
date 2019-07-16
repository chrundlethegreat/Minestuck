package com.mraof.minestuck.network.skaianet;

import com.mraof.minestuck.MinestuckConfig;
import com.mraof.minestuck.util.IdentifierHandler;
import com.mraof.minestuck.util.IdentifierHandler.PlayerIdentifier;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.*;

/**
 * Was also an interface for the session system, but now just a data structure representing a session.
 * SessionHandler is the new class for session interface.
 * @author kirderf1
 */
public class Session
{
	
	Map<PlayerIdentifier, PredefineData> predefinedPlayers;
	List<SburbConnection> connections;
	String name;
	
	/**
	 * If the "connection circle" is whole, unused if globalSession == true.
	 */
	boolean completed;
	boolean locked;
	
	//Unused, will later be 0 if not yet generated
	int skaiaId;
	int prospitId;
	int derseId;
	
	/**
	 * Checks if the variable completed should be true or false.
	 */
	void checkIfCompleted(boolean singleSession)
	{
		if(connections.isEmpty() || singleSession)
		{
			completed = false;
			return;
		}
		PlayerIdentifier start = connections.get(0).getClientIdentifier();
		PlayerIdentifier current = start;
		main: while(true){
			for(SburbConnection c : connections)
			{
				if(!c.hasEntered())
				{
					completed = false;
					return;
				}
				if(c.getServerIdentifier().equals(current))
				{
					current = c.getClientIdentifier();
					if(start.equals(current)) {
						completed = true;
						return;
					} else continue main;
				}
			}
			completed = false;
			return;
		}
	}
	
	Session()
	{
		connections = new ArrayList<>();
		predefinedPlayers = new HashMap<>();
	}
	
	/**
	 * Checks if a certain player is in the connection list.
	 * @param player The username of the player.
	 * @return If the player was found.
	 */
	public boolean containsPlayer(PlayerIdentifier player)
	{
		if(player.equals(IdentifierHandler.nullIdentifier))
			return false;
		if(predefinedPlayers.containsKey(player))
			return true;
		for(SburbConnection c : connections)
			if(c.getClientIdentifier().equals(player) || c.getServerIdentifier().equals(player))
				return true;
		return false;
	}
	
	/**
	 * Creates a list with all players in the session.
	 * @return Returns a list with the players identifiers.
	 */
	public Set<PlayerIdentifier> getPlayerList()
	{
		Set<PlayerIdentifier> list = new HashSet<>();
		for(SburbConnection c : this.connections)
		{
			list.add(c.getClientIdentifier());
			if(!c.getServerIdentifier().equals(IdentifierHandler.nullIdentifier))
				list.add(c.getServerIdentifier());
		}
		list.addAll(predefinedPlayers.keySet());
		return list;
	}
	
	/**
	 * Writes this session to an nbt tag.
	 * Note that this will only work as long as <code>SkaianetHandler.connections</code> remains unmodified.
	 * @return An CompoundNBT representing this session.
	 */
	CompoundNBT write()
	{
		CompoundNBT nbt = new CompoundNBT();
		
		if(isCustom())
			nbt.putString("name", name);
		ListNBT list = new ListNBT();
		for(SburbConnection c : connections)
			list.add(c.write());
		nbt.put("connections", list);
		ListNBT predefineList = new ListNBT();
		for(Map.Entry<PlayerIdentifier, PredefineData> entry : predefinedPlayers.entrySet())
			predefineList.add(entry.getKey().saveToNBT(entry.getValue().write(), "player"));
		nbt.put("predefinedPlayers", predefineList);
		nbt.putBoolean("locked", locked);
		//nbt.putInt("skaiaId", skaiaId);
		//nbt.putInt("derseId", derseId);
		//nbt.putInt("prospitId", prospitId);
		return nbt;
	}
	
	/**
	 * Reads data from the given nbt tag.
	 * @param nbt An CompoundNBT to read from.
	 * @return This.
	 */
	Session read(CompoundNBT nbt)
	{
		if(nbt.contains("name", 8))
			name = nbt.getString("name");
		else name = null;
		
		ListNBT list = nbt.getList("connections", 10);
		for(int i = 0; i < list.size(); i++)
			connections.add(new SburbConnection().read(list.getCompound(i)));
		
		if(nbt.contains("predefinedPlayers", 9))	//If it is a tag list
		{
			list = nbt.getList("predefinedPlayers", 10);
			for(int i = 0; i < list.size(); i++)
			{
				CompoundNBT compound = list.getCompound(i);
				predefinedPlayers.put(IdentifierHandler.load(compound, "player"), new PredefineData().read(compound));
			}
		} else
		{	//Support for saves from older minestuck versions
			CompoundNBT predefineTag = nbt.getCompound("predefinedPlayers");
			for(String player : predefineTag.keySet())
			{
				CompoundNBT compound = new CompoundNBT();
				compound.putString("player", player);
				predefinedPlayers.put(IdentifierHandler.load(compound, "player"), new PredefineData().read(predefineTag.getCompound(player)));
			}
		}
		
		locked = nbt.getBoolean("locked");
		
		checkIfCompleted(MinestuckConfig.globalSession);
		return this;
	}
	
	public boolean isCustom()
	{
		return name != null;
	}
}