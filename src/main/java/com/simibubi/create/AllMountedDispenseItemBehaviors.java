package com.simibubi.create;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.simibubi.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.simibubi.create.api.contraption.dispenser.MountedProjectileDispenseBehavior;
import com.simibubi.create.api.contraption.dispenser.OptionalMountedDispenseBehavior;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AllMountedDispenseItemBehaviors {
	private static final MountedDispenseBehavior SPAWN_EGG = new DefaultMountedDispenseBehavior() {
		@Override
		protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
			if (!(stack.getItem() instanceof SpawnEggItem egg))
				return super.execute(stack, context, pos, facing);

			if (context.world instanceof ServerLevel serverLevel) {
				EntityType<?> type = egg.getType(stack);
				BlockPos offset = BlockPos.containing(facing.x + .7, facing.y + .7, facing.z + .7);
				Entity entity = type.spawn(serverLevel, stack, null, pos.offset(offset), MobSpawnType.DISPENSER, facing.y < .5, false);
				if (entity != null) {
					entity.setDeltaMovement(context.motion.scale(2));
				}
			}

			stack.shrink(1);
			return stack;
		}
	};
	private static final MountedDispenseBehavior TNT = new DefaultMountedDispenseBehavior() {
		@Override
		protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
			double x = pos.getX() + facing.x * .7 + .5;
			double y = pos.getY() + facing.y * .7 + .5;
			double z = pos.getZ() + facing.z * .7 + .5;
			PrimedTnt tnt = new PrimedTnt(context.world, x, y, z, null);
			tnt.push(context.motion.x, context.motion.y, context.motion.z);
			context.world.addFreshEntity(tnt);
			context.world.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1, 1);
			stack.shrink(1);
			return stack;
		}
	};
	private static final MountedDispenseBehavior FIREWORK = new DefaultMountedDispenseBehavior() {
		@Override
		protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
			double x = pos.getX() + facing.x * .7 + .5;
			double y = pos.getY() + facing.y * .7 + .5;
			double z = pos.getZ() + facing.z * .7 + .5;
			FireworkRocketEntity firework = new FireworkRocketEntity(context.world, stack, x, y, z, true);
			firework.shoot(facing.x, facing.y, facing.z, 0.5F, 1.0F);
			context.world.addFreshEntity(firework);
			stack.shrink(1);
			return stack;
		}

		@Override
		protected void playSound(LevelAccessor level, BlockPos pos) {
			level.levelEvent(LevelEvent.SOUND_FIREWORK_SHOOT, pos, 0);
		}
	};
	private static final MountedDispenseBehavior FIRE_CHARGE = new DefaultMountedDispenseBehavior() {
		@Override
		protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
			RandomSource random = context.world.random;
			double x = pos.getX() + facing.x * .7 + .5;
			double y = pos.getY() + facing.y * .7 + .5;
			double z = pos.getZ() + facing.z * .7 + .5;
			SmallFireball fireball = new SmallFireball(
				context.world,
				x, y, z,
				new Vec3(random.nextGaussian() * 0.05 + facing.x + context.motion.x,
				random.nextGaussian() * 0.05 + facing.y + context.motion.y,
				random.nextGaussian() * 0.05 + facing.z + context.motion.z).normalize()
			);
			fireball.setItem(stack); // copies the stack
			context.world.addFreshEntity(fireball);
			stack.shrink(1);
			return stack;
		}

		@Override
		protected void playSound(LevelAccessor level, BlockPos pos) {
			level.levelEvent(LevelEvent.SOUND_BLAZE_FIREBALL, pos, 0);
		}
	};
	private static final MountedDispenseBehavior BUCKET = new DefaultMountedDispenseBehavior() {
		@Override
		protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
			BlockPos interactionPos = pos.relative(MountedDispenseBehavior.getClosestFacingDirection(facing));
			BlockState state = context.world.getBlockState(interactionPos);
			if (!(state.getBlock() instanceof BucketPickup bucketPickup)) {
				return super.execute(stack, context, pos, facing);
			}

			ItemStack bucket = bucketPickup.pickupBlock(null, context.world, interactionPos, state);
			MountedDispenseBehavior.placeItemInInventory(bucket, context, pos);
			stack.shrink(1);
			return stack;
		}
	};
	private static final MountedDispenseBehavior POTIONS = new MountedProjectileDispenseBehavior() {
		@Override
		protected Projectile getProjectile(Level level, double x, double y, double z, ItemStack stack, Direction facing) {
			ThrownPotion potion = new ThrownPotion(level, x, y, z);
			potion.setItem(stack); // copies item
			return potion;
		}

		@Override
		protected float getUncertainty() {
			return super.getUncertainty() * 0.5f;
		}

		@Override
		protected float getPower() {
			return super.getPower() * 1.25f;
		}
	};
	private static final MountedDispenseBehavior BOTTLE = new OptionalMountedDispenseBehavior() {
		@Override
		@Nullable
		protected ItemStack doExecute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
			BlockPos interactionPos = pos.relative(MountedDispenseBehavior.getClosestFacingDirection(facing));
			BlockState state = context.world.getBlockState(interactionPos);
			Block block = state.getBlock();

			if (block instanceof BeehiveBlock hive && state.is(BlockTags.BEEHIVES) && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
				hive.releaseBeesAndResetHoneyLevel(context.world, state, interactionPos, null, BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED);
				MountedDispenseBehavior.placeItemInInventory(new ItemStack(Items.HONEY_BOTTLE), context, pos);
				stack.shrink(1);
				return stack;
			} else if (context.world.getFluidState(interactionPos).is(FluidTags.WATER)) {
				ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
				MountedDispenseBehavior.placeItemInInventory(waterBottle, context, pos);
				stack.shrink(1);
				return stack;
			} else {
				return null;
			}
		}
	};

	public static void registerDefaults() {
		MountedDispenseBehavior.REGISTRY.registerProvider(item -> item instanceof SpawnEggItem ? SPAWN_EGG : null);

		MountedDispenseBehavior.REGISTRY.register(Items.TNT, TNT);
		MountedDispenseBehavior.REGISTRY.register(Items.FIREWORK_ROCKET, FIREWORK);
		MountedDispenseBehavior.REGISTRY.register(Items.FIRE_CHARGE, FIRE_CHARGE);
		MountedDispenseBehavior.REGISTRY.register(Items.BUCKET, BUCKET);
		MountedDispenseBehavior.REGISTRY.register(Items.GLASS_BOTTLE, BOTTLE);

		// potions can't be automatically converted since they use a weird wrapper thing
		MountedDispenseBehavior.REGISTRY.register(Items.SPLASH_POTION, POTIONS);
		MountedDispenseBehavior.REGISTRY.register(Items.LINGERING_POTION, POTIONS);
	}
}
