package com.simibubi.create.content.processing.basin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;

public class BasinMovementBehaviour implements MovementBehaviour {
	@OnlyIn(Dist.CLIENT)
	private Map<Long, BasinBlockEntity> BASINS = new HashMap<>();

	public Map<String, ItemStackHandler> getOrReadInventory(MovementContext context) {
		Map<String, ItemStackHandler> map = new HashMap<>();
		map.put("InputItems", new ItemStackHandler(9));
		map.put("OutputItems", new ItemStackHandler(8));
		map.forEach((s, h) -> h.deserializeNBT(context.blockEntityData.getCompound(s)));
		return map;
	}

	@Override
	public void tick(MovementContext context) {
		MovementBehaviour.super.tick(context);
		if (context.temporaryData == null || (boolean) context.temporaryData) {
			Vec3 facingVec = context.rotation.apply(Vec3.atLowerCornerOf(Direction.UP.getNormal()));
			facingVec.normalize();
			if (Direction.getNearest(facingVec.x, facingVec.y, facingVec.z) == Direction.DOWN)
				dump(context, facingVec);
		}
	}

	private void dump(MovementContext context, Vec3 facingVec) {
		getOrReadInventory(context).forEach((key, itemStackHandler) -> {
			for (int i = 0; i < itemStackHandler.getSlots(); i++) {
				if (itemStackHandler.getStackInSlot(i)
					.isEmpty())
					continue;
				ItemEntity itemEntity = new ItemEntity(context.world, context.position.x, context.position.y,
					context.position.z, itemStackHandler.getStackInSlot(i));
				itemEntity.setDeltaMovement(facingVec.scale(.05));
				context.world.addFreshEntity(itemEntity);
				itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
			}
			context.blockEntityData.put(key, itemStackHandler.serializeNBT());
		});
		BlockEntity blockEntity = context.contraption.presentBlockEntities.get(context.localPos);
		if (blockEntity instanceof BasinBlockEntity)
			((BasinBlockEntity) blockEntity).readOnlyItems(context.blockEntityData);
		context.temporaryData = false; // did already dump, so can't any more
	}

	@Override
	public boolean disableBlockEntityRendering() {
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	private BasinBlockEntity getBasinBlockEntity(Long l, MovementContext context, VirtualRenderWorld renderWorld) {
		return BASINS.computeIfAbsent(l, u -> {
			BasinBlockEntity basin = new BasinBlockEntity(AllBlockEntityTypes.BASIN.get(), context.localPos, context.state);
			basin.setLevel(renderWorld);
			return basin;
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
		BasinBlockEntity basin = getBasinBlockEntity(context.localPos.asLong(), context, renderWorld);
		basin.read(context.blockEntityData, true);
		basin.tick();

		BlockEntityRenderHelper.renderBlockEntities(context.world, renderWorld, List.of(basin),
			matrices.getModelViewProjection(), matrices.getLight(), buffer);
	}
}
