package com.simibubi.create.content.contraptions.components.actors;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

public class DrillTileEntity extends BlockBreakingKineticTileEntity {

	public static DamageSource damageSourceDrill = new DamageSource("create.drill").setDamageBypassesArmor();

	public DrillTileEntity(TileEntityType<? extends DrillTileEntity> type) {
		super(type);
	}

	@Override
	protected BlockPos getBreakingPos() {
		return getPos().offset(getBlockState().get(DrillBlock.FACING));
	}

}
