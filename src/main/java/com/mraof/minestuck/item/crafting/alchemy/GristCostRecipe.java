package com.mraof.minestuck.item.crafting.alchemy;

import com.google.gson.JsonObject;
import com.mraof.minestuck.item.crafting.MSRecipeTypes;
import com.mraof.minestuck.jei.JeiGristCost;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;

public abstract class GristCostRecipe implements IRecipe<IInventory>
{
	
	public static GristSet findCostForItem(ItemStack input, GristType type, boolean shouldRoundDown, World world)
	{
		return findRecipeForItem(input, world).map(recipe -> recipe.getGristCost(input, type, shouldRoundDown)).orElse(null);
	}
	
	public static Optional<GristCostRecipe> findRecipeForItem(ItemStack input, World world)
	{
		return findRecipeForItem(input, world, world.getRecipeManager());
	}
	
	public static Optional<GristCostRecipe> findRecipeForItem(ItemStack input, World world, RecipeManager recipeManager)
	{
		return recipeManager.getRecipes(MSRecipeTypes.GRIST_COST_TYPE, new Inventory(input), world).stream().max(Comparator.comparingInt(GristCostRecipe::getPriority));
	}
	
	public final ResourceLocation id;
	public final Ingredient ingredient;
	@Nullable
	public final Integer priority;
	
	public GristCostRecipe(ResourceLocation id, Ingredient ingredient, Integer priority)
	{
		this.id = id;
		this.ingredient = ingredient;
		this.priority = priority;
	}
	
	@Override
	public boolean matches(IInventory inv, World worldIn)
	{
		return ingredient.test(inv.getStackInSlot(0));
	}
	
	@Override
	public ItemStack getCraftingResult(IInventory inv)
	{
		return inv.getStackInSlot(0);
	}
	
	@Override
	public boolean canFit(int width, int height)
	{
		return true;
	}
	
	@Override
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}
	
	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.from(this.ingredient);
	}
	
	@Override
	public ResourceLocation getId()
	{
		return id;
	}
	
	@Override
	public IRecipeType<?> getType()
	{
		return MSRecipeTypes.GRIST_COST_TYPE;
	}
	
	public int getPriority()
	{
		if(priority == null)
			return priorityFromIngredient(ingredient);
		else return priority;
	}
	
	public abstract GristSet getGristCost(ItemStack input, GristType wildcardType, boolean shouldRoundDown);
	
	public boolean canPickWildcard()
	{
		return false;
	}
	
	public JeiGristCost getJeiCost()
	{
		return null;
	}
	
	private static int priorityFromIngredient(Ingredient ingredient)
	{
		return 100 - (ingredient.getMatchingStacks().length - 1)*10;
	}
	
	public static GristSet scaleToCountAndDurability(GristSet cost, ItemStack stack, boolean shouldRoundDown)
	{
		if (stack.getCount() != 1)
			cost.scale(stack.getCount());
		
		if (stack.isDamaged())
		{
			float multiplier = 1 - stack.getItem().getDamage(stack) / ((float) stack.getMaxDamage());
			cost.scale(multiplier, shouldRoundDown);
		}
		
		return cost;
	}
	
	//Helper class for implementing serializer classes
	public abstract static class AbstractSerializer<T extends GristCostRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T>
	{
		@Override
		public T read(ResourceLocation recipeId, JsonObject json)
		{
			Ingredient ingredient = Ingredient.deserialize(json.get("ingredient"));
			Integer priority = json.has("priority") ? JSONUtils.getInt(json, "priority") : null;
			
			return read(recipeId, json, ingredient, priority);
		}
		
		protected abstract T read(ResourceLocation recipeId, JsonObject json, Ingredient ingredient, Integer priority);
		
		@Nullable
		@Override
		public T read(ResourceLocation recipeId, PacketBuffer buffer)
		{
			Ingredient ingredient = Ingredient.read(buffer);
			int priority = buffer.readInt();
			
			return read(recipeId, buffer, ingredient, priority);
		}
		
		protected abstract T read(ResourceLocation recipeId, PacketBuffer buffer, Ingredient ingredient, int priority);
		
		@Override
		public void write(PacketBuffer buffer, T recipe)
		{
			recipe.ingredient.write(buffer);
			buffer.writeInt(recipe.getPriority());
		}
	}
}