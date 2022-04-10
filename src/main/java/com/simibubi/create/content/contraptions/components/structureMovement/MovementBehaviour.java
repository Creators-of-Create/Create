package com.simibubi.create.content.contraptions.components.structureMovement;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.config.AllConfigs;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface MovementBehaviour {

	default boolean isActive(MovementContext context) {
		return true;
	}

	default void tick(MovementContext context) {
	}

	default void startMoving(MovementContext context) {
	}

	default void visitNewPosition(MovementContext context, BlockPos pos) {
	}

	default Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.ZERO;
	}

	default void dropItem(MovementContext context, ItemStack stack) {
		ItemStack remainder;
		if (AllConfigs.SERVER.kinetics.moveItemsToStorage.get()) {
			try (Transaction t = TransferUtil.getTransaction()) {
				long inserted = context.contraption.inventory.insert(ItemVariant.of(stack), stack.getCount(), t);
				remainder = stack.copy();
				remainder.shrink((int) inserted);
				t.commit();
			}
		}
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

	default void onSpeedChanged(MovementContext context, Vec3 oldMotion, Vec3 motion) {
	}

	default void stopMoving(MovementContext context) {
	}

	default void writeExtraData(MovementContext context) {
	}

	default boolean renderAsNormalTileEntity() {
		return false;
	}

	default boolean hasSpecialInstancedRendering() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	default void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
	}

	@Environment(EnvType.CLIENT)
	@Nullable
	default ActorInstance createInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld, MovementContext context) {
		return null;
	}
}
