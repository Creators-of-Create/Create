package com.simibubi.create.content.contraptions.components.structureMovement;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public interface MovementBehaviour {

	default boolean isActive(MovementContext context) {
		return true;
	}

	default void tick(MovementContext context) {}

	default void startMoving(MovementContext context) {}

	default void visitNewPosition(MovementContext context, BlockPos pos) {}

	default Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.ZERO;
	}

	default void dropItem(MovementContext context, ItemStack stack) {
		ItemStack remainder;
		if (AllConfigs.SERVER.kinetics.moveItemsToStorage.get())
			remainder = ItemHandlerHelper.insertItem(context.contraption.getSharedInventory(), stack, false);
		else
			remainder = stack;
		if (remainder.isEmpty())
			return;

		Vec3 vec = context.position;
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

	default boolean renderAsNormalTileEntity() {
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
	default ActorInstance createInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		return null;
	}
}
