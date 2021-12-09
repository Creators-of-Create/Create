package com.simibubi.create.content.contraptions.components.structureMovement;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class MovementBehaviour {

	public boolean isActive(MovementContext context) {
		return true;
	}

	public void tick(MovementContext context) {}

	public void startMoving(MovementContext context) {}

	public void visitNewPosition(MovementContext context, BlockPos pos) {}

	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.ZERO;
	}

	public void dropItem(MovementContext context, ItemStack stack) {
		ItemStack remainder;
		if (AllConfigs.SERVER.kinetics.moveItemsToStorage.get())
			remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, stack, false);
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

	public void stopMoving(MovementContext context) {

	}

	public void writeExtraData(MovementContext context) {

	}

	public boolean renderAsNormalTileEntity() {
		return false;
	}

	public boolean hasSpecialInstancedRendering() {
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {}

	@OnlyIn(Dist.CLIENT)
	@Nullable
	public ActorInstance createInstance(MaterialManager materialManager, PlacementSimulationWorld simulationWorld, MovementContext context) {
		return null;
	}

	public void onSpeedChanged(MovementContext context, Vec3 oldMotion, Vec3 motion) {
	}
}
