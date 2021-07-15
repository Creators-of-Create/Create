package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.item.Item.Properties;

public class SuperGlueItem extends Item {

	public SuperGlueItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canBeDepleted() {
		return true;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return 99;
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public ActionResultType useOn(ItemUseContext context) {
		BlockPos blockpos = context.getClickedPos();
		Direction direction = context.getClickedFace();
		BlockPos blockpos1 = blockpos.relative(direction);
		PlayerEntity playerentity = context.getPlayer();
		ItemStack itemstack = context.getItemInHand();

		if (playerentity == null || !this.canPlace(playerentity, direction, itemstack, blockpos1))
			return ActionResultType.FAIL;

		World world = context.getLevel();
		SuperGlueEntity entity = new SuperGlueEntity(world, blockpos1, direction);
		CompoundNBT compoundnbt = itemstack.getTag();
		if (compoundnbt != null)
			EntityType.updateCustomEntityTag(world, playerentity, entity, compoundnbt);

		if (!entity.onValidSurface())
			return ActionResultType.FAIL;

		if (!world.isClientSide) {
			entity.playPlaceSound();
			world.addFreshEntity(entity);
		}
		itemstack.hurtAndBreak(1, playerentity, SuperGlueItem::onBroken);

		return ActionResultType.SUCCESS;
	}

	public static void onBroken(PlayerEntity player) {

	}

	protected boolean canPlace(PlayerEntity entity, Direction facing, ItemStack stack, BlockPos pos) {
		return !World.isOutsideBuildHeight(pos) && entity.mayUseItemAt(pos, facing, stack);
	}

	@OnlyIn(Dist.CLIENT)
	public static void spawnParticles(World world, BlockPos pos, Direction direction, boolean fullBlock) {
		Vector3d vec = Vector3d.atLowerCornerOf(direction.getNormal());
		Vector3d plane = VecHelper.axisAlingedPlaneOf(vec);
		Vector3d facePos = VecHelper.getCenterOf(pos)
			.add(vec.scale(.5f));

		float distance = fullBlock ? 1f : .25f + .25f * (world.random.nextFloat() - .5f);
		plane = plane.scale(distance);
		ItemStack stack = new ItemStack(Items.SLIME_BALL);

		for (int i = fullBlock ? 40 : 15; i > 0; i--) {
			Vector3d offset = VecHelper.rotate(plane, 360 * world.random.nextFloat(), direction.getAxis());
			Vector3d motion = offset.normalize()
				.scale(1 / 16f);
			if (fullBlock)
				offset = new Vector3d(MathHelper.clamp(offset.x, -.5, .5), MathHelper.clamp(offset.y, -.5, .5),
					MathHelper.clamp(offset.z, -.5, .5));
			Vector3d particlePos = facePos.add(offset);
			world.addParticle(new ItemParticleData(ParticleTypes.ITEM, stack), particlePos.x, particlePos.y,
				particlePos.z, motion.x, motion.y, motion.z);
		}

	}

}
