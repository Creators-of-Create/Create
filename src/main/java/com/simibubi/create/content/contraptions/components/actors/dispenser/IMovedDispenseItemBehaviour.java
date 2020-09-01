package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Random;

public interface IMovedDispenseItemBehaviour {
	static void init() {
		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.ARROW, new MovedProjectileDispenserBehaviour() {
			@Override
			protected IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack) {
				ArrowEntity arrowEntity = new ArrowEntity(world, x, y, z);
				arrowEntity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
				return arrowEntity;
			}
		});


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.TIPPED_ARROW, new MovedProjectileDispenserBehaviour() {
			@Override
			protected IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack) {
				ArrowEntity arrowEntity = new ArrowEntity(world, x, y, z);
				arrowEntity.setPotionEffect(itemStack);
				arrowEntity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
				return arrowEntity;
			}
		});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.SPECTRAL_ARROW, new MovedProjectileDispenserBehaviour() {
			@Override
			protected IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack) {
				AbstractArrowEntity arrowEntity = new SpectralArrowEntity(world, x, y, z);
				arrowEntity.pickupStatus = AbstractArrowEntity.PickupStatus.ALLOWED;
				return arrowEntity;
			}
		});


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.EGG, new MovedProjectileDispenserBehaviour() {
			@Override
			protected IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack) {
				return Util.make(new EggEntity(world, x, y, z), p_218408_1_ -> p_218408_1_.setItem(itemStack));
			}
		});


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.SNOWBALL, new MovedProjectileDispenserBehaviour() {
			@Override
			protected IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack) {
				return Util.make(new SnowballEntity(world, x, y, z), p_218409_1_ -> p_218409_1_.setItem(itemStack));
			}
		});


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.EXPERIENCE_BOTTLE, new MovedProjectileDispenserBehaviour() {
			@Override
			protected IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack) {
				return Util.make(new ExperienceBottleEntity(world, x, y, z), p_218409_1_ -> p_218409_1_.setItem(itemStack));
			}

			@Override
			protected float getProjectileInaccuracy() {
				return super.getProjectileInaccuracy() * 0.5F;
			}

			@Override
			protected float getProjectileVelocity() {
				return super.getProjectileVelocity() * 1.25F;
			}
		});


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.TNT, new MovedDefaultDispenseItemBehaviour() {
			@Override
			protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
				double x = pos.getX() + facing.x * .7 + .5;
				double y = pos.getY() + facing.y * .7 + .5;
				double z = pos.getZ() + facing.z * .7 + .5;
				TNTEntity tntentity = new TNTEntity(context.world, x, y, z, null);
				tntentity.addVelocity(context.motion.x, context.motion.y, context.motion.z);
				context.world.addEntity(tntentity);
				context.world.playSound(null, tntentity.getX(), tntentity.getY(), tntentity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
				itemStack.shrink(1);
				return itemStack;
			}
		});


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.FIREWORK_ROCKET, new MovedDefaultDispenseItemBehaviour() {
			@Override
			protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
				double x = pos.getX() + facing.x * .7 + .5;
				double y = pos.getY() + facing.y * .7 + .5;
				double z = pos.getZ() + facing.z * .7 + .5;
				FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(context.world, itemStack, x, y, z, true);
				fireworkrocketentity.shoot(facing.x, facing.y, facing.z, 0.5F, 1.0F);
				context.world.addEntity(fireworkrocketentity);
				itemStack.shrink(1);
				return itemStack;
			}

			@Override
			protected void playDispenseSound(IWorld world, BlockPos pos) {
				world.playEvent(1004, pos, 0);
			}
		});


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.FIRE_CHARGE, new MovedDefaultDispenseItemBehaviour() {
			@Override
			protected void playDispenseSound(IWorld world, BlockPos pos) {
				world.playEvent(1018, pos, 0);
			}

			@Override
			protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
				Random random = context.world.rand;
				double x = pos.getX() + facing.x * .7 + .5;
				double y = pos.getY() + facing.y * .7 + .5;
				double z = pos.getZ() + facing.z * .7 + .5;
				context.world.addEntity(Util.make(new SmallFireballEntity(context.world, x, y, z,
					random.nextGaussian() * 0.05D + facing.x + context.motion.x, random.nextGaussian() * 0.05D + facing.y + context.motion.y, random.nextGaussian() * 0.05D + facing.z + context.motion.z), (p_229425_1_) -> p_229425_1_.setStack(itemStack)));
				itemStack.shrink(1);
				return itemStack;
			}
		});


	}

	ItemStack dispense(ItemStack itemStack, MovementContext context, BlockPos pos);
}
