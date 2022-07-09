package com.simibubi.create.content.logistics.block.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.block.display.source.DeathCounterDisplaySource;
import com.simibubi.create.content.logistics.block.display.source.DisplaySource;
import com.simibubi.create.content.logistics.block.display.source.EnchantPowerDisplaySource;
import com.simibubi.create.content.logistics.block.display.source.RedstonePowerDisplaySource;
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
import net.minecraftforge.registries.IRegistryDelegate;

public class AllDisplayBehaviours {

	public static final Map<ResourceLocation, DisplayBehaviour> GATHERER_BEHAVIOURS = new HashMap<>();

	public static final Map<IRegistryDelegate<Block>, List<DisplaySource>> SOURCES_BY_BLOCK = new HashMap<>();
	public static final Map<IRegistryDelegate<BlockEntityType<?>>, List<DisplaySource>> SOURCES_BY_TILE = new HashMap<>();

	public static final Map<IRegistryDelegate<Block>, DisplayTarget> TARGETS_BY_BLOCK = new HashMap<>();
	public static final Map<IRegistryDelegate<BlockEntityType<?>>, DisplayTarget> TARGETS_BY_TILE = new HashMap<>();

	public static DisplayBehaviour register(ResourceLocation id, DisplayBehaviour behaviour) {
		behaviour.id = id;
		GATHERER_BEHAVIOURS.put(id, behaviour);
		return behaviour;
	}

	public static void assignBlock(DisplayBehaviour behaviour, IRegistryDelegate<Block> block) {
		if (behaviour instanceof DisplaySource source)
			SOURCES_BY_BLOCK.computeIfAbsent(block, r -> new ArrayList<>())
				.add(source);
		if (behaviour instanceof DisplayTarget target)
			TARGETS_BY_BLOCK.put(block, target);
	}

	public static void assignTile(DisplayBehaviour behaviour, IRegistryDelegate<BlockEntityType<?>> teType) {
		if (behaviour instanceof DisplaySource source)
			SOURCES_BY_TILE.computeIfAbsent(teType, r -> new ArrayList<>())
				.add(source);
		if (behaviour instanceof DisplayTarget target)
			TARGETS_BY_TILE.put(teType, target);
	}

	public static <B extends Block> NonNullConsumer<? super B> assignDataBehaviour(DisplayBehaviour behaviour,
		String... suffix) {
		return b -> {
			ResourceLocation registryName = b.getRegistryName();
			String idSuffix = behaviour instanceof DisplaySource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignBlock(register(new ResourceLocation(registryName.getNamespace(), registryName.getPath() + idSuffix),
				behaviour), b.delegate);
		};
	}

	public static <B extends BlockEntityType<?>> NonNullConsumer<? super B> assignDataBehaviourTE(
		DisplayBehaviour behaviour, String... suffix) {
		return b -> {
			ResourceLocation registryName = b.getRegistryName();
			String idSuffix = behaviour instanceof DisplaySource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignTile(
				register(new ResourceLocation(registryName.getNamespace(), registryName.getPath() + idSuffix),
					behaviour),
				b.delegate);
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

	public static List<DisplaySource> sourcesOf(Block block) {
		return SOURCES_BY_BLOCK.getOrDefault(block.getRegistryName(), Collections.emptyList());
	}

	public static List<DisplaySource> sourcesOf(BlockState state) {
		return sourcesOf(state.getBlock());
	}

	public static List<DisplaySource> sourcesOf(BlockEntityType<?> tileEntityType) {
		return SOURCES_BY_TILE.getOrDefault(tileEntityType, Collections.emptyList());
	}

	public static List<DisplaySource> sourcesOf(BlockEntity tileEntity) {
		return sourcesOf(tileEntity.getType());
	}

	@Nullable
	public static DisplayTarget targetOf(Block block) {
		return TARGETS_BY_BLOCK.get(block.delegate);
	}

	@Nullable
	public static DisplayTarget targetOf(BlockState state) {
		return targetOf(state.getBlock());
	}

	@Nullable
	public static DisplayTarget targetOf(BlockEntityType<?> tileEntityType) {
		return TARGETS_BY_TILE.get(tileEntityType.delegate);
	}

	@Nullable
	public static DisplayTarget targetOf(BlockEntity tileEntity) {
		return targetOf(tileEntity.getType());
	}

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

	//

	public static void registerDefaults() {
		assignTile(register(Create.asResource("sign_display_target"), new SignDisplayTarget()), BlockEntityType.SIGN.delegate);
		assignTile(register(Create.asResource("lectern_display_target"), new LecternDisplayTarget()), BlockEntityType.LECTERN.delegate);
		assignBlock(register(Create.asResource("death_count_display_source"), new DeathCounterDisplaySource()), Blocks.RESPAWN_ANCHOR.delegate);
		assignTile(register(Create.asResource("scoreboard_display_source"), new ScoreboardDisplaySource()), BlockEntityType.COMMAND_BLOCK.delegate);
		assignTile(register(Create.asResource("enchant_power_display_source"), new EnchantPowerDisplaySource()), BlockEntityType.ENCHANTING_TABLE.delegate);
		assignBlock(register(Create.asResource("redstone_power_display_source"), new RedstonePowerDisplaySource()), Blocks.TARGET.delegate);
	}
}
