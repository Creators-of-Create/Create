package com.simibubi.create.content.logistics.block.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.block.data.source.DataGathererSource;
import com.simibubi.create.content.logistics.block.data.source.DeathCounterDataSource;
import com.simibubi.create.content.logistics.block.data.source.ScoreboardDataSource;
import com.simibubi.create.content.logistics.block.data.target.DataGathererTarget;
import com.simibubi.create.content.logistics.block.data.target.LecternDataTarget;
import com.simibubi.create.content.logistics.block.data.target.SignDataTarget;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AllDataGathererBehaviours {

	public static final HashMap<ResourceLocation, DataGathererBehaviour>

	GATHERER_BEHAVIOURS = new HashMap<>();

	public static final HashMap<ResourceLocation, List<DataGathererSource>>

	SOURCES_BY_BLOCK = new HashMap<>(), SOURCES_BY_TILE = new HashMap<>();

	public static final HashMap<ResourceLocation, DataGathererTarget>

	TARGETS_BY_BLOCK = new HashMap<>(), TARGETS_BY_TILE = new HashMap<>();

	public static DataGathererBehaviour register(ResourceLocation id, DataGathererBehaviour behaviour) {
		if (GATHERER_BEHAVIOURS.containsKey(id))
			Create.LOGGER.warn("Data Gatherer Behaviour for " + id.toString() + " was overridden");
		behaviour.id = id;
		GATHERER_BEHAVIOURS.put(id, behaviour);
		return behaviour;
	}

	public static void assign(DataGathererBehaviour behaviour, Block block) {
		assignBlock(behaviour, block.getRegistryName());
	}

	public static void assign(DataGathererBehaviour behaviour, BlockEntityType<?> teType) {
		assignTileEntity(behaviour, teType.getRegistryName());
	}

	public static void assignBlock(DataGathererBehaviour behaviour, ResourceLocation blockId) {
		if (behaviour instanceof DataGathererSource source)
			SOURCES_BY_BLOCK.computeIfAbsent(blockId, r -> new ArrayList<>())
				.add(source);
		if (behaviour instanceof DataGathererTarget target)
			TARGETS_BY_BLOCK.put(blockId, target);
	}

	public static void assignTileEntity(DataGathererBehaviour behaviour, ResourceLocation tileId) {
		if (behaviour instanceof DataGathererSource source)
			SOURCES_BY_TILE.computeIfAbsent(tileId, r -> new ArrayList<>())
				.add(source);
		if (behaviour instanceof DataGathererTarget target)
			TARGETS_BY_TILE.put(tileId, target);
	}

	public static <B extends Block> NonNullConsumer<? super B> assignDataBehaviour(DataGathererBehaviour behaviour,
		String... suffix) {
		return b -> {
			ResourceLocation registryName = b.getRegistryName();
			String idSuffix = behaviour instanceof DataGathererSource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignBlock(register(new ResourceLocation(registryName.getNamespace(), registryName.getPath() + idSuffix),
				behaviour), registryName);
		};
	}

	public static <B extends BlockEntityType<?>> NonNullConsumer<? super B> assignDataBehaviourTE(
		DataGathererBehaviour behaviour, String... suffix) {
		return b -> {
			ResourceLocation registryName = b.getRegistryName();
			String idSuffix = behaviour instanceof DataGathererSource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignTileEntity(
				register(new ResourceLocation(registryName.getNamespace(), registryName.getPath() + idSuffix),
					behaviour),
				registryName);
		};
	}

	//

	@Nullable
	public static DataGathererSource getSource(ResourceLocation resourceLocation) {
		DataGathererBehaviour available = GATHERER_BEHAVIOURS.getOrDefault(resourceLocation, null);
		if (available instanceof DataGathererSource source)
			return source;
		return null;
	}

	@Nullable
	public static DataGathererTarget getTarget(ResourceLocation resourceLocation) {
		DataGathererBehaviour available = GATHERER_BEHAVIOURS.getOrDefault(resourceLocation, null);
		if (available instanceof DataGathererTarget target)
			return target;
		return null;
	}

	//

	@Nullable
	public static List<DataGathererSource> sourcesOf(LevelAccessor level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);

		List<DataGathererSource> sourcesOfBlock = sourcesOf(blockState);
		List<DataGathererSource> sourcesOfTE = blockEntity == null ? Collections.emptyList() : sourcesOf(blockEntity);

		if (sourcesOfTE.isEmpty())
			return sourcesOfBlock;
		return sourcesOfTE;
	}

	@Nullable
	public static DataGathererTarget targetOf(LevelAccessor level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);

		DataGathererTarget targetOfBlock = targetOf(blockState);
		DataGathererTarget targetOfTE = blockEntity == null ? null : targetOf(blockEntity);

		if (targetOfTE == null)
			return targetOfBlock;
		return targetOfTE;
	}

	public static List<DataGathererSource> sourcesOf(BlockState state) {
		return sourcesOf(state.getBlock());
	}

	public static List<DataGathererSource> sourcesOf(BlockEntity tileEntity) {
		return SOURCES_BY_TILE.getOrDefault(tileEntity.getType()
			.getRegistryName(), Collections.emptyList());
	}

	public static List<DataGathererSource> sourcesOf(Block block) {
		return SOURCES_BY_BLOCK.getOrDefault(block.getRegistryName(), Collections.emptyList());
	}

	@Nullable
	public static DataGathererTarget targetOf(BlockState state) {
		return targetOf(state.getBlock());
	}

	@Nullable
	public static DataGathererTarget targetOf(BlockEntity tileEntity) {
		return TARGETS_BY_TILE.get(tileEntity.getType()
			.getRegistryName());
	}

	@Nullable
	public static DataGathererTarget targetOf(Block block) {
		return TARGETS_BY_BLOCK.get(block.getRegistryName());
	}

	//

	public static void register() {
		assign(register(Create.asResource("sign_data_target"), new SignDataTarget()), BlockEntityType.SIGN);
		assign(register(Create.asResource("lectern_data_target"), new LecternDataTarget()), BlockEntityType.LECTERN);
		assign(register(Create.asResource("death_count_data_source"), new DeathCounterDataSource()),
			Blocks.RESPAWN_ANCHOR);
		assign(register(Create.asResource("scoreboard_data_source"), new ScoreboardDataSource()),
			BlockEntityType.COMMAND_BLOCK);
	}
}
