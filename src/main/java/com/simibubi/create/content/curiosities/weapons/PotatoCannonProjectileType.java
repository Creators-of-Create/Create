package com.simibubi.create.content.curiosities.weapons;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.ResourceLocationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;

public class PotatoCannonProjectileType {

	private Set<IRegistryDelegate<Item>> items = new HashSet<>();

	private int reloadTicks = 10;
	private int maxFire = 1;
	private int damage = 1;
	private int cost = 1;
	private int spray = 1;
	private int piercing = 0;
	private int split = 1; // Currently Unused, will be used for projectile that split when hitting an entity or after some time
	private int splitDuration = 1; // Currently Unused, will be used to determine the time before the projectile split, ignored if "onlySplitOnHit" is true
	private float accuracy = 100;
	private float sprayAccuracy = 100;
	private float knockback = 1;
	private float drag = 0.99f;
	private float velocityMultiplier = 1;
	private float gravityMultiplier = 1;
	private float soundPitch = 1;
	private boolean sticky = false;
	private boolean costOnce = false;
	private boolean onlySplitOnHit = false; // Currently Unused, will be used to determined if it split ONLY when hit
	private boolean onlySplitOnTime = false; // Currently Unused, will be used to determined if it split ONLY after the timer
	private int renderType = 0;
	private int spin_ = 0;
	private int spriteAngle_ = 0;
	private PotatoProjectileRenderMode renderMode = PotatoProjectileRenderMode.Billboard.INSTANCE;

	private Predicate<EntityHitResult> preEntityHit = e -> false; // True if hit should be canceled
	private Predicate<EntityHitResult> onEntityHit = e -> false; // True if shouldn't recover projectile
	private BiPredicate<LevelAccessor, BlockHitResult> onBlockHit = (w, ray) -> false;

	protected PotatoCannonProjectileType() {
	}

	public Set<IRegistryDelegate<Item>> getItems() {
		return items;
	}

	public int getReloadTicks() {
		return reloadTicks;
	}

	public int getMaxFire() {
		return maxFire;
	}

	public int getDamage() {
		return damage;
	}

	public int getPiercing() {
		return piercing;
	}

	public int getCost() {
		return cost;
	}

	public int getSpray() {
		return spray;
	}

	public int getSplit() {
		return split;
	}

	public int getSplitDuration() {
		return splitDuration;
	}

	public float getKnockback() {
		return knockback;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public float getSprayAccuracy() {
		return sprayAccuracy;
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

	public boolean onlyCostOnce() {
		return costOnce;
	}

	public boolean onlySplitOnHit() {
		return onlySplitOnHit;
	}

	public boolean onlySplitOnTime() {
		return onlySplitOnTime;
	}

	public PotatoProjectileRenderMode getRenderMode() {
		return renderMode;
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

			parseJsonPrimitive(object, "reload_ticks", JsonPrimitive::isNumber, primitive -> type.reloadTicks = primitive.getAsInt());
			parseJsonPrimitive(object, "max_fire", JsonPrimitive::isNumber, primitive -> type.maxFire = primitive.getAsInt());
			parseJsonPrimitive(object, "piercing", JsonPrimitive::isNumber, primitive -> type.piercing = primitive.getAsInt());
			parseJsonPrimitive(object, "damage", JsonPrimitive::isNumber, primitive -> type.damage = primitive.getAsInt());
			parseJsonPrimitive(object, "cost", JsonPrimitive::isNumber, primitive -> type.cost = primitive.getAsInt());
			parseJsonPrimitive(object, "spray", JsonPrimitive::isNumber, primitive -> type.spray = primitive.getAsInt());
			parseJsonPrimitive(object, "split", JsonPrimitive::isNumber, primitive -> type.split = primitive.getAsInt());
			parseJsonPrimitive(object, "split_duration", JsonPrimitive::isNumber, primitive -> type.splitDuration = primitive.getAsInt());
			parseJsonPrimitive(object, "accuracy", JsonPrimitive::isNumber, primitive -> type.accuracy = primitive.getAsFloat());
			parseJsonPrimitive(object, "spray_accuracy", JsonPrimitive::isNumber, primitive -> type.sprayAccuracy = primitive.getAsFloat());
			parseJsonPrimitive(object, "knockback", JsonPrimitive::isNumber, primitive -> type.knockback = primitive.getAsFloat());
			parseJsonPrimitive(object, "drag", JsonPrimitive::isNumber, primitive -> type.drag = primitive.getAsFloat());
			parseJsonPrimitive(object, "velocity_multiplier", JsonPrimitive::isNumber, primitive -> type.velocityMultiplier = primitive.getAsFloat());
			parseJsonPrimitive(object, "gravity_multiplier", JsonPrimitive::isNumber, primitive -> type.gravityMultiplier = primitive.getAsFloat());
			parseJsonPrimitive(object, "sound_pitch", JsonPrimitive::isNumber, primitive -> type.soundPitch = primitive.getAsFloat());
			parseJsonPrimitive(object, "sticky", JsonPrimitive::isBoolean, primitive -> type.sticky = primitive.getAsBoolean());
			parseJsonPrimitive(object, "cost_once", JsonPrimitive::isBoolean, primitive -> type.costOnce = primitive.getAsBoolean());
			parseJsonPrimitive(object, "only_split_on_hit", JsonPrimitive::isBoolean, primitive -> type.onlySplitOnHit = primitive.getAsBoolean());
			parseJsonPrimitive(object, "only_split_on_time", JsonPrimitive::isBoolean, primitive -> type.onlySplitOnTime = primitive.getAsBoolean());
			parseJsonPrimitive(object, "render_type", JsonPrimitive::isNumber, primitive -> type.renderType = primitive.getAsInt());
			parseJsonPrimitive(object, "spin", JsonPrimitive::isNumber, primitive -> type.spin_ = primitive.getAsInt());
			parseJsonPrimitive(object, "spriteAngle", JsonPrimitive::isNumber, primitive -> type.spriteAngle_ = primitive.getAsInt());
			switch (type.renderType) {
				default:
					type.renderMode = PotatoProjectileRenderMode.Billboard.INSTANCE;
					break;
				case 1:
					type.renderMode = PotatoProjectileRenderMode.Tumble.INSTANCE;
					break;
				case 2:
					type.renderMode = new PotatoProjectileRenderMode.TowardMotion(type.spriteAngle_, type.spin_);
					break;
			}
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
		for (IRegistryDelegate<Item> delegate : type.items) {
			buffer.writeResourceLocation(delegate.name());
		}
		buffer.writeInt(type.reloadTicks);
		buffer.writeInt(type.maxFire);
		buffer.writeInt(type.piercing);
		buffer.writeInt(type.damage);
		buffer.writeInt(type.cost);
		buffer.writeInt(type.spray);
		buffer.writeInt(type.split);
		buffer.writeInt(type.splitDuration);
		buffer.writeFloat(type.accuracy);
		buffer.writeFloat(type.sprayAccuracy);
		buffer.writeFloat(type.knockback);
		buffer.writeFloat(type.drag);
		buffer.writeFloat(type.velocityMultiplier);
		buffer.writeFloat(type.gravityMultiplier);
		buffer.writeFloat(type.soundPitch);
		buffer.writeBoolean(type.sticky);
		buffer.writeBoolean(type.costOnce);
		buffer.writeBoolean(type.onlySplitOnHit);
		buffer.writeBoolean(type.onlySplitOnTime);
	}

	public static PotatoCannonProjectileType fromBuffer(FriendlyByteBuf buffer) {
		PotatoCannonProjectileType type = new PotatoCannonProjectileType();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			Item item = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
			if (item != null) {
				type.items.add(item.delegate);
			}
		}
		type.reloadTicks = buffer.readInt();
		type.maxFire = buffer.readInt();
		type.piercing = buffer.readInt();
		type.damage = buffer.readInt();
		type.cost = buffer.readInt();
		type.spray = buffer.readInt();
		type.split = buffer.readInt();
		type.splitDuration = buffer.readInt();
		type.accuracy = buffer.readFloat();
		type.sprayAccuracy = buffer.readFloat();
		type.knockback = buffer.readFloat();
		type.drag = buffer.readFloat();
		type.velocityMultiplier = buffer.readFloat();
		type.gravityMultiplier = buffer.readFloat();
		type.soundPitch = buffer.readFloat();
		type.sticky = buffer.readBoolean();
		type.costOnce = buffer.readBoolean();
		type.onlySplitOnHit = buffer.readBoolean();
		type.onlySplitOnTime = buffer.readBoolean();
		return type;
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

		public Builder numberOfShot(int maxFire) {
			result.maxFire = maxFire;
			return this;
		}

		public Builder damage(int damage) {
			result.damage = damage;
			return this;
		}

		public Builder piercing(int piercing) {
			result.piercing = piercing;
			return this;
		}

		public Builder cost(int cost) {
			result.cost = cost;
			return this;
		}

		public Builder sprayInto(int spray) {
			result.spray = spray;
			return this;
		}

		public Builder splitInto(int split) {
			result.split = split;
			return this;
		}

		public Builder splitTimer(int splitDuration) {
			result.splitDuration = splitDuration;
			return this;
		}

		public Builder accuracy(float accuracy) {
			result.accuracy = accuracy;
			return this;
		}

		public Builder sprayAccuracy(float sprayAccuracy) {
			result.sprayAccuracy = sprayAccuracy;
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

		public Builder onlyCostOnce() {
			result.costOnce = true;
			return this;
		}

		public Builder onlySplitOnHit() {
			result.onlySplitOnHit = true;
			return this;
		}

		public Builder onlySplitOnTime() {
			result.onlySplitOnTime = true;
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
