package com.simibubi.create.content.curiosities.weapons;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.registries.IRegistryDelegate;

public class BuiltinPotatoProjectileTypes {

	private static final GameProfile ZOMBIE_CONVERTER_NAME =
			new GameProfile(UUID.fromString("be12d3dc-27d3-4992-8c97-66be53fd49c5"), "Converter");
	private static final WorldAttached<FakePlayer> ZOMBIE_CONVERTERS =
			new WorldAttached<>(w -> new FakePlayer((ServerLevel) w, ZOMBIE_CONVERTER_NAME));

	public static final PotatoCannonProjectileType

			FALLBACK = create("fallback").damage(0)
			.register(),

	WHEAT_SEED = create("wheat_seeds").damage(2)
			.reloadTicks(20)
			.numberOfShot(4)
			.cost(1)
			.velocity(1.25f)
			.accuracy(50.0f)
			.sprayAccuracy(30.0f)
			.knockback(0.25f)
			.sprayInto(3)
			.onBlockHit(plantCrop(Blocks.WHEAT.delegate))
			.registerAndAssign(Items.WHEAT_SEEDS),

	BEETROOT_SEEDS = create("beetroot_seeds").damage(3)
			.reloadTicks(15)
			.numberOfShot(2)
			.cost(1)
			.velocity(1.0f)
			.accuracy(100.0f)
			.sprayAccuracy(20.0f)
			.knockback(0.30f)
			.sprayInto(2)
			.onBlockHit(plantCrop(Blocks.BEETROOTS.delegate))
			.registerAndAssign(Items.BEETROOT_SEEDS),

	MELON_SEEDS = create("melon_seeds").damage(3)
			.reloadTicks(5)
			.numberOfShot(16)
			.cost(1)
			.velocity(1.75f)
			.accuracy(0.0f)
			.knockback(0.15f)
			.onBlockHit(plantCrop(Blocks.MELON_STEM.delegate))
			.registerAndAssign(Items.MELON_SEEDS),

	PUMPKIN_SEEDS = create("pumpkin_seeds").damage(4)
			.reloadTicks(10)
			.numberOfShot(8)
			.cost(1)
			.velocity(1.25f)
			.accuracy(0.0f)
			.sprayAccuracy(0.0f)
			.knockback(0.40f)
			.sprayInto(3)
			.onBlockHit(plantCrop(Blocks.PUMPKIN_STEM.delegate))
			.registerAndAssign(Items.PUMPKIN_SEEDS),

	POTATO = create("potato").damage(5)
			.reloadTicks(15)
			.cost(1)
			.velocity(1.25f)
			.accuracy(60.0f)
			.knockback(1.5f)
			.renderTumbling()
			.onBlockHit(plantCrop(Blocks.POTATOES.delegate))
			.registerAndAssign(Items.POTATO),

	BAKED_POTATO = create("baked_potato").damage(5)
			.reloadTicks(15)
			.cost(1)
			.velocity(1.25f)
			.accuracy(65.0f)
			.knockback(0.5f)
			.renderTumbling()
			.preEntityHit(setFire(3))
			.registerAndAssign(Items.BAKED_POTATO),

	CARROT = create("carrot").damage(4)
			.reloadTicks(12)
			.cost(1)
			.piercing(1)
			.velocity(1.45f)
			.knockback(0.3f)
			.accuracy(90.0f)
			.renderTowardMotion(140, 1)
			.soundPitch(1.5f)
			.onBlockHit(plantCrop(Blocks.CARROTS.delegate))
			.registerAndAssign(Items.CARROT),

	GOLDEN_CARROT = create("golden_carrot").damage(12)
			.reloadTicks(15)
			.cost(1)
			.piercing(2)
			.velocity(1.45f)
			.knockback(0.5f)
			.accuracy(100.0f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.5f)
			.registerAndAssign(Items.GOLDEN_CARROT),

	SWEET_BERRIES = create("sweet_berry").damage(3)
			.reloadTicks(10)
			.cost(1)
			.knockback(0.1f)
			.accuracy(70.0f)
			.sprayAccuracy(50.0f)
			.velocity(1.05f)
			.renderTumbling()
			.sprayInto(3)
			.soundPitch(1.25f)
			.registerAndAssign(Items.SWEET_BERRIES),

	GLOW_BERRIES = create("glow_berry").damage(2)
			.reloadTicks(10)
			.cost(1)
			.knockback(0.05f)
			.accuracy(75.0f)
			.sprayAccuracy(25.0f)
			.velocity(1.05f)
			.renderTumbling()
			.sprayInto(2)
			.soundPitch(1.2f)
			.onEntityHit(potion(MobEffects.GLOWING, 1, 200, false))
			.registerAndAssign(Items.GLOW_BERRIES),

	CHOCOLATE_BERRIES = create("chocolate_berry").damage(4)
			.reloadTicks(10)
			.cost(1)
			.knockback(0.2f)
			.accuracy(80.0f)
			.sprayAccuracy(60.0f)
			.velocity(1.05f)
			.renderTumbling()
			.sprayInto(3)
			.soundPitch(1.25f)
			.registerAndAssign(AllItems.CHOCOLATE_BERRIES.get()),

	POISON_POTATO = create("poison_potato").damage(5)
			.reloadTicks(15)
			.cost(1)
			.knockback(0.05f)
			.accuracy(75.0f)
			.velocity(1.25f)
			.renderTumbling()
			.onEntityHit(potion(MobEffects.POISON, 1, 160, true))
			.registerAndAssign(Items.POISONOUS_POTATO),

	CHORUS_FRUIT = create("chorus_fruit").damage(3)
			.reloadTicks(15)
			.cost(1)
			.velocity(1.20f)
			.accuracy(100.0f)
			.knockback(0.05f)
			.renderTumbling()
			.onEntityHit(chorusTeleport(20))
			.registerAndAssign(Items.CHORUS_FRUIT),

	APPLE = create("apple").damage(5)
			.reloadTicks(10)
			.cost(1)
			.velocity(1.45f)
			.knockback(0.5f)
			.accuracy(80.0f)
			.renderTumbling()
			.soundPitch(1.1f)
			.registerAndAssign(Items.APPLE),

	HONEYED_APPLE = create("honeyed_apple").damage(6)
			.reloadTicks(15)
			.cost(1)
			.velocity(1.35f)
			.knockback(0.1f)
			.accuracy(75.0f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(potion(MobEffects.MOVEMENT_SLOWDOWN, 2, 160, true))
			.registerAndAssign(AllItems.HONEYED_APPLE.get()),

	GOLDEN_APPLE = create("golden_apple").damage(1)
			.reloadTicks(100)
			.cost(1)
			.velocity(1.45f)
			.knockback(0.05f)
			.accuracy(100.0f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(ray -> {
				Entity entity = ray.getEntity();
				Level world = entity.level;

				if (!(entity instanceof ZombieVillager)
						|| !((ZombieVillager) entity).hasEffect(MobEffects.WEAKNESS))
					return foodEffects(Foods.GOLDEN_APPLE, false).test(ray);
				if (world.isClientSide)
					return false;

				FakePlayer dummy = ZOMBIE_CONVERTERS.get(world);
				dummy.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_APPLE, 1));
				((ZombieVillager) entity).mobInteract(dummy, InteractionHand.MAIN_HAND);
				return true;
			})
			.registerAndAssign(Items.GOLDEN_APPLE),

	ENCHANTED_GOLDEN_APPLE = create("enchanted_golden_apple").damage(1)
			.reloadTicks(100)
			.cost(1)
			.velocity(1.45f)
			.knockback(0.05f)
			.accuracy(100.0f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(foodEffects(Foods.ENCHANTED_GOLDEN_APPLE, false))
			.registerAndAssign(Items.ENCHANTED_GOLDEN_APPLE),

	BEETROOT = create("beetroot").damage(2)
			.reloadTicks(5)
			.cost(1)
			.velocity(1.6f)
			.knockback(0.1f)
			.accuracy(50.0f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.6f)
			.registerAndAssign(Items.BEETROOT),

	MELON_SLICE = create("melon_slice").damage(3)
			.reloadTicks(8)
			.cost(1)
			.knockback(0.1f)
			.velocity(1.45f)
			.accuracy(40.0f)
			.renderTumbling()
			.soundPitch(1.5f)
			.registerAndAssign(Items.MELON_SLICE),

	GLISTERING_MELON = create("glistering_melon").damage(5)
			.reloadTicks(8)
			.cost(1)
			.knockback(0.1f)
			.velocity(1.45f)
			.accuracy(60.0f)
			.renderTumbling()
			.soundPitch(1.5f)
			.onEntityHit(potion(MobEffects.GLOWING, 1, 100, true))
			.registerAndAssign(Items.GLISTERING_MELON_SLICE),

	MELON_BLOCK = create("melon_block").damage(8)
			.reloadTicks(20)
			.cost(1)
			.knockback(2.0f)
			.velocity(0.95f)
			.accuracy(90.0f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(placeBlockOnGround(Blocks.MELON.delegate))
			.registerAndAssign(Blocks.MELON),

	PUMPKIN_BLOCK = create("pumpkin_block").damage(6)
			.reloadTicks(15)
			.cost(1)
			.knockback(2.0f)
			.velocity(0.95f)
			.accuracy(100.0f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(placeBlockOnGround(Blocks.PUMPKIN.delegate))
			.registerAndAssign(Blocks.PUMPKIN),

	PUMPKIN_PIE = create("pumpkin_pie").damage(7)
			.reloadTicks(15)
			.cost(1)
			.knockback(0.05f)
			.velocity(1.1f)
			.accuracy(80.0f)
			.renderTumbling()
			.sticky()
			.soundPitch(1.1f)
			.registerAndAssign(Items.PUMPKIN_PIE),

	CAKE = create("cake").damage(8)
			.reloadTicks(15)
			.cost(1)
			.knockback(0.1f)
			.accuracy(95.0f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.soundPitch(1.0f)
			.registerAndAssign(Items.CAKE),

	BLAZE_CAKE = create("blaze_cake").damage(15)
			.reloadTicks(20)
			.cost(1)
			.knockback(0.3f)
			.velocity(1.1f)
			.accuracy(90.0f)
			.renderTumbling()
			.sticky()
			.preEntityHit(setFire(12))
			.soundPitch(1.0f)
			.registerAndAssign(AllItems.BLAZE_CAKE.get()),

	SNOWBALL = create("snowball").damage(0) // Unlimited Ammo Test
			.reloadTicks(10)
			.cost(0)
			.knockback(0.1f)
			.velocity(1.0f)
			.accuracy(80.0f)
			.soundPitch(1.0f)
			.registerAndAssign(Items.SNOWBALL)
			/*,

	 Maybe add that when it's going to be possible to change the texture without changing the item!
	HONEY_BOTTLE = create("honey_bottle").damage(1)
			.reloadTicks(100)
			.numberOfShot(1)
			.cost(1)
			.knockback(0.3f)
			.velocity(1.5f)
			.accuracy(10.0f)
			.sprayAccuracy(10.0f)
			.sprayInto(8)
			.sticky()
			//.onlyCostOnce() // If a refire system come out, this is going to be important!!
			.soundPitch(1.0f)
			.registerAndAssign(Items.HONEY_BOTTLE)*/;


	private static PotatoCannonProjectileType.Builder create(String name) {
		return new PotatoCannonProjectileType.Builder(Create.asResource(name));
	}

	private static Predicate<EntityHitResult> setFire(int seconds) {
		return ray -> {
			ray.getEntity().setSecondsOnFire(seconds);
			return false;
		};
	}

	private static Predicate<EntityHitResult> potion(MobEffect effect, int level, int ticks, boolean recoverable) {
		return ray -> {
			Entity entity = ray.getEntity();
			if (entity.level.isClientSide)
				return true;
			if (entity instanceof LivingEntity)
				applyEffect((LivingEntity) entity, new MobEffectInstance(effect, ticks, level - 1));
			return !recoverable;
		};
	}

	private static Predicate<EntityHitResult> foodEffects(FoodProperties food, boolean recoverable) {
		return ray -> {
			Entity entity = ray.getEntity();
			if (entity.level.isClientSide)
				return true;

			if (entity instanceof LivingEntity) {
				for (Pair<MobEffectInstance, Float> effect : food.getEffects()) {
					if (Create.RANDOM.nextFloat() < effect.getSecond())
						applyEffect((LivingEntity) entity, new MobEffectInstance(effect.getFirst()));
				}
			}
			return !recoverable;
		};
	}

	private static void applyEffect(LivingEntity entity, MobEffectInstance effect) {
		if (effect.getEffect().isInstantenous())
			effect.getEffect().applyInstantenousEffect(null, null, entity, effect.getDuration(), 1.0);
		else
			entity.addEffect(effect);
	}

	private static BiPredicate<LevelAccessor, BlockHitResult> plantCrop(IRegistryDelegate<? extends Block> cropBlock) {
		return (world, ray) -> {
			if (world.isClientSide())
				return true;

			BlockPos hitPos = ray.getBlockPos();
			if (!world.isAreaLoaded(hitPos, 1))
				return true;
			Direction face = ray.getDirection();
			BlockPos placePos = hitPos.relative(face);
			if (!world.getBlockState(placePos)
					.getMaterial()
					.isReplaceable())
				return false;
			if (!(cropBlock.get() instanceof IPlantable))
				return false;
			BlockState blockState = world.getBlockState(hitPos);
			if (!blockState.canSustainPlant(world, hitPos, face, (IPlantable) cropBlock.get()))
				return false;
			world.setBlock(placePos, cropBlock.get().defaultBlockState(), 3);
			return true;
		};
	}

	private static BiPredicate<LevelAccessor, BlockHitResult> placeBlockOnGround(IRegistryDelegate<? extends Block> block) {
		return (world, ray) -> {
			if (world.isClientSide())
				return true;

			BlockPos hitPos = ray.getBlockPos();
			if (!world.isAreaLoaded(hitPos, 1))
				return true;
			Direction face = ray.getDirection();
			BlockPos placePos = hitPos.relative(face);
			if (!world.getBlockState(placePos)
					.getMaterial()
					.isReplaceable())
				return false;

			if (face == Direction.UP) {
				world.setBlock(placePos, block.get().defaultBlockState(), 3);
			} else if (world instanceof Level) {
				double y = ray.getLocation().y - 0.5;
				if (!world.isEmptyBlock(placePos.above()))
					y = Math.min(y, placePos.getY());
				if (!world.isEmptyBlock(placePos.below()))
					y = Math.max(y, placePos.getY());

				FallingBlockEntity falling = new FallingBlockEntity((Level) world, placePos.getX() + 0.5, y,
						placePos.getZ() + 0.5, block.get().defaultBlockState());
				falling.time = 1;
				world.addFreshEntity(falling);
			}

			return true;
		};
	}

	private static Predicate<EntityHitResult> chorusTeleport(double teleportDiameter) {
		return ray -> {
			Entity entity = ray.getEntity();
			Level world = entity.getCommandSenderWorld();
			if (world.isClientSide)
				return true;
			if (!(entity instanceof LivingEntity))
				return false;
			LivingEntity livingEntity = (LivingEntity) entity;

			double entityX = livingEntity.getX();
			double entityY = livingEntity.getY();
			double entityZ = livingEntity.getZ();

			for (int teleportTry = 0; teleportTry < 16; ++teleportTry) {
				double teleportX = entityX + (livingEntity.getRandom().nextDouble() - 0.5D) * teleportDiameter;
				double teleportY = Mth.clamp(entityY + (livingEntity.getRandom().nextInt((int) teleportDiameter) - (int) (teleportDiameter / 2)), 0.0D, world.getHeight() - 1);
				double teleportZ = entityZ + (livingEntity.getRandom().nextDouble() - 0.5D) * teleportDiameter;

				EntityTeleportEvent.ChorusFruit event = ForgeEventFactory.onChorusFruitTeleport(livingEntity, teleportX, teleportY, teleportZ);
				if (event.isCanceled())
					return false;
				if (livingEntity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
					if (livingEntity.isPassenger())
						livingEntity.stopRiding();

					SoundEvent soundevent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
					world.playSound(null, entityX, entityY, entityZ, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
					livingEntity.playSound(soundevent, 1.0F, 1.0F);
					livingEntity.setDeltaMovement(Vec3.ZERO);
					return true;
				}
			}

			return false;
		};
	}

	public static void register() {
	}

}
