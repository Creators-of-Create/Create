package com.simibubi.create.content.curiosities.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.simibubi.create.Create;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraftforge.registries.IRegistryDelegate;

public class PotatoCannonProjectileTypes {

	public static final Map<ResourceLocation, PotatoCannonProjectileTypes> ALL = new HashMap<>();
	public static final Map<IRegistryDelegate<Item>, PotatoCannonProjectileTypes> ITEM_MAP = new HashMap<>();
	public static final PotatoCannonProjectileTypes

	FALLBACK = create("fallback").damage(0)
		.register(),

		POTATO = create("potato").damage(4)
			.reloadTicks(15)
			.knockback(1.5f)
			.renderTumbling()
			.registerAndAssign(Items.POTATO),

		BAKED_POTATO = create("baked_potato").damage(3)
			.reloadTicks(15)
			.knockback(1.5f)
			.renderTumbling()
			.onEntityHit(ray -> ray.getEntity()
				.setFireTicks(10))
			.registerAndAssign(Items.BAKED_POTATO),

		CARROT = create("carrot").damage(3)
			.renderTowardMotion(140, 1)
			.velocity(1.25f)
			.knockback(0.5f)
			.soundPitch(1.25f)
			.registerAndAssign(Items.CARROT),

		GOLDEN_CARROT = create("golden_carrot").damage(8)
			.reloadTicks(20)
			.knockback(0.5f)
			.velocity(1.25f)
			.renderTowardMotion(140, 2)
			.soundPitch(1.25f)
			.registerAndAssign(Items.GOLDEN_CARROT),

		POISON_POTATO = create("poison_potato").damage(5)
			.reloadTicks(15)
			.knockback(0.5f)
			.renderTumbling()
			.onEntityHit(ray -> {
				Entity entity = ray.getEntity();
				if (entity instanceof LivingEntity)
					((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.POISON, 40));
			})
			.registerAndAssign(Items.POISONOUS_POTATO)

	;

	public static void registerType(ResourceLocation resLoc, PotatoCannonProjectileTypes type) {
		ALL.put(resLoc, type);
	}

	public static void assignType(IRegistryDelegate<Item> item, PotatoCannonProjectileTypes type) {
		ITEM_MAP.put(item, type);
	}

	public static Optional<PotatoCannonProjectileTypes> getProjectileTypeOf(ItemStack item) {
		if (item.isEmpty())
			return Optional.empty();
		return Optional.ofNullable(ITEM_MAP.get(item.getItem().delegate));
	}

	public static void register() {}

	private static PotatoCannonProjectileTypes.Builder create(String name) {
		return new PotatoCannonProjectileTypes.Builder(Create.asResource(name));
	}

	private float gravityMultiplier = 1;
	private float velocityMultiplier = 1;
	private float knockback = 1;
	private int reloadTicks = 10;
	private int damage = 1;
	private float fwoompPitch = 1;
	private PotatoProjectileRenderMode renderMode = new PotatoProjectileRenderMode.Billboard();
	private Consumer<EntityRayTraceResult> onEntityHit = e -> {
	};
	private Consumer<BlockRayTraceResult> onBlockHit = e -> {
	};

	public float getGravityMultiplier() {
		return gravityMultiplier;
	}

	public float getVelocityMultiplier() {
		return velocityMultiplier;
	}

	public float getKnockback() {
		return knockback;
	}

	public int getReloadTicks() {
		return reloadTicks;
	}

	public float getSoundPitch() {
		return fwoompPitch;
	}

	public PotatoProjectileRenderMode getRenderMode() {
		return renderMode;
	}

	public int getDamage() {
		return damage;
	}

	public void onEntityHit(EntityRayTraceResult ray) {
		onEntityHit.accept(ray);
	}

	public void onBlockHit(BlockRayTraceResult ray) {
		onBlockHit.accept(ray);
	}

	public static class Builder {

		ResourceLocation loc;
		PotatoCannonProjectileTypes result;

		public Builder(ResourceLocation loc) {
			this.result = new PotatoCannonProjectileTypes();
			this.loc = loc;
		}

		public Builder damage(int damage) {
			result.damage = damage;
			return this;
		}

		public Builder gravity(float modifier) {
			result.gravityMultiplier = modifier;
			return this;
		}

		public Builder knockback(float knockback) {
			result.knockback = knockback;
			return this;
		}

		public Builder reloadTicks(int reload) {
			result.reloadTicks = reload;
			return this;
		}

		public Builder soundPitch(float pitch) {
			result.fwoompPitch = pitch;
			return this;
		}

		public Builder velocity(float velocity) {
			result.velocityMultiplier = velocity;
			return this;
		}

		public Builder renderTumbling() {
			result.renderMode = new PotatoProjectileRenderMode.Tumble();
			return this;
		}

		public Builder renderBillboard() {
			result.renderMode = new PotatoProjectileRenderMode.Billboard();
			return this;
		}

		public Builder renderTowardMotion(int spriteAngle, float spin) {
			result.renderMode = new PotatoProjectileRenderMode.TowardMotion(spriteAngle, spin);
			return this;
		}

		public Builder onEntityHit(Consumer<EntityRayTraceResult> callback) {
			result.onEntityHit = callback;
			return this;
		}

		public Builder onBlockHit(Consumer<BlockRayTraceResult> callback) {
			result.onBlockHit = callback;
			return this;
		}

		public PotatoCannonProjectileTypes register() {
			registerType(loc, result);
			return result;
		}

		public PotatoCannonProjectileTypes registerAndAssign(IItemProvider... items) {
			registerType(loc, result);
			for (IItemProvider provider : items)
				assignType(provider.asItem().delegate, result);
			return result;
		}

	}

}
