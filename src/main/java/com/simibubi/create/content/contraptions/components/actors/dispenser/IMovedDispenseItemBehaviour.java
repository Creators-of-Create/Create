package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.projectile.*;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

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


		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.GLASS_BOTTLE, new MovedOptionalDispenseBehaviour() {
			@Override
			protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
				this.successful = false;
				BlockPos interactAt = pos.offset(getClosestFacingDirection(facing));
				BlockState state = context.world.getBlockState(interactAt);
				Block block = state.getBlock();

				if (block.isIn(BlockTags.field_226151_aa_) && state.get(BeehiveBlock.HONEY_LEVEL) >= 5) { // Beehive -> honey bottles
					((BeehiveBlock) block).takeHoney(context.world, state, interactAt, null, BeehiveTileEntity.State.BEE_RELEASED);
					this.successful = true;
					return placeItemInInventory(itemStack, new ItemStack(Items.field_226638_pX_), context, pos, facing);
				} else if (context.world.getFluidState(interactAt).isTagged(FluidTags.WATER)) {
					this.successful = true;
					return placeItemInInventory(itemStack, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER), context, pos, facing);
				} else {
					return super.dispenseStack(itemStack, context, pos, facing);
				}
			}

			private ItemStack placeItemInInventory(ItemStack bottles, ItemStack output, MovementContext context, BlockPos pos, Vec3d facing) {
				bottles.shrink(1);
				ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, output.copy(), false);
				if (!remainder.isEmpty())
					super.dispenseStack(output, context, pos, facing);
				return bottles;
			}
		});

		DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(Items.BUCKET, new MovedDefaultDispenseItemBehaviour() {
			@Override
			protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
				BlockPos interactAt = pos.offset(getClosestFacingDirection(facing));
				BlockState state = context.world.getBlockState(interactAt);
				Block block = state.getBlock();
				if (block instanceof IBucketPickupHandler) {
					Fluid fluid = ((IBucketPickupHandler) block).pickupFluid(context.world, interactAt, state);
					if (fluid instanceof FlowingFluid)
						return placeItemInInventory(itemStack, new ItemStack(fluid.getFilledBucket()), context, pos, facing);
				}
				return super.dispenseStack(itemStack, context, pos, facing);
			}

			private ItemStack placeItemInInventory(ItemStack buckets, ItemStack output, MovementContext context, BlockPos pos, Vec3d facing) {
				buckets.shrink(1);
				ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, output.copy(), false);
				if (!remainder.isEmpty())
					super.dispenseStack(output, context, pos, facing);
				return buckets;
			}
		});

		final IMovedDispenseItemBehaviour spawnEggDispenseBehaviour = new MovedDefaultDispenseItemBehaviour() {
			@Override
			protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
				if (!(itemStack.getItem() instanceof SpawnEggItem))
					return super.dispenseStack(itemStack, context, pos, facing);
				EntityType<?> entityType = ((SpawnEggItem) itemStack.getItem()).getType(itemStack.getTag());
				Entity spawnedEntity = entityType.spawn(context.world, itemStack, null, pos.add(facing.x + .7, facing.y + .7, facing.z + .7), SpawnReason.DISPENSER, facing.y < .5, false);
				if (spawnedEntity != null)
					spawnedEntity.setMotion(context.motion.scale(2));
				itemStack.shrink(1);
				return itemStack;
			}
		};

		for (SpawnEggItem spawneggitem : SpawnEggItem.getEggs()) {
			DispenserMovementBehaviour.registerMovedDispenseItemBehaviour(spawneggitem, spawnEggDispenseBehaviour);
		}
	}

	ItemStack dispense(ItemStack itemStack, MovementContext context, BlockPos pos);
}
