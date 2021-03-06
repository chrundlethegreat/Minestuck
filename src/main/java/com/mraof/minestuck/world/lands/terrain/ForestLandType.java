package com.mraof.minestuck.world.lands.terrain;

import com.mraof.minestuck.Minestuck;
import com.mraof.minestuck.block.MSBlocks;
import com.mraof.minestuck.entity.MSEntityTypes;
import com.mraof.minestuck.entity.consort.ConsortEntity;
import com.mraof.minestuck.world.biome.LandWrapperBiome;
import com.mraof.minestuck.world.biome.MSBiomes;
import com.mraof.minestuck.world.gen.feature.structure.blocks.StructureBlockRegistry;
import com.mraof.minestuck.world.lands.LandProperties;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.DefaultBiomeFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placement.AtSurfaceWithExtraConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;

public class ForestLandType extends TerrainLandType
{
	public static final String FORESTS = "minestuck.forests";
	public static final String TREES = "minestuck.trees";
	public static final String BOREAL_FORESTS = "minestuck.boreal_forests";
	public static final String TAIGAS = "minestuck.taigas";
	public static final String COLD_FORESTS = "minestuck.cold_forests";
	
	public static final ResourceLocation GROUP_NAME = new ResourceLocation(Minestuck.MOD_ID, "forest");
	private final Variant type;
	
	public ForestLandType(Variant variation)
	{
		super(GROUP_NAME);
		type = variation;
	}
	
	@Override
	public void registerBlocks(StructureBlockRegistry registry)
	{
		registry.setBlockState("surface", type == Variant.TAIGA ? Blocks.PODZOL.getDefaultState() : Blocks.GRASS_BLOCK.getDefaultState());
		registry.setBlockState("upper", Blocks.DIRT.getDefaultState());
		if(type == Variant.TAIGA) {
			registry.setBlockState("structure_primary", Blocks.SPRUCE_WOOD.getDefaultState());
			registry.setBlockState("structure_primary_decorative", MSBlocks.FROST_WOOD.getDefaultState());
		} else {
			registry.setBlockState("structure_primary", MSBlocks.VINE_WOOD.getDefaultState());
			registry.setBlockState("structure_primary_decorative", MSBlocks.FLOWERY_VINE_WOOD.getDefaultState());
		}
		registry.setBlockState("structure_secondary", Blocks.STONE_BRICKS.getDefaultState());
		registry.setBlockState("structure_secondary_decorative", Blocks.CHISELED_STONE_BRICKS.getDefaultState());
		registry.setBlockState("structure_secondary_stairs", Blocks.STONE_BRICK_STAIRS.getDefaultState());
		registry.setBlockState("village_path", Blocks.GRASS_PATH.getDefaultState());
		registry.setBlockState("bush", Blocks.FERN.getDefaultState());
		registry.setBlockState("structure_wool_1", Blocks.GREEN_WOOL.getDefaultState());
		if(type == Variant.TAIGA) {
			registry.setBlockState("structure_wool_3", Blocks.LIGHT_BLUE_WOOL.getDefaultState());
		} else {
			registry.setBlockState("structure_wool_3", Blocks.BROWN_WOOL.getDefaultState());
		}
	}
	
	@Override
	public String[] getNames()
	{
		if(type == Variant.FOREST) {
			return new String[] {FORESTS, TREES};
		} else {
			return new String[] {TAIGAS, BOREAL_FORESTS, COLD_FORESTS};
		}
	}
	
	@Override
	public void setProperties(LandProperties properties)
	{
		properties.category = this.type == Variant.TAIGA ? Biome.Category.TAIGA : Biome.Category.FOREST;
		properties.downfall = 0.8F;
		properties.rainType = Biome.RainType.RAIN;
		properties.forceRain = LandProperties.ForceType.DEFAULT;
	}
	
	@Override
	public void setBiomeSettings(LandWrapperBiome biome, StructureBlockRegistry blocks)
	{
		if(biome.staticBiome == MSBiomes.LAND_NORMAL)
		{
			biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.GRASS, new GrassFeatureConfig(Blocks.GRASS.getDefaultState()), Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(2)));
			
			switch(this.type)
			{
				case FOREST:
					biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.RANDOM_SELECTOR, new MultipleRandomFeatureConfig(new Feature[]{Feature.BIRCH_TREE, Feature.FANCY_TREE}, new IFeatureConfig[]{IFeatureConfig.NO_FEATURE_CONFIG, IFeatureConfig.NO_FEATURE_CONFIG}, new float[]{0.2F, 0.1F}, Feature.NORMAL_TREE, IFeatureConfig.NO_FEATURE_CONFIG), Placement.COUNT_EXTRA_HEIGHTMAP, new AtSurfaceWithExtraConfig(5, 0.1F, 1)));
					break;
				case TAIGA:
					biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.RANDOM_SELECTOR, new MultipleRandomFeatureConfig(new Feature[]{Feature.PINE_TREE}, new IFeatureConfig[]{IFeatureConfig.NO_FEATURE_CONFIG}, new float[]{1/3F}, Feature.SPRUCE_TREE, IFeatureConfig.NO_FEATURE_CONFIG), Placement.COUNT_EXTRA_HEIGHTMAP, new AtSurfaceWithExtraConfig(5, 0.1F, 1)));
					break;
			}
		} else if(biome.staticBiome == MSBiomes.LAND_ROUGH)
		{
			biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.GRASS, new GrassFeatureConfig(Blocks.GRASS.getDefaultState()), Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(3)));
			biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.GRASS, new GrassFeatureConfig(Blocks.FERN.getDefaultState()), Placement.COUNT_HEIGHTMAP_DOUBLE, new FrequencyConfig(1)));
			
			switch(this.type)
			{
				case FOREST:
					biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.RANDOM_SELECTOR, new MultipleRandomFeatureConfig(new Feature[]{Feature.BIRCH_TREE, Feature.FANCY_TREE}, new IFeatureConfig[]{IFeatureConfig.NO_FEATURE_CONFIG, IFeatureConfig.NO_FEATURE_CONFIG}, new float[]{0.2F, 0.1F}, Feature.NORMAL_TREE, IFeatureConfig.NO_FEATURE_CONFIG), Placement.COUNT_EXTRA_HEIGHTMAP, new AtSurfaceWithExtraConfig(10, 0.1F, 1)));
					break;
				case TAIGA:
					biome.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(Feature.RANDOM_SELECTOR, new MultipleRandomFeatureConfig(new Feature[]{Feature.PINE_TREE}, new IFeatureConfig[]{IFeatureConfig.NO_FEATURE_CONFIG}, new float[]{0.33333334F}, Feature.SPRUCE_TREE, IFeatureConfig.NO_FEATURE_CONFIG), Placement.COUNT_EXTRA_HEIGHTMAP, new AtSurfaceWithExtraConfig(10, 0.1F, 1)));
					break;
			}
		} else if(biome.staticBiome == MSBiomes.LAND_OCEAN)
		{
			DefaultBiomeFeatures.addSwampClayDisks(biome);
		}
		
		biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(Feature.ORE, new OreFeatureConfig(blocks.getGroundType(), Blocks.DIRT.getDefaultState(), 33), Placement.COUNT_RANGE, new CountRangeConfig(10, 0, 0, 256)));
		biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(Feature.ORE, new OreFeatureConfig(blocks.getGroundType(), Blocks.GRAVEL.getDefaultState(), 33), Placement.COUNT_RANGE, new CountRangeConfig(8, 0, 0, 256)));
		biome.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, Biome.createDecoratedFeature(Feature.ORE, new OreFeatureConfig(blocks.getGroundType(), Blocks.COAL_ORE.getDefaultState(), 17), Placement.COUNT_RANGE, new CountRangeConfig(20, 0, 0, 128)));
		DefaultBiomeFeatures.addExtraEmeraldOre(biome);
		
	}
	
	@Override
	public Vec3d getFogColor()
	{
		return new Vec3d(0.0D, 1.0D, 0.6D);
	}
	
	@Override
	public Vec3d getSkyColor()
	{
		return new Vec3d(0.4D, 0.7D, 1.0D);
	}
	
	@Override
	public EntityType<? extends ConsortEntity> getConsortType()
	{
		return MSEntityTypes.IGUANA;
	}
	
	public enum Variant
	{
		FOREST,
		TAIGA
	}
}