package com.simibubi.create.api.equipment.potatoCannon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction.Type;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.Billboard;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.TowardMotion;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes.Tumble;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

// TODO: 1.21.1+ - Move into api package
public record PotatoCannonProjectileType(HolderSet<Item> items, int reloadTicks, int damage, int split, float knockback,
										 float drag, float velocityMultiplier, float gravityMultiplier,
										 float soundPitch, boolean sticky, ItemStack dropStack,
										 PotatoProjectileRenderMode renderMode,
										 Optional<PotatoProjectileEntityHitAction> preEntityHit,
										 Optional<PotatoProjectileEntityHitAction> onEntityHit,
										 Optional<PotatoProjectileBlockHitAction> onBlockHit) {
	public static final Codec<PotatoCannonProjectileType> CODEC = RecordCodecBuilder.create(i -> i.group(
		RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(PotatoCannonProjectileType::items),
		Codec.INT.optionalFieldOf("reload_ticks", 10).forGetter(PotatoCannonProjectileType::reloadTicks),
		Codec.INT.optionalFieldOf("damage", 1).forGetter(PotatoCannonProjectileType::damage),
		Codec.INT.optionalFieldOf("split", 1).forGetter(PotatoCannonProjectileType::split),
		Codec.FLOAT.optionalFieldOf("knockback", 1f).forGetter(PotatoCannonProjectileType::knockback),
		Codec.FLOAT.optionalFieldOf("drag", .99f).forGetter(PotatoCannonProjectileType::drag),
		Codec.FLOAT.optionalFieldOf("velocity_multiplier", 1f).forGetter(PotatoCannonProjectileType::velocityMultiplier),
		Codec.FLOAT.optionalFieldOf("gravity_multiplier", 1f).forGetter(PotatoCannonProjectileType::gravityMultiplier),
		Codec.FLOAT.optionalFieldOf("sound_pitch", 1f).forGetter(PotatoCannonProjectileType::soundPitch),
		Codec.BOOL.optionalFieldOf("sticky", false).forGetter(PotatoCannonProjectileType::sticky),
		ItemStack.CODEC.optionalFieldOf("drop_stack", ItemStack.EMPTY).forGetter(PotatoCannonProjectileType::dropStack),
		PotatoProjectileRenderMode.CODEC.optionalFieldOf("render_mode", Billboard.INSTANCE).forGetter(PotatoCannonProjectileType::renderMode),
		PotatoProjectileEntityHitAction.CODEC.optionalFieldOf("pre_entity_hit").forGetter(p -> p.preEntityHit),
		PotatoProjectileEntityHitAction.CODEC.optionalFieldOf("on_entity_hit").forGetter(p -> p.onEntityHit),
		PotatoProjectileBlockHitAction.CODEC.optionalFieldOf("on_block_hit").forGetter(p -> p.onBlockHit)
	).apply(i, PotatoCannonProjectileType::new));

	public static Optional<Reference<PotatoCannonProjectileType>> getTypeForItem(RegistryAccess registryAccess, Item item) {
		// Cache this if it causes performance issues, but it probably won't
		return registryAccess.lookupOrThrow(CreateRegistries.POTATO_PROJECTILE_TYPE)
			.listElements()
			.filter(ref -> ref.value().items.contains(item.builtInRegistryHolder()))
			.findFirst();
	}

	public boolean preEntityHit(ItemStack stack, EntityHitResult ray) {
		return preEntityHit.map(i -> i.execute(stack, ray, Type.PRE_HIT)).orElse(false);
	}

	public boolean onEntityHit(ItemStack stack, EntityHitResult ray) {
		return onEntityHit.map(i -> i.execute(stack, ray, Type.ON_HIT)).orElse(false);
	}

	public boolean onBlockHit(LevelAccessor level, ItemStack stack, BlockHitResult ray) {
		return onBlockHit.map(i -> i.execute(level, stack, ray)).orElse(false);
	}

	// Copy the stack so it's not mutated and lost
	@Override
	public ItemStack dropStack() {
		return dropStack.copy();
	}

	public static class Builder {
		private final List<Holder<Item>> items = new ArrayList<>();
		private int reloadTicks = 10;
		private int damage = 1;
		private int split = 1;
		private float knockback = 1f;
		private float drag = 0.99f;
		private float velocityMultiplier = 1f;
		private float gravityMultiplier = 1f;
		private float soundPitch = 1f;
		private boolean sticky = false;
		private ItemStack dropStack = ItemStack.EMPTY;
		private PotatoProjectileRenderMode renderMode = Billboard.INSTANCE;
		private PotatoProjectileEntityHitAction preEntityHit = null;
		private PotatoProjectileEntityHitAction onEntityHit = null;
		private PotatoProjectileBlockHitAction onBlockHit = null;

		public Builder reloadTicks(int reload) {
			this.reloadTicks = reload;
			return this;
		}

		public Builder damage(int damage) {
			this.damage = damage;
			return this;
		}

		public Builder splitInto(int split) {
			this.split = split;
			return this;
		}

		public Builder knockback(float knockback) {
			this.knockback = knockback;
			return this;
		}

		public Builder drag(float drag) {
			this.drag = drag;
			return this;
		}

		public Builder velocity(float velocity) {
			this.velocityMultiplier = velocity;
			return this;
		}

		public Builder gravity(float modifier) {
			this.gravityMultiplier = modifier;
			return this;
		}

		public Builder soundPitch(float pitch) {
			this.soundPitch = pitch;
			return this;
		}

		public Builder sticky() {
			this.sticky = true;
			return this;
		}

		public Builder dropStack(ItemStack stack) {
			this.dropStack = stack;
			return this;
		}

		public Builder renderMode(PotatoProjectileRenderMode renderMode) {
			this.renderMode = renderMode;
			return this;
		}

		public Builder renderBillboard() {
			renderMode(Billboard.INSTANCE);
			return this;
		}

		public Builder renderTumbling() {
			renderMode(Tumble.INSTANCE);
			return this;
		}

		public Builder renderTowardMotion(int spriteAngle, float spin) {
			renderMode(new TowardMotion(spriteAngle, spin));
			return this;
		}

		public Builder preEntityHit(PotatoProjectileEntityHitAction entityHitAction) {
			this.preEntityHit = entityHitAction;
			return this;
		}

		public Builder onEntityHit(PotatoProjectileEntityHitAction entityHitAction) {
			this.onEntityHit = entityHitAction;
			return this;
		}

		public Builder onBlockHit(PotatoProjectileBlockHitAction blockHitAction) {
			this.onBlockHit = blockHitAction;
			return this;
		}

		public Builder addItems(ItemLike... items) {
			for (ItemLike provider : items)
				this.items.add(provider.asItem().builtInRegistryHolder());
			return this;
		}

		public PotatoCannonProjectileType build() {
			return new PotatoCannonProjectileType(
				HolderSet.direct(items),
				reloadTicks,
				damage,
				split,
				knockback,
				drag,
				velocityMultiplier,
				gravityMultiplier,
				soundPitch,
				sticky,
				dropStack,
				renderMode,
				Optional.ofNullable(preEntityHit),
				Optional.ofNullable(onEntityHit),
				Optional.ofNullable(onBlockHit)
			);
		}
	}
}
