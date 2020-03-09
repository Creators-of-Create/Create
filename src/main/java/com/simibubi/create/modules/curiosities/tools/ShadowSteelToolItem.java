package com.simibubi.create.modules.curiosities.tools;

import java.util.List;

import com.simibubi.create.foundation.item.AbstractToolItem;
import com.simibubi.create.foundation.item.AllToolTypes;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;

public class ShadowSteelToolItem extends AbstractToolItem {

	public ShadowSteelToolItem(float attackDamageIn, float attackSpeedIn, Properties builder, AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, AllToolTiers.SHADOW_STEEL, builder, types);
	}

	@Override
	public boolean modifiesDrops() {
		return true;
	}

	@Override
	public void modifyDrops(List<ItemStack> drops, IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
		drops.clear();
	}

	@Override
	public void spawnParticles(IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
		if (!canHarvestBlock(tool, state))
			return;
		Vec3d smokePos = VecHelper.offsetRandomly(VecHelper.getCenterOf(pos), world.getRandom(), .15f);
		world.addParticle(ParticleTypes.SMOKE, smokePos.getX(), smokePos.getY(), smokePos.getZ(), 0, .01f, 0);
	}

}
