package com.simibubi.create.content.contraptions.components.crusher;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.lib.helper.DamageSourceHelper;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CrushingWheelTileEntity extends KineticTileEntity {

	public static DamageSource damageSource = DamageSourceHelper.create$createArmorBypassingDamageSource("create.crush")
			.setScalesWithDifficulty();

	public CrushingWheelTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(20);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		fixControllers();
	}

	public void fixControllers() {
		for (Direction d : Iterate.directions)
			((CrushingWheelBlock) getBlockState().getBlock()).updateControllers(getBlockState(), getLevel(), getBlockPos(),
					d);
	}

	@Override
	public AABB makeRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		fixControllers();
	}

	public static int crushingIsFortunate(DamageSource source) {
		if (source != damageSource)
			return 0;
		return 2;		//This does not currently increase mob drops. It seems like this only works for damage done by an entity.
	}

	public static boolean handleCrushedMobDrops(DamageSource source, Collection<ItemEntity> drops) {
		if (source != CrushingWheelTileEntity.damageSource)
			return false;
		Vec3 outSpeed = Vec3.ZERO;
		for (ItemEntity outputItem : drops) {
			outputItem.setDeltaMovement(outSpeed);
		}
		return false;
	}

}
