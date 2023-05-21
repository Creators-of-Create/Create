package com.simibubi.create.content.decoration.placard;

import java.util.List;

import com.mojang.math.Vector3f;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PlacardBlockEntity extends SmartBlockEntity {

	ItemStack heldItem;
	int poweredTicks;

	public PlacardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		heldItem = ItemStack.EMPTY;
		poweredTicks = 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (level.isClientSide)
			return;
		if (poweredTicks == 0)
			return;

		poweredTicks--;
		if (poweredTicks > 0)
			return;

		BlockState blockState = getBlockState();
		level.setBlock(worldPosition, blockState.setValue(PlacardBlock.POWERED, false), 3);
		PlacardBlock.updateNeighbours(blockState, level, worldPosition);
	}

	public ItemStack getHeldItem() {
		return heldItem;
	}

	public void setHeldItem(ItemStack heldItem) {
		this.heldItem = heldItem;
		notifyUpdate();
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		tag.putInt("PoweredTicks", poweredTicks);
		tag.put("Item", heldItem.serializeNBT());
		super.write(tag, clientPacket);
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		int prevTicks = poweredTicks;
		poweredTicks = tag.getInt("PoweredTicks");
		heldItem = ItemStack.of(tag.getCompound("Item"));
		super.read(tag, clientPacket);

		if (clientPacket && prevTicks < poweredTicks)
			spawnParticles();
	}

	private void spawnParticles() {
		BlockState blockState = getBlockState();
		if (!AllBlocks.PLACARD.has(blockState))
			return;

		DustParticleOptions pParticleData = new DustParticleOptions(new Vector3f(1, .2f, 0), 1);
		Vec3 centerOf = VecHelper.getCenterOf(worldPosition);
		Vec3 normal = Vec3.atLowerCornerOf(PlacardBlock.connectedDirection(blockState)
			.getNormal());
		Vec3 offset = VecHelper.axisAlingedPlaneOf(normal);

		for (int i = 0; i < 10; i++) {
			Vec3 v = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .5f)
				.multiply(offset)
				.normalize()
				.scale(.45f)
				.add(normal.scale(-.45f))
				.add(centerOf);
			level.addParticle(pParticleData, v.x, v.y, v.z, 0, 0, 0);
		}
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

}
