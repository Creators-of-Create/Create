package com.simibubi.create.content.redstone.displayLink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.redstone.displayLink.source.ComputerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.DeathCounterDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.EnchantPowerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.RedstonePowerDisplaySource;
import com.simibubi.create.content.redstone.displayLink.source.ScoreboardDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.LecternDisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.SignDisplayTarget;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class AllDisplayBehaviours {
	public static final Map<ResourceLocation, DisplayBehaviour> GATHERER_BEHAVIOURS = new HashMap<>();

	private static final AttachedRegistry<Block, List<DisplaySource>> SOURCES_BY_BLOCK = new AttachedRegistry<>(ForgeRegistries.BLOCKS);
	private static final AttachedRegistry<BlockEntityType<?>, List<DisplaySource>> SOURCES_BY_BLOCK_ENTITY = new AttachedRegistry<>(ForgeRegistries.BLOCK_ENTITIES);

	private static final AttachedRegistry<Block, DisplayTarget> TARGETS_BY_BLOCK = new AttachedRegistry<>(ForgeRegistries.BLOCKS);
	private static final AttachedRegistry<BlockEntityType<?>, DisplayTarget> TARGETS_BY_BLOCK_ENTITY = new AttachedRegistry<>(ForgeRegistries.BLOCK_ENTITIES);

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

	public static void assignBlockEntity(DisplayBehaviour behaviour, ResourceLocation beType) {
		if (behaviour instanceof DisplaySource source) {
			List<DisplaySource> sources = SOURCES_BY_BLOCK_ENTITY.get(beType);
			if (sources == null) {
				sources = new ArrayList<>();
				SOURCES_BY_BLOCK_ENTITY.register(beType, sources);
			}
			sources.add(source);
		}
		if (behaviour instanceof DisplayTarget target) {
			TARGETS_BY_BLOCK_ENTITY.register(beType, target);
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

	public static void assignBlockEntity(DisplayBehaviour behaviour, BlockEntityType<?> beType) {
		if (behaviour instanceof DisplaySource source) {
			List<DisplaySource> sources = SOURCES_BY_BLOCK_ENTITY.get(beType);
			if (sources == null) {
				sources = new ArrayList<>();
				SOURCES_BY_BLOCK_ENTITY.register(beType, sources);
			}
			sources.add(source);
		}
		if (behaviour instanceof DisplayTarget target) {
			TARGETS_BY_BLOCK_ENTITY.register(beType, target);
		}
	}

	public static <B extends Block> NonNullConsumer<? super B> assignDataBehaviour(DisplayBehaviour behaviour,
		String... suffix) {
		return b -> {
			ResourceLocation registryName = CatnipServices.REGISTRIES.getKeyOrThrow(b);
			String idSuffix = behaviour instanceof DisplaySource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignBlock(register(new ResourceLocation(registryName.getNamespace(), registryName.getPath() + idSuffix),
				behaviour), registryName);
		};
	}

	public static <B extends BlockEntityType<?>> NonNullConsumer<? super B> assignDataBehaviourBE(
		DisplayBehaviour behaviour, String... suffix) {
		return b -> {
			ResourceLocation registryName = CatnipServices.REGISTRIES.getKeyOrThrow(b);
			String idSuffix = behaviour instanceof DisplaySource ? "_source" : "_target";
			if (suffix.length > 0)
				idSuffix += "_" + suffix[0];
			assignBlockEntity(
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

	public static List<DisplaySource> sourcesOf(BlockEntityType<?> blockEntityType) {
		List<DisplaySource> sources = SOURCES_BY_BLOCK_ENTITY.get(blockEntityType);
		if (sources == null) {
			return Collections.emptyList();
		}
		return sources;
	}

	public static List<DisplaySource> sourcesOf(BlockEntity blockEntity) {
		return sourcesOf(blockEntity.getType());
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
	public static DisplayTarget targetOf(BlockEntityType<?> blockEntityType) {
		return TARGETS_BY_BLOCK_ENTITY.get(blockEntityType);
	}

	@Nullable
	public static DisplayTarget targetOf(BlockEntity blockEntity) {
		return targetOf(blockEntity.getType());
	}

	public static List<DisplaySource> sourcesOf(LevelAccessor level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);

		List<DisplaySource> sourcesOfBlock = sourcesOf(blockState);
		List<DisplaySource> sourcesOfBlockEntity = blockEntity == null ? Collections.emptyList() : sourcesOf(blockEntity);

		if (sourcesOfBlockEntity.isEmpty())
			return sourcesOfBlock;
		return sourcesOfBlockEntity;
	}

	@Nullable
	public static DisplayTarget targetOf(LevelAccessor level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);
		BlockEntity blockEntity = level.getBlockEntity(pos);

		DisplayTarget targetOfBlock = targetOf(blockState);
		DisplayTarget targetOfBlockEntity = blockEntity == null ? null : targetOf(blockEntity);

		// Commonly added by mods, but with a non-vanilla blockentitytype
		if (targetOfBlockEntity == null && blockEntity instanceof SignBlockEntity)
			targetOfBlockEntity = targetOf(BlockEntityType.SIGN);

		if (targetOfBlockEntity == null)
			return targetOfBlock;
		return targetOfBlockEntity;
	}

	//

	public static void registerDefaults() {
		assignBlockEntity(register(Create.asResource("sign_display_target"), new SignDisplayTarget()), BlockEntityType.SIGN);
		assignBlockEntity(register(Create.asResource("lectern_display_target"), new LecternDisplayTarget()), BlockEntityType.LECTERN);
		assignBlock(register(Create.asResource("death_count_display_source"), new DeathCounterDisplaySource()), Blocks.RESPAWN_ANCHOR);
		assignBlockEntity(register(Create.asResource("scoreboard_display_source"), new ScoreboardDisplaySource()), BlockEntityType.COMMAND_BLOCK);
		assignBlockEntity(register(Create.asResource("enchant_power_display_source"), new EnchantPowerDisplaySource()), BlockEntityType.ENCHANTING_TABLE);
		assignBlock(register(Create.asResource("redstone_power_display_source"), new RedstonePowerDisplaySource()), Blocks.TARGET);

		Mods.COMPUTERCRAFT.executeIfInstalled(() -> () -> {
			DisplayBehaviour computerDisplaySource = register(Create.asResource("computer_display_source"), new ComputerDisplaySource());

			assignBlockEntity(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "wired_modem_full"));
			assignBlockEntity(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "computer_normal"));
			assignBlockEntity(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "computer_advanced"));
			assignBlockEntity(computerDisplaySource, new ResourceLocation(Mods.COMPUTERCRAFT.asId(), "computer_command"));
		});
	}
}
