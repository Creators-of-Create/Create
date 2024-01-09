package com.simibubi.create.foundation.render;

import static com.simibubi.create.Create.asResource;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.FloatRepr;
import com.jozufozu.flywheel.api.layout.IntegerRepr;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.lib.instance.SimpleInstanceType;
import com.simibubi.create.content.contraptions.actors.flwdata.ActorInstance;
import com.simibubi.create.content.kinetics.belt.BeltInstance;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.logistics.flwdata.FlapData;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllInstanceTypes {
	// FIXME: memory alignment issues and light being represented by bytes

	public static final InstanceType<RotatingInstance> ROTATING = SimpleInstanceType.builder(RotatingInstance::new)
			.cullShader(asResource("instance/cull/rotating.glsl"))
			.vertexShader(asResource("instance/rotating.vert"))
			.layout(LayoutBuilder.create()
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("pos", FloatRepr.FLOAT, 3)
					.scalar("speed", FloatRepr.FLOAT)
					.scalar("offset", FloatRepr.FLOAT)
					.vector("axis", FloatRepr.NORMALIZED_BYTE, 3)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutShort(ptr, instance.blockLight);
				MemoryUtil.memPutShort(ptr + 2, instance.skyLight);
				MemoryUtil.memPutByte(ptr + 4, instance.r);
				MemoryUtil.memPutByte(ptr + 5, instance.g);
				MemoryUtil.memPutByte(ptr + 6, instance.b);
				MemoryUtil.memPutByte(ptr + 7, instance.a);
				MemoryUtil.memPutFloat(ptr + 6, instance.x);
				MemoryUtil.memPutFloat(ptr + 10, instance.y);
				MemoryUtil.memPutFloat(ptr + 14, instance.z);
				MemoryUtil.memPutFloat(ptr + 18, instance.rotationalSpeed);
				MemoryUtil.memPutFloat(ptr + 22, instance.rotationOffset);
				MemoryUtil.memPutByte(ptr + 26, instance.rotationAxisX);
				MemoryUtil.memPutByte(ptr + 27, instance.rotationAxisY);
				MemoryUtil.memPutByte(ptr + 28, instance.rotationAxisZ);
			})
			.register();
	public static final InstanceType<BeltInstance> BELTS = SimpleInstanceType.builder(BeltInstance::new)
			.cullShader(asResource("instance/cull/belt.glsl"))
			.vertexShader(asResource("instance/belt.vert"))
			.layout(LayoutBuilder.create()
					.vector("light", IntegerRepr.SHORT, 2)
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("pos", FloatRepr.FLOAT, 3)
					.scalar("speed", FloatRepr.FLOAT)
					.scalar("offset", FloatRepr.FLOAT)
					.vector("rotation", FloatRepr.FLOAT, 4)
					.vector("sourceTexture", FloatRepr.FLOAT, 2)
					.vector("scrollTexture", FloatRepr.FLOAT, 4)
					.scalar("scrollMult", FloatRepr.NORMALIZED_BYTE)
					.build())
			.writer((ptr, instance) -> {
				MemoryUtil.memPutShort(ptr, instance.blockLight);
				MemoryUtil.memPutShort(ptr + 2, instance.skyLight);
				MemoryUtil.memPutByte(ptr + 4, instance.r);
				MemoryUtil.memPutByte(ptr + 5, instance.g);
				MemoryUtil.memPutByte(ptr + 6, instance.b);
				MemoryUtil.memPutByte(ptr + 7, instance.a);
				MemoryUtil.memPutFloat(ptr + 6, instance.x);
				MemoryUtil.memPutFloat(ptr + 10, instance.y);
				MemoryUtil.memPutFloat(ptr + 14, instance.z);
				MemoryUtil.memPutFloat(ptr + 18, instance.rotationalSpeed);
				MemoryUtil.memPutFloat(ptr + 22, instance.rotationOffset);
				MemoryUtil.memPutFloat(ptr + 26, instance.qX);
				MemoryUtil.memPutFloat(ptr + 30, instance.qY);
				MemoryUtil.memPutFloat(ptr + 34, instance.qZ);
				MemoryUtil.memPutFloat(ptr + 38, instance.qW);
				MemoryUtil.memPutFloat(ptr + 42, instance.sourceU);
				MemoryUtil.memPutFloat(ptr + 46, instance.sourceV);
				MemoryUtil.memPutFloat(ptr + 50, instance.minU);
				MemoryUtil.memPutFloat(ptr + 54, instance.minV);
				MemoryUtil.memPutFloat(ptr + 58, instance.maxU);
				MemoryUtil.memPutFloat(ptr + 62, instance.maxV);
				MemoryUtil.memPutByte(ptr + 66, instance.scrollMult);
			})
			.register();
	public static final InstanceType<ActorInstance> ACTORS = SimpleInstanceType.builder(ActorInstance::new)
			.cullShader(asResource("instance/cull/contraption_actor.glsl"))
			.vertexShader(asResource("instance/contraption_actor.vert"))
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
				MemoryUtil.memPutByte(ptr + 12, instance.blockLight);
				MemoryUtil.memPutByte(ptr + 13, instance.skyLight);
				MemoryUtil.memPutFloat(ptr + 14, instance.rotationOffset);
				MemoryUtil.memPutByte(ptr + 18, instance.rotationAxisX);
				MemoryUtil.memPutByte(ptr + 19, instance.rotationAxisY);
				MemoryUtil.memPutByte(ptr + 20, instance.rotationAxisZ);
				MemoryUtil.memPutFloat(ptr + 21, instance.qX);
				MemoryUtil.memPutFloat(ptr + 25, instance.qY);
				MemoryUtil.memPutFloat(ptr + 29, instance.qZ);
				MemoryUtil.memPutFloat(ptr + 33, instance.qW);
				MemoryUtil.memPutByte(ptr + 37, instance.rotationCenterX);
				MemoryUtil.memPutByte(ptr + 38, instance.rotationCenterY);
				MemoryUtil.memPutByte(ptr + 39, instance.rotationCenterZ);
				MemoryUtil.memPutFloat(ptr + 40, instance.speed);
			})
			.register();
	public static final InstanceType<FlapData> FLAPS = SimpleInstanceType.builder(FlapData::new)
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
				MemoryUtil.memPutByte(ptr + 12, (byte) (instance.blockLight << 4));
				MemoryUtil.memPutByte(ptr + 13, (byte) (instance.skyLight << 4));
				MemoryUtil.memPutFloat(ptr + 14, instance.segmentOffsetX);
				MemoryUtil.memPutFloat(ptr + 18, instance.segmentOffsetY);
				MemoryUtil.memPutFloat(ptr + 22, instance.segmentOffsetZ);
				MemoryUtil.memPutFloat(ptr + 26, instance.pivotX);
				MemoryUtil.memPutFloat(ptr + 30, instance.pivotY);
				MemoryUtil.memPutFloat(ptr + 34, instance.pivotZ);
				MemoryUtil.memPutFloat(ptr + 38, instance.horizontalAngle);
				MemoryUtil.memPutFloat(ptr + 42, instance.intensity);
				MemoryUtil.memPutFloat(ptr + 46, instance.flapScale);
				MemoryUtil.memPutFloat(ptr + 50, instance.flapness);
			})
			.register();

	public static void init() {
		// noop
	}
}
