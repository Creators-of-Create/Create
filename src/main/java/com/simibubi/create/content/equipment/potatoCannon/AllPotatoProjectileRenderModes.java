package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class AllPotatoProjectileRenderModes {
	
	static {
		register("billboard", Billboard.CODEC);
		register("tumble", Tumble.CODEC);
		register("toward_motion", TowardMotion.CODEC);
		register("stuck_to_entity", StuckToEntity.CODEC);
	}
	
	public static void init() {
	}

	private static void register(String name, MapCodec<? extends PotatoProjectileRenderMode> codec) {
		Registry.register(CreateBuiltInRegistries.POTATO_PROJECTILE_RENDER_MODE, Create.asResource(name), codec);
	}

	public enum Billboard implements PotatoProjectileRenderMode {
		INSTANCE;

		public static final MapCodec<Billboard> CODEC = MapCodec.unit(INSTANCE);

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			Minecraft mc = Minecraft.getInstance();
			Vec3 p1 = mc.getCameraEntity()
				.getEyePosition(pt);
			Vec3 diff = entity.getBoundingBox()
				.getCenter()
				.subtract(p1);

			TransformStack.of(ms)
				.rotateYDegrees(AngleHelper.deg(Mth.atan2(diff.x, diff.z)) + 180)
				.rotateXDegrees(AngleHelper.deg(Mth.atan2(diff.y, Mth.sqrt((float) (diff.x * diff.x + diff.z * diff.z)))));
		}

		@Override
		public MapCodec<? extends PotatoProjectileRenderMode> codec() {
			return CODEC;
		}
	}

	public enum Tumble implements PotatoProjectileRenderMode {
		INSTANCE;

		public static final MapCodec<Tumble> CODEC = MapCodec.unit(INSTANCE);

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			Billboard.INSTANCE.transform(ms, entity, pt);
			TransformStack.of(ms)
				.rotateZDegrees((entity.tickCount + pt) * 2 * entityRandom(entity, 16))
				.rotateXDegrees((entity.tickCount + pt) * entityRandom(entity, 32));
		}

		@Override
		public MapCodec<? extends PotatoProjectileRenderMode> codec() {
			return CODEC;
		}
	}

	public record TowardMotion(int spriteAngleOffset, float spin) implements PotatoProjectileRenderMode {
		public static final MapCodec<TowardMotion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.fieldOf("sprite_angle_offset").forGetter(i -> i.spriteAngleOffset),
			Codec.FLOAT.fieldOf("spin").forGetter(i -> i.spin)
		).apply(instance, TowardMotion::new));

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			Vec3 diff = entity.getDeltaMovement();
			TransformStack.of(ms)
				.rotateYDegrees(AngleHelper.deg(Mth.atan2(diff.x, diff.z)))
				.rotateXDegrees(270
					+ AngleHelper.deg(Mth.atan2(diff.y, -Mth.sqrt((float) (diff.x * diff.x + diff.z * diff.z)))));
			TransformStack.of(ms)
				.rotateYDegrees((entity.tickCount + pt) * 20 * spin + entityRandom(entity, 360))
				.rotateZDegrees(-spriteAngleOffset);
		}

		@Override
		public MapCodec<? extends PotatoProjectileRenderMode> codec() {
			return CODEC;
		}
	}

	public record StuckToEntity(Vec3 offset) implements PotatoProjectileRenderMode {
		public static final MapCodec<StuckToEntity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Vec3.CODEC.fieldOf("offset").forGetter(i -> i.offset)
		).apply(instance, StuckToEntity::new));

		@Override
		@OnlyIn(Dist.CLIENT)
		public void transform(PoseStack ms, PotatoProjectileEntity entity, float pt) {
			TransformStack.of(ms).rotateYDegrees(AngleHelper.deg(Mth.atan2(offset.x, offset.z)));
		}

		@Override
		public MapCodec<? extends PotatoProjectileRenderMode> codec() {
			return CODEC;
		}
	}

	private static int entityRandom(Entity entity, int maxValue) {
		return (System.identityHashCode(entity) * 31) % maxValue;
	}
}
