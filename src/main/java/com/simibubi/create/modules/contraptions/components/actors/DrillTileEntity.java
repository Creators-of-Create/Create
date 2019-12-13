package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.AllTileEntities;

import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

public class DrillTileEntity extends BlockBreakingKineticTileEntity {

	public static DamageSource damageSourceDrill = new DamageSource("create.drill").setDamageBypassesArmor();

	public DrillTileEntity() {
		super(AllTileEntities.DRILL.type);
	}

	@Override
	protected BlockPos getBreakingPos() {
		return getPos().offset(getBlockState().get(DrillBlock.FACING));
	}

}
