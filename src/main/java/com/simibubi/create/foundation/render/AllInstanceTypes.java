package com.simibubi.create.foundation.render;

import static com.simibubi.create.Create.asResource;

import org.lwjgl.system.MemoryUtil;

import com.simibubi.create.content.contraptions.actors.ActorInstance;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.belt.BeltInstance;
import com.simibubi.create.content.logistics.flwdata.FlapInstance;
import com.simibubi.create.content.processing.burner.ScrollInstance;

import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.IntegerRepr;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType;
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllInstanceTypes {
	public static final InstanceType<RotatingInstance> ROTATING = SimpleInstanceType.builder(RotatingInstance::new)
			.cullShader(asResource("instance/cull/rotating.glsl"))
			.vertexShader(asResource("instance/rotating.vert"))
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("pos", FloatRepr.FLOAT, 3)
					.scalar("speed", FloatRepr.FLOAT)
					.scalar("offset", FloatRepr.FLOAT)
					.vector("axis", FloatRepr.NORMALIZED_BYTE, 3)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.r);
				MemoryUtil.memPutByte(ptr + 1, instance.g);
				MemoryUtil.memPutByte(ptr + 2, instance.b);
				MemoryUtil.memPutByte(ptr + 3, instance.a);
				ExtraMemoryOps.put2x16(ptr + 4, instance.light);
				ExtraMemoryOps.put2x16(ptr + 8, instance.overlay);
				MemoryUtil.memPutFloat(ptr + 12, instance.x);
				MemoryUtil.memPutFloat(ptr + 16, instance.y);
				MemoryUtil.memPutFloat(ptr + 20, instance.z);
				MemoryUtil.memPutFloat(ptr + 24, instance.rotationalSpeed);
				MemoryUtil.memPutFloat(ptr + 28, instance.rotationOffset);
				MemoryUtil.memPutByte(ptr + 32, instance.rotationAxisX);
				MemoryUtil.memPutByte(ptr + 33, instance.rotationAxisY);
				MemoryUtil.memPutByte(ptr + 34, instance.rotationAxisZ);
			})
			.register();

	public static final InstanceType<BeltInstance> BELT = SimpleInstanceType.builder(BeltInstance::new)
			.cullShader(asResource("instance/cull/belt.glsl"))
			.vertexShader(asResource("instance/belt.vert"))
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("pos", FloatRepr.FLOAT, 3)
					.scalar("speed", FloatRepr.FLOAT)
					.scalar("offset", FloatRepr.FLOAT)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.vector("sourceTexture", FloatRepr.FLOAT, 2)
					.vector("scrollTexture", FloatRepr.FLOAT, 4)
					.scalar("scrollMult", FloatRepr.FLOAT)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.r);
				MemoryUtil.memPutByte(ptr + 1, instance.g);
				MemoryUtil.memPutByte(ptr + 2, instance.b);
				MemoryUtil.memPutByte(ptr + 3, instance.a);
				ExtraMemoryOps.put2x16(ptr + 4, instance.light);
				ExtraMemoryOps.put2x16(ptr + 8, instance.overlay);
				MemoryUtil.memPutFloat(ptr + 12, instance.x);
				MemoryUtil.memPutFloat(ptr + 16, instance.y);
				MemoryUtil.memPutFloat(ptr + 20, instance.z);
				MemoryUtil.memPutFloat(ptr + 24, instance.rotationalSpeed);
				MemoryUtil.memPutFloat(ptr + 28, instance.rotationOffset);
				ExtraMemoryOps.putQuaternionf(ptr + 32, instance.rotation);
				MemoryUtil.memPutFloat(ptr + 48, instance.sourceU);
				MemoryUtil.memPutFloat(ptr + 52, instance.sourceV);
				MemoryUtil.memPutFloat(ptr + 56, instance.minU);
				MemoryUtil.memPutFloat(ptr + 60, instance.minV);
				MemoryUtil.memPutFloat(ptr + 64, instance.maxU);
				MemoryUtil.memPutFloat(ptr + 68, instance.maxV);
				MemoryUtil.memPutFloat(ptr + 72, instance.scrollMult);
			})
			.register();

	// TODO: use this for belts too
	public static final InstanceType<ScrollInstance> SCROLLING = SimpleInstanceType.builder(ScrollInstance::new)
			.cullShader(asResource("instance/cull/scrolling.glsl"))
			.vertexShader(asResource("instance/scrolling.vert"))
			.layout(LayoutBuilder.create()
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("overlay", IntegerRepr.SHORT, 2)
					.vector("pos", FloatRepr.FLOAT, 3)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.vector("speed", FloatRepr.FLOAT, 2)
					.vector("diff", FloatRepr.FLOAT, 2)
					.vector("scale", FloatRepr.FLOAT, 2)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutByte(ptr, instance.r);
				MemoryUtil.memPutByte(ptr + 1, instance.g);
				MemoryUtil.memPutByte(ptr + 2, instance.b);
				MemoryUtil.memPutByte(ptr + 3, instance.a);
				ExtraMemoryOps.put2x16(ptr + 4, instance.light);
				ExtraMemoryOps.put2x16(ptr + 8, instance.overlay);
				MemoryUtil.memPutFloat(ptr + 12, instance.x);
				MemoryUtil.memPutFloat(ptr + 16, instance.y);
				MemoryUtil.memPutFloat(ptr + 20, instance.z);
				ExtraMemoryOps.putQuaternionf(ptr + 24, instance.rotation);
				MemoryUtil.memPutFloat(ptr + 40, instance.speedU);
				MemoryUtil.memPutFloat(ptr + 44, instance.speedV);
				MemoryUtil.memPutFloat(ptr + 48, instance.diffU);
				MemoryUtil.memPutFloat(ptr + 52, instance.diffV);
				MemoryUtil.memPutFloat(ptr + 56, instance.scaleU);
				MemoryUtil.memPutFloat(ptr + 60, instance.scaleV);
			})
			.register();

	public static final InstanceType<ActorInstance> ACTOR = SimpleInstanceType.builder(ActorInstance::new)
			.cullShader(asResource("instance/cull/actor.glsl"))
			.vertexShader(asResource("instance/actor.vert"))
			.layout(LayoutBuilder.create()
					.vector("pos", FloatRepr.FLOAT, 3)
					.vector("light", IntegerRepr.SHORT, 2)
					.scalar("offset", FloatRepr.FLOAT)
					.vector("axis", FloatRepr.NORMALIZED_BYTE, 3)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.vector("rotationCenter", FloatRepr.NORMALIZED_BYTE, 3)
					.scalar("speed", FloatRepr.FLOAT)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutFloat(ptr, instance.x);
				MemoryUtil.memPutFloat(ptr + 4, instance.y);
				MemoryUtil.memPutFloat(ptr + 8, instance.z);
				MemoryUtil.memPutShort(ptr + 12, instance.blockLight);
				MemoryUtil.memPutShort(ptr + 14, instance.skyLight);
				MemoryUtil.memPutFloat(ptr + 16, instance.rotationOffset);
				MemoryUtil.memPutByte(ptr + 20, instance.rotationAxisX);
				MemoryUtil.memPutByte(ptr + 21, instance.rotationAxisY);
				MemoryUtil.memPutByte(ptr + 22, instance.rotationAxisZ);
				ExtraMemoryOps.putQuaternionf(ptr + 24, instance.rotation);
				MemoryUtil.memPutByte(ptr + 40, instance.rotationCenterX);
				MemoryUtil.memPutByte(ptr + 41, instance.rotationCenterY);
				MemoryUtil.memPutByte(ptr + 42, instance.rotationCenterZ);
				MemoryUtil.memPutFloat(ptr + 44, instance.speed);
			})
			.register();

	// TODO: remove
	public static final InstanceType<FlapInstance> FLAP = SimpleInstanceType.builder(FlapInstance::new)
			.cullShader(asResource("instance/cull/flap.glsl"))
			.vertexShader(asResource("instance/flap.vert"))
			.layout(LayoutBuilder.create()
					.vector("instancePos", FloatRepr.FLOAT, 3)
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("segmentOffset", FloatRepr.FLOAT, 3)
					.vector("pivot", FloatRepr.FLOAT, 3)
					.scalar("horizontalAngle", FloatRepr.FLOAT)
					.scalar("intensity", FloatRepr.FLOAT)
					.scalar("flapScale", FloatRepr.FLOAT)
					.scalar("flapness", FloatRepr.FLOAT)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutFloat(ptr, instance.x);
				MemoryUtil.memPutFloat(ptr + 4, instance.y);
				MemoryUtil.memPutFloat(ptr + 8, instance.z);
				ExtraMemoryOps.put2x16(ptr + 12, instance.packedLight);
				MemoryUtil.memPutFloat(ptr + 16, instance.segmentOffsetX);
				MemoryUtil.memPutFloat(ptr + 20, instance.segmentOffsetY);
				MemoryUtil.memPutFloat(ptr + 24, instance.segmentOffsetZ);
				MemoryUtil.memPutFloat(ptr + 28, instance.pivotX);
				MemoryUtil.memPutFloat(ptr + 32, instance.pivotY);
				MemoryUtil.memPutFloat(ptr + 36, instance.pivotZ);
				MemoryUtil.memPutFloat(ptr + 40, instance.horizontalAngle);
				MemoryUtil.memPutFloat(ptr + 44, instance.intensity);
				MemoryUtil.memPutFloat(ptr + 48, instance.flapScale);
				MemoryUtil.memPutFloat(ptr + 52, instance.flapness);
			})
			.register();

	public static void init() {
		// noop
	}
}
