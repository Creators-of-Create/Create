package com.simibubi.create.content.curiosities.weapons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.ForgeRegistries;

public class PotatoCannonProjectileType {

	private List<Supplier<Item>> items = new ArrayList<>();
	private List<ProjectileEffect> effects = new ArrayList<>();
	private Supplier<Item> itemOverride = null;
	private int reloadTicks = 10;
	private int damage = 1;
	private int spray = 1;
	private int split = 0;
	private List<SplitProperty> splitInto = new ArrayList<>();
	private float splitSpeed = 0.25f;
	private int splitAmount = -1;
	private float knockback = 1;
	private float drag = 0.99f;
	private float velocityMultiplier = 1;
	private float gravityMultiplier = 1;
	private float soundPitch = 1;
	private boolean sticky = false;
	private PotatoProjectileRenderMode renderMode = null;
	private String renderType = "";
	private int angle = 0;
	private float spin = 0;
	private int fireTimer = 0;
	private int iceTimer = 0;
	private boolean recoverable = true;

	private Predicate<EntityHitResult> preEntityHit = e -> false; // True if hit should be canceled
	private Predicate<EntityHitResult> onEntityHit = e -> false; // True if shouldn't recover projectile
	private BiPredicate<LevelAccessor, BlockHitResult> onBlockHit = (w, ray) -> false;

	protected PotatoCannonProjectileType() {
	}

	public List<Supplier<Item>> getItems() {
		return items;
	}

	public Supplier<Item> getOverride(){
		return itemOverride;
	}

	public int getReloadTicks() {
		return reloadTicks;
	}

	public int getDamage() {
		return damage;
	}

	public int getSpray() {
		return spray;
	}

	public int getSplit() {
		return split;
	}

	public float getSplitSpeed() {
		return splitSpeed;
	}

	public int getSplitAmount() {
		return splitAmount;
	}

	public List<SplitProperty> getSplitInto() {
		return splitInto;
	}

	public float getKnockback() {
		return knockback;
	}

	public float getDrag() {
		return drag;
	}

	public float getVelocityMultiplier() {
		return velocityMultiplier;
	}

	public float getGravityMultiplier() {
		return gravityMultiplier;
	}

	public float getSoundPitch() {
		return soundPitch;
	}

	public boolean isSticky() {
		return sticky;
	}

	public PotatoProjectileRenderMode getRenderMode() {
		if (renderMode != null)
			return renderMode;
		return switch (renderType) {
			case "tumbling" -> new PotatoProjectileRenderMode.Tumble();
			case "motion" -> new PotatoProjectileRenderMode.TowardMotion(angle, spin);
			default -> new PotatoProjectileRenderMode.Billboard();
		};
	}

	public boolean preEntityHit(EntityHitResult ray) {
		return preEntityHit.test(ray);
	}

	public boolean onEntityHit(EntityHitResult ray) {
		return onEntityHit.test(ray);
	}

	public boolean onBlockHit(LevelAccessor world, BlockHitResult ray) {
		return onBlockHit.test(world, ray);
	}

	public static PotatoCannonProjectileType fromJson(JsonObject object) {
		PotatoCannonProjectileType type = new PotatoCannonProjectileType();
		try {
			JsonElement itemsElement = object.get("items");
			if (itemsElement != null && itemsElement.isJsonArray()) {
				for (JsonElement element : itemsElement.getAsJsonArray()) {
					if (element.isJsonPrimitive()) {
						JsonPrimitive primitive = element.getAsJsonPrimitive();
						if (primitive.isString()) {
							try {
								Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(primitive.getAsString()));
								if (item != null) {
									type.items.add(item.delegate);
								}
							} catch (ResourceLocationException e) {
								//
							}
						}
					}
				}
			}
			JsonElement effectElement = object.get("effects");
			if (effectElement != null && effectElement.isJsonArray()) {
				for (JsonElement effect : effectElement.getAsJsonArray()) {
					if (effect.isJsonObject()) {
						JsonObject effectType = effect.getAsJsonObject();
						String effectId = "";
						int level = -1;
						int seconds = -1;
						if (effectType.get("effect").isJsonPrimitive() && effectType.get("effect").getAsJsonPrimitive().isString())
							effectId = effectType.get("effect").getAsString();
						if (effectType.get("level").isJsonPrimitive() && effectType.get("level").getAsJsonPrimitive().isNumber())
							level = effectType.get("level").getAsInt();
						if (effectType.get("seconds").isJsonPrimitive() && effectType.get("seconds").getAsJsonPrimitive().isNumber())
							seconds = effectType.get("seconds").getAsInt();
						try {
							MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectId));
							if (mobEffect != null) {
								type.effects.add(new ProjectileEffect(mobEffect, level, seconds));
							}
						} catch (ResourceLocationException e) {
							//
						}
					}
				}
			}

			parseJsonPrimitive(object, "fire_timer", JsonPrimitive::isNumber, primitive -> type.fireTimer = primitive.getAsInt());
			parseJsonPrimitive(object, "ice_timer", JsonPrimitive::isNumber, primitive -> type.iceTimer = primitive.getAsInt());

			parseJsonPrimitive(object, "reload_ticks", JsonPrimitive::isNumber, primitive -> type.reloadTicks = primitive.getAsInt());
			parseJsonPrimitive(object, "damage", JsonPrimitive::isNumber, primitive -> type.damage = primitive.getAsInt());
			parseJsonPrimitive(object, "spray", JsonPrimitive::isNumber, primitive -> type.spray = primitive.getAsInt());
			parseJsonPrimitive(object, "split", JsonPrimitive::isNumber, primitive -> type.split = primitive.getAsInt());
			parseJsonPrimitive(object, "split_speed", JsonPrimitive::isNumber, primitive -> type.splitSpeed = primitive.getAsFloat());
			parseJsonPrimitive(object, "split_amount", JsonPrimitive::isNumber, primitive -> type.splitAmount = primitive.getAsInt());
			parseJsonPrimitive(object, "knockback", JsonPrimitive::isNumber, primitive -> type.knockback = primitive.getAsFloat());
			parseJsonPrimitive(object, "drag", JsonPrimitive::isNumber, primitive -> type.drag = primitive.getAsFloat());
			parseJsonPrimitive(object, "velocity_multiplier", JsonPrimitive::isNumber, primitive -> type.velocityMultiplier = primitive.getAsFloat());
			parseJsonPrimitive(object, "gravity_multiplier", JsonPrimitive::isNumber, primitive -> type.gravityMultiplier = primitive.getAsFloat());
			parseJsonPrimitive(object, "sound_pitch", JsonPrimitive::isNumber, primitive -> type.soundPitch = primitive.getAsFloat());
			parseJsonPrimitive(object, "sticky", JsonPrimitive::isBoolean, primitive -> type.sticky = primitive.getAsBoolean());
			parseJsonPrimitive(object, "angle", JsonPrimitive::isNumber, primitive -> type.angle = primitive.getAsInt());
			parseJsonPrimitive(object, "spin", JsonPrimitive::isNumber, primitive -> type.spin = primitive.getAsFloat());
			parseJsonPrimitive(object, "render_type", JsonPrimitive::isString, primitive -> type.renderType = primitive.getAsString());
			parseJsonPrimitive(object, "recover", JsonPrimitive::isBoolean, primitive -> type.recoverable = primitive.getAsBoolean());

			if (object.get("split_into").isJsonArray())
				for (JsonElement element : (JsonArray) object.get("split_into")) {
					if (element.isJsonObject()) {
						JsonObject projectile = element.getAsJsonObject();
						if (projectile.get("projectile").isJsonObject())
							type.splitInto.add(
									new SplitProperty(fromJson(projectile.getAsJsonObject("projectile")),
											projectile.getAsJsonPrimitive("chance").getAsFloat())
							);
					}
					System.err.println("Element : " + element);
				}
			// REALLY unoptimized, will do something better later on
		} catch (Exception e) {
			//
		}
		return type;
	}

	private static void parseJsonPrimitive(JsonObject object, String key, Predicate<JsonPrimitive> predicate, Consumer<JsonPrimitive> consumer) {
		JsonElement element = object.get(key);
		if (element != null && element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (predicate.test(primitive)) {
				consumer.accept(primitive);
			}
		}
	}

	public static void toBuffer(PotatoCannonProjectileType type, FriendlyByteBuf buffer) {
		buffer.writeVarInt(type.items.size());
		for (Supplier<Item> delegate : type.items) {
			buffer.writeResourceLocation(RegisteredObjects.getKeyOrThrow(delegate.get()));
		}
		buffer.writeVarInt(type.effects.size());
		for (ProjectileEffect pEffect : type.effects) {
			buffer.writeResourceLocation(pEffect.effect().getRegistryName());// Actually cannot be null lmao
			buffer.writeInt(pEffect.level());
			buffer.writeInt(pEffect.seconds());
		}
		buffer.writeInt(type.fireTimer);
		buffer.writeInt(type.iceTimer);
		buffer.writeBoolean(type.recoverable);
		buffer.writeInt(type.reloadTicks);
		buffer.writeInt(type.damage);
		buffer.writeInt(type.spray);
		buffer.writeInt(type.split);
		buffer.writeFloat(type.splitSpeed);
		buffer.writeInt(type.splitAmount);
		buffer.writeFloat(type.knockback);
		buffer.writeFloat(type.drag);
		buffer.writeFloat(type.velocityMultiplier);
		buffer.writeFloat(type.gravityMultiplier);
		buffer.writeFloat(type.soundPitch);
		buffer.writeBoolean(type.sticky);
		buffer.writeInt(type.angle);
		buffer.writeFloat(type.spin);
		buffer.writeUtf(type.renderType);
		buffer.writeBoolean(!type.splitInto.isEmpty());
		if (!type.splitInto.isEmpty())
			for (SplitProperty property : type.splitInto) {
				toBuffer(property.type(), buffer);
				buffer.writeFloat(property.chance());
			}
	}

	public static PotatoCannonProjectileType fromBuffer(FriendlyByteBuf buffer) {
		PotatoCannonProjectileType type = new PotatoCannonProjectileType();

		int sizeA = buffer.readVarInt();
		for (int i = 0; i < sizeA; i++) {
			Item item = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
			if (item != null) {
				type.items.add(item.delegate);
			}
		}
		int sizeB = buffer.readVarInt();
		for (int i = 0; i < sizeB; i++) {
			MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(buffer.readResourceLocation());
			int level = buffer.readInt();
			int seconds = buffer.readInt();
			if (mobEffect != null) {
				type.effects.add(new ProjectileEffect(mobEffect, level, seconds));
			}
		}
		type.fireTimer = buffer.readInt();
		type.iceTimer = buffer.readInt();
		type.recoverable = buffer.readBoolean();

		type.onEntityHit = BuiltinPotatoProjectileTypes.getEffects(type.effects, type.fireTimer, type.iceTimer, type.recoverable);

		type.reloadTicks = buffer.readInt();
		type.damage = buffer.readInt();
		type.spray = buffer.readInt();
		type.split = buffer.readInt();
		type.splitSpeed = buffer.readFloat();
		type.splitAmount = buffer.readInt();
		type.knockback = buffer.readFloat();
		type.drag = buffer.readFloat();
		type.velocityMultiplier = buffer.readFloat();
		type.gravityMultiplier = buffer.readFloat();
		type.soundPitch = buffer.readFloat();
		type.sticky = buffer.readBoolean();
		type.angle = buffer.readInt();
		type.spin = buffer.readFloat();
		type.renderType = buffer.readUtf();

		if (buffer.readBoolean()) {
			type.splitInto.add(new SplitProperty(fromBuffer(buffer), buffer.readFloat()));
		}


		return type;
	}

	@Override
	public String toString() {
		return "PotatoCannonProjectileType{" +
				"items=" + items.get(0).get().getDescriptionId() +
				", effects=" + effects +
				", reloadTicks=" + reloadTicks +
				", damage=" + damage +
				", spray=" + spray +
				", split=" + split +
				", splitInto=" + splitInto +
				", splitSpeed=" + splitSpeed +
				", splitAmount=" + splitAmount +
				", knockback=" + knockback +
				", drag=" + drag +
				", velocityMultiplier=" + velocityMultiplier +
				", gravityMultiplier=" + gravityMultiplier +
				", soundPitch=" + soundPitch +
				", sticky=" + sticky +
				", renderMode=" + renderMode +
				", renderType='" + renderType + '\'' +
				", angle=" + angle +
				", spin=" + spin +
				", fireTimer=" + fireTimer +
				", iceTimer=" + iceTimer +
				", recoverable=" + recoverable +
				", preEntityHit=" + preEntityHit +
				", onEntityHit=" + onEntityHit +
				", onBlockHit=" + onBlockHit +
				'}';
	}

	public static class Builder {

		protected ResourceLocation id;
		protected PotatoCannonProjectileType result;

		public Builder(ResourceLocation id) {
			this.id = id;
			this.result = new PotatoCannonProjectileType();
		}

		public Builder reloadTicks(int reload) {
			result.reloadTicks = reload;
			return this;
		}

		public Builder damage(int damage) {
			result.damage = damage;
			return this;
		}

		public Builder spray(int spray) {
			result.spray = spray;
			return this;
		}

		public Builder split(int split) {
			result.split = split;
			return this;
		}

		public Builder splitSpeed(float splitSpeed) {
			result.splitSpeed = splitSpeed;
			return this;
		}

		public Builder splitAmount(int splitAmount) {
			result.splitAmount = splitAmount;
			return this;
		}

		public Builder splitOnly(Item item) {
			result.itemOverride = item.delegate;
			return this;
		}

		public Builder splitInto(SplitProperty... splitInto) {
			result.splitInto.addAll(List.of(splitInto));
			return this;
		}

		public Builder knockback(float knockback) {
			result.knockback = knockback;
			return this;
		}

		public Builder drag(float drag) {
			result.drag = drag;
			return this;
		}

		public Builder velocity(float velocity) {
			result.velocityMultiplier = velocity;
			return this;
		}

		public Builder gravity(float modifier) {
			result.gravityMultiplier = modifier;
			return this;
		}

		public Builder soundPitch(float pitch) {
			result.soundPitch = pitch;
			return this;
		}

		public Builder sticky() {
			result.sticky = true;
			return this;
		}

		public Builder renderMode(PotatoProjectileRenderMode renderMode) {
			result.renderMode = renderMode;
			return this;
		}

		public Builder renderBillboard() {
			renderMode(PotatoProjectileRenderMode.Billboard.INSTANCE);
			return this;
		}

		public Builder renderTumbling() {
			renderMode(PotatoProjectileRenderMode.Tumble.INSTANCE);
			return this;
		}

		public Builder renderTowardMotion(int spriteAngle, float spin) {
			renderMode(new PotatoProjectileRenderMode.TowardMotion(spriteAngle, spin));
			return this;
		}

		public Builder preEntityHit(Predicate<EntityHitResult> callback) {
			result.preEntityHit = callback;
			return this;
		}

		public Builder onEntityHit(Predicate<EntityHitResult> callback) {
			result.onEntityHit = callback;
			return this;
		}

		public Builder onBlockHit(BiPredicate<LevelAccessor, BlockHitResult> callback) {
			result.onBlockHit = callback;
			return this;
		}

		public Builder addItems(ItemLike... items) {
			for (ItemLike provider : items)
				result.items.add(provider.asItem().delegate);
			return this;
		}

		public PotatoCannonProjectileType register() {
			PotatoProjectileTypeManager.registerBuiltinType(id, result);
			return result;
		}

		public PotatoCannonProjectileType registerAndAssign(ItemLike... items) {
			addItems(items);
			register();
			return result;
		}

	}

}
