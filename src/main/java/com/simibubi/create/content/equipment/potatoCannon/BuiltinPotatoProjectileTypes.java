package com.simibubi.create.content.equipment.potatoCannon;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.mixin.accessor.FallingBlockEntityAccessor;
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
import net.minecraftforge.registries.ForgeRegistries;

public class BuiltinPotatoProjectileTypes {

	private static final GameProfile ZOMBIE_CONVERTER_NAME =
		new GameProfile(UUID.fromString("be12d3dc-27d3-4992-8c97-66be53fd49c5"), "Converter");
	private static final WorldAttached<FakePlayer> ZOMBIE_CONVERTERS =
		new WorldAttached<>(w -> new FakePlayer((ServerLevel) w, ZOMBIE_CONVERTER_NAME));

	public static final PotatoCannonProjectileType

	FALLBACK = create("fallback").damage(0)
		.register(),

		POTATO = create("potato").damage(5)
			.reloadTicks(15)
			.velocity(1.25f)
			.knockback(1.5f)
			.renderTumbling()
			.onBlockHit(plantCrop(Blocks.POTATOES))
			.registerAndAssign(Items.POTATO),

		BAKED_POTATO = create("baked_potato").damage(5)
			.reloadTicks(15)
			.velocity(1.25f)
			.knockback(0.5f)
			.renderTumbling()
			.preEntityHit(setFire(3))
			.registerAndAssign(Items.BAKED_POTATO),

		CARROT = create("carrot").damage(4)
			.reloadTicks(12)
			.velocity(1.45f)
			.knockback(0.3f)
			.renderTowardMotion(140, 1)
			.soundPitch(1.5f)
			.onBlockHit(plantCrop(Blocks.CARROTS))
			.registerAndAssign(Items.CARROT),

		GOLDEN_CARROT = create("golden_carrot").damage(12)
			.reloadTicks(15)
			.velocity(1.45f)
			.knockback(0.5f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.5f)
			.registerAndAssign(Items.GOLDEN_CARROT),

		SWEET_BERRIES = create("sweet_berry").damage(3)
			.reloadTicks(10)
			.knockback(0.1f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(3)
			.soundPitch(1.25f)
			.registerAndAssign(Items.SWEET_BERRIES),

		GLOW_BERRIES = create("glow_berry").damage(2)
			.reloadTicks(10)
			.knockback(0.05f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(2)
			.soundPitch(1.2f)
			.onEntityHit(potion(MobEffects.GLOWING, 1, 200, false))
			.registerAndAssign(Items.GLOW_BERRIES),

		CHOCOLATE_BERRIES = create("chocolate_berry").damage(4)
			.reloadTicks(10)
			.knockback(0.2f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(3)
			.soundPitch(1.25f)
			.registerAndAssign(AllItems.CHOCOLATE_BERRIES.get()),

		POISON_POTATO = create("poison_potato").damage(5)
			.reloadTicks(15)
			.knockback(0.05f)
			.velocity(1.25f)
			.renderTumbling()
			.onEntityHit(potion(MobEffects.POISON, 1, 160, true))
			.registerAndAssign(Items.POISONOUS_POTATO),

		CHORUS_FRUIT = create("chorus_fruit").damage(3)
			.reloadTicks(15)
			.velocity(1.20f)
			.knockback(0.05f)
			.renderTumbling()
			.onEntityHit(chorusTeleport(20))
			.registerAndAssign(Items.CHORUS_FRUIT),

		APPLE = create("apple").damage(5)
			.reloadTicks(10)
			.velocity(1.45f)
			.knockback(0.5f)
			.renderTumbling()
			.soundPitch(1.1f)
			.registerAndAssign(Items.APPLE),

		HONEYED_APPLE = create("honeyed_apple").damage(6)
			.reloadTicks(15)
			.velocity(1.35f)
			.knockback(0.1f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(potion(MobEffects.MOVEMENT_SLOWDOWN, 2, 160, true))
			.registerAndAssign(AllItems.HONEYED_APPLE.get()),

		GOLDEN_APPLE = create("golden_apple").damage(1)
			.reloadTicks(100)
			.velocity(1.45f)
			.knockback(0.05f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(ray -> {
				Entity entity = ray.getEntity();
				Level world = entity.level;

				if (!(entity instanceof ZombieVillager) || !((ZombieVillager) entity).hasEffect(MobEffects.WEAKNESS))
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
			.velocity(1.45f)
			.knockback(0.05f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(foodEffects(Foods.ENCHANTED_GOLDEN_APPLE, false))
			.registerAndAssign(Items.ENCHANTED_GOLDEN_APPLE),

		BEETROOT = create("beetroot").damage(2)
			.reloadTicks(5)
			.velocity(1.6f)
			.knockback(0.1f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.6f)
			.registerAndAssign(Items.BEETROOT),

		MELON_SLICE = create("melon_slice").damage(3)
			.reloadTicks(8)
			.knockback(0.1f)
			.velocity(1.45f)
			.renderTumbling()
			.soundPitch(1.5f)
			.registerAndAssign(Items.MELON_SLICE),

		GLISTERING_MELON = create("glistering_melon").damage(5)
			.reloadTicks(8)
			.knockback(0.1f)
			.velocity(1.45f)
			.renderTumbling()
			.soundPitch(1.5f)
			.onEntityHit(potion(MobEffects.GLOWING, 1, 100, true))
			.registerAndAssign(Items.GLISTERING_MELON_SLICE),

		MELON_BLOCK = create("melon_block").damage(8)
			.reloadTicks(20)
			.knockback(2.0f)
			.velocity(0.95f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(placeBlockOnGround(Blocks.MELON))
			.registerAndAssign(Blocks.MELON),

		PUMPKIN_BLOCK = create("pumpkin_block").damage(6)
			.reloadTicks(15)
			.knockback(2.0f)
			.velocity(0.95f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(placeBlockOnGround(Blocks.PUMPKIN))
			.registerAndAssign(Blocks.PUMPKIN),

		PUMPKIN_PIE = create("pumpkin_pie").damage(7)
			.reloadTicks(15)
			.knockback(0.05f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.soundPitch(1.1f)
			.registerAndAssign(Items.PUMPKIN_PIE),

		CAKE = create("cake").damage(8)
			.reloadTicks(15)
			.knockback(0.1f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.soundPitch(1.0f)
			.registerAndAssign(Items.CAKE),

		BLAZE_CAKE = create("blaze_cake").damage(15)
			.reloadTicks(20)
			.knockback(0.3f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.preEntityHit(setFire(12))
			.soundPitch(1.0f)
			.registerAndAssign(AllItems.BLAZE_CAKE.get())

	;

	private static PotatoCannonProjectileType.Builder create(String name) {
		return new PotatoCannonProjectileType.Builder(Create.asResource(name));
	}

	private static Predicate<EntityHitResult> setFire(int seconds) {
		return ray -> {
			ray.getEntity()
				.setSecondsOnFire(seconds);
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
		if (effect.getEffect()
			.isInstantenous())
			effect.getEffect()
				.applyInstantenousEffect(null, null, entity, effect.getDuration(), 1.0);
		else
			entity.addEffect(effect);
	}

	private static BiPredicate<LevelAccessor, BlockHitResult> plantCrop(Supplier<? extends Block> cropBlock) {
		return (world, ray) -> {
			if (world.isClientSide())
				return true;

			BlockPos hitPos = ray.getBlockPos();
			if (world instanceof Level l && !l.isLoaded(hitPos))
				return true;
			Direction face = ray.getDirection();
			if (face != Direction.UP)
				return false;
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
			world.setBlock(placePos, cropBlock.get()
				.defaultBlockState(), 3);
			return true;
		};
	}

	private static BiPredicate<LevelAccessor, BlockHitResult> plantCrop(Block cropBlock) {
		return plantCrop(ForgeRegistries.BLOCKS.getDelegateOrThrow(cropBlock));
	}

	private static BiPredicate<LevelAccessor, BlockHitResult> placeBlockOnGround(
		Supplier<? extends Block> block) {
		return (world, ray) -> {
			if (world.isClientSide())
				return true;

			BlockPos hitPos = ray.getBlockPos();
			if (world instanceof Level l && !l.isLoaded(hitPos))
				return true;
			Direction face = ray.getDirection();
			BlockPos placePos = hitPos.relative(face);
			if (!world.getBlockState(placePos)
				.getMaterial()
				.isReplaceable())
				return false;

			if (face == Direction.UP) {
				world.setBlock(placePos, block.get()
					.defaultBlockState(), 3);
			} else if (world instanceof Level level) {
				double y = ray.getLocation().y - 0.5;
				if (!world.isEmptyBlock(placePos.above()))
					y = Math.min(y, placePos.getY());
				if (!world.isEmptyBlock(placePos.below()))
					y = Math.max(y, placePos.getY());

				FallingBlockEntity falling = FallingBlockEntityAccessor.create$callInit(level, placePos.getX() + 0.5, y,
					placePos.getZ() + 0.5, block.get().defaultBlockState());
				falling.time = 1;
				world.addFreshEntity(falling);
			}

			return true;
		};
	}

	private static BiPredicate<LevelAccessor, BlockHitResult> placeBlockOnGround(Block block) {
		return placeBlockOnGround(ForgeRegistries.BLOCKS.getDelegateOrThrow(block));
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
				double teleportX = entityX + (livingEntity.getRandom()
					.nextDouble() - 0.5D) * teleportDiameter;
				double teleportY = Mth.clamp(entityY + (livingEntity.getRandom()
					.nextInt((int) teleportDiameter) - (int) (teleportDiameter / 2)), 0.0D, world.getHeight() - 1);
				double teleportZ = entityZ + (livingEntity.getRandom()
					.nextDouble() - 0.5D) * teleportDiameter;

				EntityTeleportEvent.ChorusFruit event =
					ForgeEventFactory.onChorusFruitTeleport(livingEntity, teleportX, teleportY, teleportZ);
				if (event.isCanceled())
					return false;
				if (livingEntity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
					if (livingEntity.isPassenger())
						livingEntity.stopRiding();

					SoundEvent soundevent =
						livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
					world.playSound(null, entityX, entityY, entityZ, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
					livingEntity.playSound(soundevent, 1.0F, 1.0F);
					livingEntity.setDeltaMovement(Vec3.ZERO);
					return true;
				}
			}

			return false;
		};
	}

	public static void register() {}

}
