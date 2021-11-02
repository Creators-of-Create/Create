package com.simibubi.create.content.curiosities.weapons;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;

public class PotatoCannonProjectileType {

	private Set<IRegistryDelegate<Item>> items = new HashSet<>();

	private int reloadTicks = 10;
	private int damage = 1;
	private int split = 1;
	private float knockback = 1;
	private float drag = 0.99f;
	private float velocityMultiplier = 1;
	private float gravityMultiplier = 1;
	private float soundPitch = 1;
	private boolean sticky = false;
	private PotatoProjectileRenderMode renderMode = PotatoProjectileRenderMode.Billboard.INSTANCE;

	private Predicate<EntityRayTraceResult> preEntityHit = e -> false; // True if hit should be canceled
	private Predicate<EntityRayTraceResult> onEntityHit = e -> false; // True if shouldn't recover projectile
	private BiPredicate<IWorld, BlockRayTraceResult> onBlockHit = (w, ray) -> false;

	protected PotatoCannonProjectileType() {
	}

	public Set<IRegistryDelegate<Item>> getItems() {
		return items;
	}

	public int getReloadTicks() {
		return reloadTicks;
	}

	public int getDamage() {
		return damage;
	}

	public int getSplit() {
		return split;
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
		return renderMode;
	}

	public boolean preEntityHit(EntityRayTraceResult ray) {
		return preEntityHit.test(ray);
	}
	
	public boolean onEntityHit(EntityRayTraceResult ray) {
		return onEntityHit.test(ray);
	}

	public boolean onBlockHit(IWorld world, BlockRayTraceResult ray) {
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
			parseJsonPrimitive(object, "damage", JsonPrimitive::isNumber, primitive -> type.damage = primitive.getAsInt());
			parseJsonPrimitive(object, "split", JsonPrimitive::isNumber, primitive -> type.split = primitive.getAsInt());
			parseJsonPrimitive(object, "knockback", JsonPrimitive::isNumber, primitive -> type.knockback = primitive.getAsFloat());
			parseJsonPrimitive(object, "drag", JsonPrimitive::isNumber, primitive -> type.drag = primitive.getAsFloat());
			parseJsonPrimitive(object, "velocity_multiplier", JsonPrimitive::isNumber, primitive -> type.velocityMultiplier = primitive.getAsFloat());
			parseJsonPrimitive(object, "gravity_multiplier", JsonPrimitive::isNumber, primitive -> type.gravityMultiplier = primitive.getAsFloat());
			parseJsonPrimitive(object, "sound_pitch", JsonPrimitive::isNumber, primitive -> type.soundPitch = primitive.getAsFloat());
			parseJsonPrimitive(object, "sticky", JsonPrimitive::isBoolean, primitive -> type.sticky = primitive.getAsBoolean());
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

	public static void toBuffer(PotatoCannonProjectileType type, PacketBuffer buffer) {
		buffer.writeVarInt(type.items.size());
		for (IRegistryDelegate<Item> delegate : type.items) {
			buffer.writeResourceLocation(delegate.name());
		}
		buffer.writeInt(type.reloadTicks);
		buffer.writeInt(type.damage);
		buffer.writeInt(type.split);
		buffer.writeFloat(type.knockback);
		buffer.writeFloat(type.drag);
		buffer.writeFloat(type.velocityMultiplier);
		buffer.writeFloat(type.gravityMultiplier);
		buffer.writeFloat(type.soundPitch);
		buffer.writeBoolean(type.sticky);
	}

	public static PotatoCannonProjectileType fromBuffer(PacketBuffer buffer) {
		PotatoCannonProjectileType type = new PotatoCannonProjectileType();
		int size = buffer.readVarInt();
		for (int i = 0; i < size; i++) {
			Item item = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
			if (item != null) {
				type.items.add(item.delegate);
			}
		}
		type.reloadTicks = buffer.readInt();
		type.damage = buffer.readInt();
		type.split = buffer.readInt();
		type.knockback = buffer.readFloat();
		type.drag = buffer.readFloat();
		type.velocityMultiplier = buffer.readFloat();
		type.gravityMultiplier = buffer.readFloat();
		type.soundPitch = buffer.readFloat();
		type.sticky = buffer.readBoolean();
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

		public Builder damage(int damage) {
			result.damage = damage;
			return this;
		}

		public Builder splitInto(int split) {
			result.split = split;
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

		public Builder preEntityHit(Predicate<EntityRayTraceResult> callback) {
			result.preEntityHit = callback;
			return this;
		}

		public Builder onEntityHit(Predicate<EntityRayTraceResult> callback) {
			result.onEntityHit = callback;
			return this;
		}

		public Builder onBlockHit(BiPredicate<IWorld, BlockRayTraceResult> callback) {
			result.onBlockHit = callback;
			return this;
		}

		public Builder addItems(IItemProvider... items) {
			for (IItemProvider provider : items)
				result.items.add(provider.asItem().delegate);
			return this;
		}

		public PotatoCannonProjectileType register() {
			PotatoProjectileTypeManager.registerBuiltinType(id, result);
			return result;
		}

		public PotatoCannonProjectileType registerAndAssign(IItemProvider... items) {
			addItems(items);
			register();
			return result;
		}

	}

}
