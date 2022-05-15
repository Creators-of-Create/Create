package com.simibubi.create.content.logistics.block.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.block.display.source.DeathCounterDataSource;
import com.simibubi.create.content.logistics.block.display.source.DisplaySource;
import com.simibubi.create.content.logistics.block.display.source.ScoreboardDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTarget;
import com.simibubi.create.content.logistics.block.display.target.LecternDisplayTarget;
import com.simibubi.create.content.logistics.block.display.target.SignDisplayTarget;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AllDisplayBehaviours {

	public static final HashMap<ResourceLocation, DisplayBehaviour>

	GATHERER_BEHAVIOURS = new HashMap<>();

	public static final HashMap<ResourceLocation, List<DisplaySource>>

	SOURCES_BY_BLOCK = new HashMap<>(), SOURCES_BY_TILE = new HashMap<>();

	public static final HashMap<ResourceLocation, DisplayTarget>

	TARGETS_BY_BLOCK = new HashMap<>(), TARGETS_BY_TILE = new HashMap<>();

	public static DisplayBehaviour register(ResourceLocation id, DisplayBehaviour behaviour) {
		if (GATHERER_BEHAVIOURS.containsKey(id))
			Create.LOGGER.warn("Data Gatherer Behaviour for " + id.toString() + " was overridden");
		behaviour.id = id;
		GATHERER_BEHAVIOURS.put(id, behaviour);
		return behaviour;
	}

	public static void assign(DisplayBehaviour behaviour, Block block) {
		assignBlock(behaviour, block.getRegistryName());
	}

	public static void assign(DisplayBehaviour behaviour, BlockEntityType<?> teType) {
		assignTileEntity(behaviour, teType.getRegistryName());
	}

	public static void assignBlock(DisplayBehaviour behaviour, ResourceLocation blockId) {
		if (behaviour instanceof DisplaySource source)
			SOURCES_BY_BLOCK.computeIfAbsent(blockId, r -> new ArrayList<>())
				.add(source);
		if (behaviour instanceof DisplayTarget target)
			TARGETS_BY_BLOCK.put(blockId, target);
	}

	public static void assignTileEntity(DisplayBehaviour behaviour, ResourceLocation tileId) {
		if (behaviour instanceof DisplaySource source)
			SOURCES_BY_TILE.computeIfAbsent(tileId, r -> new ArrayList<>())
				.add(source);
		if (behaviour instanceof DisplayTarget target)
			TARGETS_BY_TILE.put(tileId, target);
	}

	public static <B extends Block> NonNullConsumer<? super B> assignDataBehaviour(DisplayBehaviour behaviour,
		String... suffix) {
		return b -> {
			ResourceLocation registryName = b.getRegistryName();
			String idSuffix = behaviour instanceof DisplaySource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignBlock(register(new ResourceLocation(registryName.getNamespace(), registryName.getPath() + idSuffix),
				behaviour), registryName);
		};
	}

	public static <B extends BlockEntityType<?>> NonNullConsumer<? super B> assignDataBehaviourTE(
		DisplayBehaviour behaviour, String... suffix) {
		return b -> {
			ResourceLocation registryName = b.getRegistryName();
			String idSuffix = behaviour instanceof DisplaySource ? "_source" : "_target";
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
	public static DisplaySource getSource(ResourceLocation resourceLocation) {
		DisplayBehaviour available = GATHERER_BEHAVIOURS.getOrDefault(resourceLocation, null);
		if (available instanceof DisplaySource source)
			return source;
		return null;
	}

	@Nullable
	public static DisplayTarget getTarget(ResourceLocation resourceLocation) {
		DisplayBehaviour available = GATHERER_BEHAVIOURS.getOrDefault(resourceLocation, null);
		if (available instanceof DisplayTarget target)
			return target;
		return null;
	}

	//

	@Nullable
	public static List<DisplaySource> sourcesOf(LevelAccessor level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);

		List<DisplaySource> sourcesOfBlock = sourcesOf(blockState);
		List<DisplaySource> sourcesOfTE = blockEntity == null ? Collections.emptyList() : sourcesOf(blockEntity);

		if (sourcesOfTE.isEmpty())
			return sourcesOfBlock;
		return sourcesOfTE;
	}

	@Nullable
	public static DisplayTarget targetOf(LevelAccessor level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);

		DisplayTarget targetOfBlock = targetOf(blockState);
		DisplayTarget targetOfTE = blockEntity == null ? null : targetOf(blockEntity);

		if (targetOfTE == null)
			return targetOfBlock;
		return targetOfTE;
	}

	public static List<DisplaySource> sourcesOf(BlockState state) {
		return sourcesOf(state.getBlock());
	}

	public static List<DisplaySource> sourcesOf(BlockEntity tileEntity) {
		return SOURCES_BY_TILE.getOrDefault(tileEntity.getType()
			.getRegistryName(), Collections.emptyList());
	}

	public static List<DisplaySource> sourcesOf(Block block) {
		return SOURCES_BY_BLOCK.getOrDefault(block.getRegistryName(), Collections.emptyList());
	}

	@Nullable
	public static DisplayTarget targetOf(BlockState state) {
		return targetOf(state.getBlock());
	}

	@Nullable
	public static DisplayTarget targetOf(BlockEntity tileEntity) {
		return TARGETS_BY_TILE.get(tileEntity.getType()
			.getRegistryName());
	}

	@Nullable
	public static DisplayTarget targetOf(Block block) {
		return TARGETS_BY_BLOCK.get(block.getRegistryName());
	}

	//

	public static void register() {
		assign(register(Create.asResource("sign_display_target"), new SignDisplayTarget()), BlockEntityType.SIGN);
		assign(register(Create.asResource("lectern_display_target"), new LecternDisplayTarget()), BlockEntityType.LECTERN);
		assign(register(Create.asResource("death_count_display_source"), new DeathCounterDataSource()),
			Blocks.RESPAWN_ANCHOR);
		assign(register(Create.asResource("scoreboard_display_source"), new ScoreboardDisplaySource()),
			BlockEntityType.COMMAND_BLOCK);
	}
}
