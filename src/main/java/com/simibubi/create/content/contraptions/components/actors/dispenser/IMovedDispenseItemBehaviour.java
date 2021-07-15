package com.simibubi.create.content.contraptions.components.actors.dispenser;

import java.util.Random;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface IMovedDispenseItemBehaviour {

	static void initSpawneggs() {
		final IMovedDispenseItemBehaviour spawnEggDispenseBehaviour = new MovedDefaultDispenseItemBehaviour() {
			@Override
			protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos,
				Vector3d facing) {
				if (!(itemStack.getItem() instanceof SpawnEggItem))
					return super.dispenseStack(itemStack, context, pos, facing);
				if (context.world instanceof ServerWorld) {
					EntityType<?> entityType = ((SpawnEggItem) itemStack.getItem()).getType(itemStack.getTag());
					Entity spawnedEntity = entityType.spawn((ServerWorld) context.world, itemStack, null,
						pos.offset(facing.x + .7, facing.y + .7, facing.z + .7), SpawnReason.DISPENSER, facing.y < .5,
						false);
					if (spawnedEntity != null)
						spawnedEntity.setDeltaMovement(context.motion.scale(2));
				}
				itemStack.shrink(1);
				return itemStack;
			}
		};

		for (SpawnEggItem spawneggitem : SpawnEggItem.eggs())
			DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(spawneggitem, spawnEggDispenseBehaviour);
	}

	static void init() {
		MovedProjectileDispenserBehaviour movedPotionDispenseItemBehaviour = new MovedProjectileDispenserBehaviour() {
			@Override
			protected ProjectileEntity getProjectileEntity(World world, double x, double y, double z,
				ItemStack itemStack) {
				return Util.make(new PotionEntity(world, x, y, z), (p_218411_1_) -> p_218411_1_.setItem(itemStack));
			}

			protected float getProjectileInaccuracy() {
				return super.getProjectileInaccuracy() * 0.5F;
			}

			protected float getProjectileVelocity() {
				return super.getProjectileVelocity() * .5F;
			}
		};

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.SPLASH_POTION,
			movedPotionDispenseItemBehaviour);
		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.LINGERING_POTION,
			movedPotionDispenseItemBehaviour);

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.TNT,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos,
					Vector3d facing) {
					double x = pos.getX() + facing.x * .7 + .5;
					double y = pos.getY() + facing.y * .7 + .5;
					double z = pos.getZ() + facing.z * .7 + .5;
					TNTEntity tntentity = new TNTEntity(context.world, x, y, z, null);
					tntentity.push(context.motion.x, context.motion.y, context.motion.z);
					context.world.addFreshEntity(tntentity);
					context.world.playSound(null, tntentity.getX(), tntentity.getY(), tntentity.getZ(),
						SoundEvents.TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
					itemStack.shrink(1);
					return itemStack;
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.FIREWORK_ROCKET,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos,
					Vector3d facing) {
					double x = pos.getX() + facing.x * .7 + .5;
					double y = pos.getY() + facing.y * .7 + .5;
					double z = pos.getZ() + facing.z * .7 + .5;
					FireworkRocketEntity fireworkrocketentity =
						new FireworkRocketEntity(context.world, itemStack, x, y, z, true);
					fireworkrocketentity.shoot(facing.x, facing.y, facing.z, 0.5F, 1.0F);
					context.world.addFreshEntity(fireworkrocketentity);
					itemStack.shrink(1);
					return itemStack;
				}

				@Override
				protected void playDispenseSound(IWorld world, BlockPos pos) {
					world.levelEvent(1004, pos, 0);
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.FIRE_CHARGE,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected void playDispenseSound(IWorld world, BlockPos pos) {
					world.levelEvent(1018, pos, 0);
				}

				@Override
				protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos,
					Vector3d facing) {
					Random random = context.world.random;
					double x = pos.getX() + facing.x * .7 + .5;
					double y = pos.getY() + facing.y * .7 + .5;
					double z = pos.getZ() + facing.z * .7 + .5;
					context.world.addFreshEntity(Util.make(
						new SmallFireballEntity(context.world, x, y, z,
							random.nextGaussian() * 0.05D + facing.x + context.motion.x,
							random.nextGaussian() * 0.05D + facing.y + context.motion.y,
							random.nextGaussian() * 0.05D + facing.z + context.motion.z),
						(p_229425_1_) -> p_229425_1_.setItem(itemStack)));
					itemStack.shrink(1);
					return itemStack;
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.GLASS_BOTTLE,
			new MovedOptionalDispenseBehaviour() {
				@Override
				protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos,
					Vector3d facing) {
					this.successful = false;
					BlockPos interactAt = pos.relative(getClosestFacingDirection(facing));
					BlockState state = context.world.getBlockState(interactAt);
					Block block = state.getBlock();

					if (block.is(BlockTags.BEEHIVES) && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) { 
						((BeehiveBlock) block).releaseBeesAndResetHoneyLevel(context.world, state, interactAt, null,
							BeehiveTileEntity.State.BEE_RELEASED);
						this.successful = true;
						return placeItemInInventory(itemStack, new ItemStack(Items.HONEY_BOTTLE), context, pos,
							facing);
					} else if (context.world.getFluidState(interactAt)
						.is(FluidTags.WATER)) {
						this.successful = true;
						return placeItemInInventory(itemStack,
							PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), context, pos,
							facing);
					} else {
						return super.dispenseStack(itemStack, context, pos, facing);
					}
				}
			});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.BUCKET,
			new MovedDefaultDispenseItemBehaviour() {
				@Override
				protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos,
					Vector3d facing) {
					BlockPos interactAt = pos.relative(getClosestFacingDirection(facing));
					BlockState state = context.world.getBlockState(interactAt);
					Block block = state.getBlock();
					if (block instanceof IBucketPickupHandler) {
						Fluid fluid = ((IBucketPickupHandler) block).takeLiquid(context.world, interactAt, state);
						if (fluid instanceof FlowingFluid)
							return placeItemInInventory(itemStack, new ItemStack(fluid.getBucket()), context, pos,
								facing);
					}
					return super.dispenseStack(itemStack, context, pos, facing);
				}
			});
	}

	ItemStack dispense(ItemStack itemStack, MovementContext context, BlockPos pos);
}
