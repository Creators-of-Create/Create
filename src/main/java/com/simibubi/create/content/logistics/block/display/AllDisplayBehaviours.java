package com.simibubi.create.content.logistics.block.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.logistics.block.display.source.ComputerDisplaySource;
import com.simibubi.create.content.logistics.block.display.source.DeathCounterDisplaySource;
import com.simibubi.create.content.logistics.block.display.source.DisplaySource;
import com.simibubi.create.content.logistics.block.display.source.EnchantPowerDisplaySource;
import com.simibubi.create.content.logistics.block.display.source.RedstonePowerDisplaySource;
import com.simibubi.create.content.logistics.block.display.source.ScoreboardDisplaySource;
import com.simibubi.create.content.logistics.block.display.target.DisplayTarget;
import com.simibubi.create.content.logistics.block.display.target.LecternDisplayTarget;
import com.simibubi.create.content.logistics.block.display.target.SignDisplayTarget;
import com.simibubi.create.foundation.utility.CreateRegistry;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;

public class AllDisplayBehaviours {
	public static final Map<ResourceLocation, DisplayBehaviour> GATHERER_BEHAVIOURS = new HashMap<>();

	private static final CreateRegistry<Block, List<DisplaySource>> SOURCES_BY_BLOCK = new CreateRegistry<>(ForgeRegistries.BLOCKS);
	private static final CreateRegistry<BlockEntityType<?>, List<DisplaySource>> SOURCES_BY_TILE = new CreateRegistry<>(ForgeRegistries.BLOCK_ENTITIES);

	private static final CreateRegistry<Block, DisplayTarget> TARGETS_BY_BLOCK = new CreateRegistry<>(ForgeRegistries.BLOCKS);
	private static final CreateRegistry<BlockEntityType<?>, DisplayTarget> TARGETS_BY_TILE = new CreateRegistry<>(ForgeRegistries.BLOCK_ENTITIES);

	public static DisplayBehaviour register(ResourceLocation id, DisplayBehaviour behaviour) {
		behaviour.id = id;
		GATHERER_BEHAVIOURS.put(id, behaviour);
		return behaviour;
	}

	public static void assignBlock(DisplayBehaviour behaviour, ResourceLocation block) {
		if (behaviour instanceof DisplaySource source) {
			List<DisplaySource> sources = SOURCES_BY_BLOCK.get(block);
			if (sources == null) {
				sources = new ArrayList<>();
				SOURCES_BY_BLOCK.register(block, sources);
			}
			sources.add(source);
		}
		if (behaviour instanceof DisplayTarget target) {
			TARGETS_BY_BLOCK.register(block, target);
		}
	}

	public static void assignTile(DisplayBehaviour behaviour, ResourceLocation teType) {
		if (behaviour instanceof DisplaySource source) {
			List<DisplaySource> sources = SOURCES_BY_TILE.get(teType);
			if (sources == null) {
				sources = new ArrayList<>();
				SOURCES_BY_TILE.register(teType, sources);
			}
			sources.add(source);
		}
		if (behaviour instanceof DisplayTarget target) {
			TARGETS_BY_TILE.register(teType, target);
		}
	}

	public static void assignBlock(DisplayBehaviour behaviour, Block block) {
		if (behaviour instanceof DisplaySource source) {
			List<DisplaySource> sources = SOURCES_BY_BLOCK.get(block);
			if (sources == null) {
				sources = new ArrayList<>();
				SOURCES_BY_BLOCK.register(block, sources);
			}
			sources.add(source);
		}
		if (behaviour instanceof DisplayTarget target) {
			TARGETS_BY_BLOCK.register(block, target);
		}
	}

	public static void assignTile(DisplayBehaviour behaviour, BlockEntityType<?> teType) {
		if (behaviour instanceof DisplaySource source) {
			List<DisplaySource> sources = SOURCES_BY_TILE.get(teType);
			if (sources == null) {
				sources = new ArrayList<>();
				SOURCES_BY_TILE.register(teType, sources);
			}
			sources.add(source);
		}
		if (behaviour instanceof DisplayTarget target) {
			TARGETS_BY_TILE.register(teType, target);
		}
	}

	@Deprecated(forRemoval = true)
	public static void assignBlock(DisplayBehaviour behaviour, IRegistryDelegate<Block> block) {
		assignBlock(behaviour, block.name());
	}

	@Deprecated(forRemoval = true)
	public static void assignTile(DisplayBehaviour behaviour, IRegistryDelegate<BlockEntityType<?>> teType) {
		assignTile(behaviour, teType.name());
	}

	public static <B extends Block> NonNullConsumer<? super B> assignDataBehaviour(DisplayBehaviour behaviour,
		String... suffix) {
		return b -> {
			ResourceLocation registryName = RegisteredObjects.getKeyOrThrow(b);
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
			ResourceLocation registryName = RegisteredObjects.getKeyOrThrow(b);
			String idSuffix = behaviour instanceof DisplaySource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignTile(
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

	public static List<DisplaySource> sourcesOf(Block block) {
		List<DisplaySource> sources = SOURCES_BY_BLOCK.get(block);
		if (sources == null) {
			return Collections.emptyList();
		}
		return sources;
	}

	public static List<DisplaySource> sourcesOf(BlockState state) {
		return sourcesOf(state.getBlock());
	}

	public static List<DisplaySource> sourcesOf(BlockEntityType<?> tileEntityType) {
		List<DisplaySource> sources = SOURCES_BY_TILE.get(tileEntityType);
		if (sources == null) {
			return Collections.emptyList();
		}
		return sources;
	}

	public static List<DisplaySource> sourcesOf(BlockEntity tileEntity) {
		return sourcesOf(tileEntity.getType());
	}

	@Nullable
	public static DisplayTarget targetOf(Block block) {
		return TARGETS_BY_BLOCK.get(block);
	}

	@Nullable
	public static DisplayTarget targetOf(BlockState state) {
		return targetOf(state.getBlock());
	}

	@Nullable
	public static DisplayTarget targetOf(BlockEntityType<?> tileEntityType) {
		return TARGETS_BY_TILE.get(tileEntityType);
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
		assignTile(register(Create.asResource("sign_display_target"), new SignDisplayTarget()), BlockEntityType.SIGN);
		assignTile(register(Create.asResource("lectern_display_target"), new LecternDisplayTarget()), BlockEntityType.LECTERN);
		assignBlock(register(Create.asResource("death_count_display_source"), new DeathCounterDisplaySource()), Blocks.RESPAWN_ANCHOR);
		assignTile(register(Create.asResource("scoreboard_display_source"), new ScoreboardDisplaySource()), BlockEntityType.COMMAND_BLOCK);
		assignTile(register(Create.asResource("enchant_power_display_source"), new EnchantPowerDisplaySource()), BlockEntityType.ENCHANTING_TABLE);
		assignBlock(register(Create.asResource("redstone_power_display_source"), new RedstonePowerDisplaySource()), Blocks.TARGET);

		Mods.COMPUTERCRAFT.executeIfInstalled(() -> () -> {
			DisplayBehaviour computerDisplaySource = register(Create.asResource("computer_display_source"), new ComputerDisplaySource());

			assignTile(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "wired_modem_full"));
			assignTile(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "computer_normal"));
			assignTile(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "computer_advanced"));
			assignTile(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "computer_command"));
		});
	}
}
