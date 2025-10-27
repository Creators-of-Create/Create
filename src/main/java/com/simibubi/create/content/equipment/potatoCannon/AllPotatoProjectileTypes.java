package com.simibubi.create.content.equipment.potatoCannon;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileBlockHitActions.PlaceBlockOnGround;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileBlockHitActions.PlantCrop;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.ChorusTeleport;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.CureZombieVillager;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.FoodEffects;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.PotionEffect;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.SetOnFire;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.SuspiciousStew;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class AllPotatoProjectileTypes {
	public static final ResourceKey<PotatoCannonProjectileType> FALLBACK = ResourceKey.create(CreateRegistries.POTATO_PROJECTILE_TYPE, Create.asResource("fallback"));

	public static void bootstrap(BootstrapContext<PotatoCannonProjectileType> ctx) {
		register(ctx, "fallback", new PotatoCannonProjectileType.Builder()
			.damage(0)
			.build());

		register(ctx, "potato", new PotatoCannonProjectileType.Builder()
			.damage(5)
			.reloadTicks(15)
			.velocity(1.25f)
			.knockback(1.5f)
			.renderTumbling()
			.onBlockHit(new PlantCrop(Blocks.POTATOES))
			.addItems(Items.POTATO)
			.build());

		register(ctx, "baked_potato", new PotatoCannonProjectileType.Builder()
			.damage(5)
			.reloadTicks(15)
			.velocity(1.25f)
			.knockback(0.5f)
			.renderTumbling()
			.preEntityHit(SetOnFire.seconds(3))
			.addItems(Items.BAKED_POTATO)
			.build());

		register(ctx, "carrot", new PotatoCannonProjectileType.Builder()
			.damage(4)
			.reloadTicks(12)
			.velocity(1.45f)
			.knockback(0.3f)
			.renderTowardMotion(140, 1)
			.soundPitch(1.5f)
			.onBlockHit(new PlantCrop(Blocks.CARROTS))
			.addItems(Items.CARROT)
			.build());

		register(ctx, "golden_carrot", new PotatoCannonProjectileType.Builder()
			.damage(12)
			.reloadTicks(15)
			.velocity(1.45f)
			.knockback(0.5f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.5f)
			.addItems(Items.GOLDEN_CARROT)
			.build());

		register(ctx, "sweet_berry", new PotatoCannonProjectileType.Builder()
			.damage(3)
			.reloadTicks(10)
			.knockback(0.1f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(3)
			.soundPitch(1.25f)
			.addItems(Items.SWEET_BERRIES)
			.build());

		register(ctx, "glow_berry", new PotatoCannonProjectileType.Builder()
			.damage(2)
			.reloadTicks(10)
			.knockback(0.05f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(2)
			.soundPitch(1.2f)
			.onEntityHit(new PotionEffect(MobEffects.GLOWING, 1, 200, false))
			.addItems(Items.GLOW_BERRIES)
			.build());

		register(ctx, "chocolate_berry", new PotatoCannonProjectileType.Builder()
			.damage(4)
			.reloadTicks(10)
			.knockback(0.2f)
			.velocity(1.05f)
			.renderTumbling()
			.splitInto(3)
			.soundPitch(1.25f)
			.addItems(AllItems.CHOCOLATE_BERRIES.get())
			.build());

		register(ctx, "poison_potato", new PotatoCannonProjectileType.Builder()
			.damage(5)
			.reloadTicks(15)
			.knockback(0.05f)
			.velocity(1.25f)
			.renderTumbling()
			.onEntityHit(new PotionEffect(MobEffects.POISON, 1, 160, true))
			.addItems(Items.POISONOUS_POTATO)
			.build());

		register(ctx, "chorus_fruit", new PotatoCannonProjectileType.Builder()
			.damage(3)
			.reloadTicks(15)
			.velocity(1.20f)
			.knockback(0.05f)
			.renderTumbling()
			.onEntityHit(new ChorusTeleport(20))
			.addItems(Items.CHORUS_FRUIT)
			.build());

		register(ctx, "apple", new PotatoCannonProjectileType.Builder()
			.damage(5)
			.reloadTicks(10)
			.velocity(1.45f)
			.knockback(0.5f)
			.renderTumbling()
			.soundPitch(1.1f)
			.addItems(Items.APPLE)
			.build());

		register(ctx, "honeyed_apple", new PotatoCannonProjectileType.Builder()
			.damage(6)
			.reloadTicks(15)
			.velocity(1.35f)
			.knockback(0.1f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(new PotionEffect(MobEffects.MOVEMENT_SLOWDOWN, 2, 160, true))
			.addItems(AllItems.HONEYED_APPLE.get())
			.build());

		register(ctx, "golden_apple", new PotatoCannonProjectileType.Builder()
			.damage(1)
			.reloadTicks(100)
			.velocity(1.45f)
			.knockback(0.05f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(CureZombieVillager.INSTANCE)
			.addItems(Items.GOLDEN_APPLE)
			.build());

		register(ctx, "enchanted_golden_apple", new PotatoCannonProjectileType.Builder()
			.damage(1)
			.reloadTicks(100)
			.velocity(1.45f)
			.knockback(0.05f)
			.renderTumbling()
			.soundPitch(1.1f)
			.onEntityHit(new FoodEffects(Foods.ENCHANTED_GOLDEN_APPLE, false))
			.addItems(Items.ENCHANTED_GOLDEN_APPLE)
			.build());

		register(ctx, "beetroot", new PotatoCannonProjectileType.Builder()
			.damage(2)
			.reloadTicks(5)
			.velocity(1.6f)
			.knockback(0.1f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.6f)
			.addItems(Items.BEETROOT)
			.build());

		register(ctx, "melon_slice", new PotatoCannonProjectileType.Builder()
			.damage(3)
			.reloadTicks(8)
			.knockback(0.1f)
			.velocity(1.45f)
			.renderTumbling()
			.soundPitch(1.5f)
			.addItems(Items.MELON_SLICE)
			.build());

		register(ctx, "glistering_melon", new PotatoCannonProjectileType.Builder()
			.damage(5)
			.reloadTicks(8)
			.knockback(0.1f)
			.velocity(1.45f)
			.renderTumbling()
			.soundPitch(1.5f)
			.onEntityHit(new PotionEffect(MobEffects.GLOWING, 1, 100, true))
			.addItems(Items.GLISTERING_MELON_SLICE)
			.build());

		register(ctx, "melon_block", new PotatoCannonProjectileType.Builder()
			.damage(8)
			.reloadTicks(20)
			.knockback(2.0f)
			.velocity(0.95f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(new PlaceBlockOnGround(Blocks.MELON))
			.addItems(Blocks.MELON)
			.build());

		register(ctx, "pumpkin_block", new PotatoCannonProjectileType.Builder()
			.damage(6)
			.reloadTicks(15)
			.knockback(2.0f)
			.velocity(0.95f)
			.renderTumbling()
			.soundPitch(0.9f)
			.onBlockHit(new PlaceBlockOnGround(Blocks.PUMPKIN))
			.addItems(Blocks.PUMPKIN)
			.build());

		register(ctx, "pumpkin_pie", new PotatoCannonProjectileType.Builder()
			.damage(7)
			.reloadTicks(15)
			.knockback(0.05f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.soundPitch(1.1f)
			.addItems(Items.PUMPKIN_PIE)
			.build());

		register(ctx, "cake", new PotatoCannonProjectileType.Builder()
			.damage(8)
			.reloadTicks(15)
			.knockback(0.1f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.addItems(Items.CAKE)
			.build());

		register(ctx, "blaze_cake", new PotatoCannonProjectileType.Builder()
			.damage(15)
			.reloadTicks(20)
			.knockback(0.3f)
			.velocity(1.1f)
			.renderTumbling()
			.sticky()
			.preEntityHit(SetOnFire.seconds(12))
			.addItems(AllItems.BLAZE_CAKE.get())
			.build());

		register(ctx, "fish", new PotatoCannonProjectileType.Builder()
			.damage(4)
			.knockback(0.6f)
			.velocity(1.3f)
			.renderTowardMotion(140, 1)
			.sticky()
			.soundPitch(1.3f)
			.addItems(Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON, Items.TROPICAL_FISH)
			.build());

		register(ctx, "pufferfish", new PotatoCannonProjectileType.Builder()
			.damage(4)
			.knockback(0.4f)
			.velocity(1.1f)
			.renderTowardMotion(140, 1)
			.sticky()
			.onEntityHit(new FoodEffects(Foods.PUFFERFISH, false))
			.soundPitch(1.1f)
			.addItems(Items.PUFFERFISH)
			.build());

		register(ctx, "suspicious_stew", new PotatoCannonProjectileType.Builder()
			.damage(3)
			.reloadTicks(40)
			.knockback(0.2f)
			.velocity(0.8f)
			.renderTowardMotion(140, 1)
			.dropStack(Items.BOWL.getDefaultInstance())
			.onEntityHit(SuspiciousStew.INSTANCE)
			.addItems(Items.SUSPICIOUS_STEW)
			.build());
	}

	private static void register(BootstrapContext<PotatoCannonProjectileType> ctx, String name, PotatoCannonProjectileType type) {
		ctx.register(ResourceKey.create(CreateRegistries.POTATO_PROJECTILE_TYPE, Create.asResource(name)), type);
	}
}
