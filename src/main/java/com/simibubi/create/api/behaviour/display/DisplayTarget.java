package com.simibubi.create.api.behaviour.display;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDisplayTargets;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.CreateLang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class DisplayTarget {
	public static final SimpleRegistry<Block, DisplayTarget> BY_BLOCK = SimpleRegistry.create();
	public static final SimpleRegistry<BlockEntityType<?>, DisplayTarget> BY_BLOCK_ENTITY = SimpleRegistry.create();

	public abstract void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context);

	public abstract DisplayTargetStats provideStats(DisplayLinkContext context);

	public AABB getMultiblockBounds(LevelAccessor level, BlockPos pos) {
		VoxelShape shape = level.getBlockState(pos)
			.getShape(level, pos);
		if (shape.isEmpty())
			return new AABB(pos);
		return shape.bounds()
			.move(pos);
	}

	public Component getLineOptionText(int line) {
		return CreateLang.translateDirect("display_target.line", line + 1);
	}

	public static void reserve(int line, BlockEntity target, DisplayLinkContext context) {
		if (line == 0)
			return;

		CompoundTag tag = target.getPersistentData();
		CompoundTag compound = tag.getCompound("DisplayLink");
		compound.putLong("Line" + line, context.blockEntity()
			.getBlockPos()
			.asLong());
		tag.put("DisplayLink", compound);
	}

	public boolean isReserved(int line, BlockEntity target, DisplayLinkContext context) {
		CompoundTag tag = target.getPersistentData();
		CompoundTag compound = tag.getCompound("DisplayLink");

		if (!compound.contains("Line" + line))
			return false;

		long l = compound.getLong("Line" + line);
		BlockPos reserved = BlockPos.of(l);

		if (!reserved.equals(context.blockEntity()
			.getBlockPos()) && AllBlocks.DISPLAY_LINK.has(target.getLevel()
				.getBlockState(reserved)))
			return true;

		compound.remove("Line" + line);
		if (compound.isEmpty())
			tag.remove("DisplayLink");
		return false;
	}

	public boolean requiresComponentSanitization() {
		return false;
	}

	/**
	 * Utility for use with Registrate builders. Creates a builder transformer
	 * that will register the given DisplayTarget to a block when ready.
	 */
	public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> displayTarget(RegistryEntry<DisplayTarget, ? extends DisplayTarget> target) {
		return builder -> builder.onRegisterAfter(CreateRegistries.DISPLAY_TARGET, block -> BY_BLOCK.register(block, target.get()));
	}

	/**
	 * Get the DisplayTarget with the given ID, accounting for legacy names.
	 */
	@Nullable
	public static DisplayTarget get(@Nullable ResourceLocation id) {
		if (id == null)
			return null;

		if (id.getNamespace().equals(Create.ID) && AllDisplayTargets.LEGACY_NAMES.containsKey(id.getPath())) {
			return AllDisplayTargets.LEGACY_NAMES.get(id.getPath()).get();
		}

		return CreateBuiltInRegistries.DISPLAY_TARGET.get(id);
	}

	/**
	 * Get the DisplayTarget applicable to the given location, or null if there isn't one.
	 */
	@Nullable
	public static DisplayTarget get(LevelAccessor level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		DisplayTarget byBlock = BY_BLOCK.get(state);
		// block takes priority if present, it's more granular
		if (byBlock != null)
			return byBlock;

		BlockEntity be = level.getBlockEntity(pos);
		if (be == null)
			return null;

		DisplayTarget byBe = BY_BLOCK_ENTITY.get(be.getType());
		if (byBe != null)
			return byBe;

		// special case: modded signs are common
		return be instanceof SignBlockEntity ? AllDisplayTargets.SIGN.get() : null;
	}
}
