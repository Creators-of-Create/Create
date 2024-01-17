package com.simibubi.create.content.contraptions.behaviour;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public interface MovementBehaviour {

	default boolean isActive(MovementContext context) {
		return !context.disabled;
	}

	default void tick(MovementContext context) {}

	default void startMoving(MovementContext context) {}

	default void visitNewPosition(MovementContext context, BlockPos pos) {}

	default Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.ZERO;
	}

	@Nullable
	default ItemStack canBeDisabledVia(MovementContext context) {
		Block block = context.state.getBlock();
		if (block == null)
			return null;
		return new ItemStack(block);
	}

	default void onDisabledByControls(MovementContext context) {
		cancelStall(context);
	}

	default boolean mustTickWhileDisabled() {
		return false;
	}

	default void dropItem(MovementContext context, ItemStack stack) {
		ItemStack remainder;
		if (AllConfigs.server().kinetics.moveItemsToStorage.get())
			remainder = ItemHandlerHelper.insertItem(context.contraption.getSharedInventory(), stack, false);
		else
			remainder = stack;
		if (remainder.isEmpty())
			return;

		// Actors might void items if their positions is undefined
		Vec3 vec = context.position;
		if (vec == null)
			return;

		ItemEntity itemEntity = new ItemEntity(context.world, vec.x, vec.y, vec.z, remainder);
		itemEntity.setDeltaMovement(context.motion.add(0, 0.5f, 0)
			.scale(context.world.random.nextFloat() * .3f));
		context.world.addFreshEntity(itemEntity);
	}

	default void onSpeedChanged(MovementContext context, Vec3 oldMotion, Vec3 motion) {}

	default void stopMoving(MovementContext context) {}

	default void cancelStall(MovementContext context) {
		context.stall = false;
	}

	default void writeExtraData(MovementContext context) {}

	default boolean renderAsNormalBlockEntity() {
		return false;
	}

	default boolean hasSpecialInstancedRendering() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	default void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	default ActorVisual createInstance(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld,
		MovementContext movementContext) {
		return null;
	}
}
