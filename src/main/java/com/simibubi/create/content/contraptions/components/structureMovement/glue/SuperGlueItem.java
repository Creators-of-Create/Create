package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.simibubi.create.lib.item.CustomMaxCountItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class SuperGlueItem extends Item implements CustomMaxCountItem {

	public SuperGlueItem(Properties properties) {
		super(properties.durability(99));
	}

	@Override
	public boolean canBeDepleted() {
		return true;
	}

//	done in item properties instead
//	@Override
//	public int getMaxDamage(ItemStack stack) {
//		return 99;
//	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos blockpos = context.getClickedPos();
		Direction direction = context.getClickedFace();
		BlockPos blockpos1 = blockpos.relative(direction);
		Player playerentity = context.getPlayer();
		ItemStack itemstack = context.getItemInHand();

		if (playerentity == null || !this.canPlace(playerentity, direction, itemstack, blockpos1))
			return InteractionResult.FAIL;

		Level world = context.getLevel();
		SuperGlueEntity entity = new SuperGlueEntity(world, blockpos1, direction);
		CompoundTag compoundnbt = itemstack.getTag();
		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, playerentity, entity, compoundnbt);

		if (!entity.onValidSurface())
			return InteractionResult.FAIL;

		if (!world.isClientSide) {
			entity.playPlaceSound();
			world.addFreshEntity(entity);
		}
		itemstack.hurtAndBreak(1, playerentity, SuperGlueItem::onBroken);

		return InteractionResult.SUCCESS;
	}

	public static void onBroken(Player player) {

	}

	protected boolean canPlace(Player entity, Direction facing, ItemStack stack, BlockPos pos) {
		return !entity.level.isOutsideBuildHeight(pos) && entity.mayUseItemAt(pos, facing, stack);
	}

	@Environment(EnvType.CLIENT)
	public static void spawnParticles(Level world, BlockPos pos, Direction direction, boolean fullBlock) {
		Vec3 vec = Vec3.atLowerCornerOf(direction.getNormal());
		Vec3 plane = VecHelper.axisAlingedPlaneOf(vec);
		Vec3 facePos = VecHelper.getCenterOf(pos)
			.add(vec.scale(.5f));

		float distance = fullBlock ? 1f : .25f + .25f * (world.random.nextFloat() - .5f);
		plane = plane.scale(distance);
		ItemStack stack = new ItemStack(Items.SLIME_BALL);

		for (int i = fullBlock ? 40 : 15; i > 0; i--) {
			Vec3 offset = VecHelper.rotate(plane, 360 * world.random.nextFloat(), direction.getAxis());
			Vec3 motion = offset.normalize()
				.scale(1 / 16f);
			if (fullBlock)
				offset = new Vec3(Mth.clamp(offset.x, -.5, .5), Mth.clamp(offset.y, -.5, .5),
					Mth.clamp(offset.z, -.5, .5));
			Vec3 particlePos = facePos.add(offset);
			world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), particlePos.x, particlePos.y,
				particlePos.z, motion.x, motion.y, motion.z);
		}

	}

}
