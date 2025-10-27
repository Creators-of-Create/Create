package com.simibubi.create.content.kinetics.chainConveyor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.render.SpecialModels;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.engine_room.flywheel.lib.visual.util.SmartRecycler;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class ChainConveyorVisual extends SingleAxisRotatingVisual<ChainConveyorBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

	private final List<TransformedInstance> guards = new ArrayList<>();

	private final SmartRecycler<ResourceLocation, TransformedInstance> boxes;
	private final SmartRecycler<ResourceLocation, TransformedInstance> rigging;

	public ChainConveyorVisual(VisualizationContext context, ChainConveyorBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick, Models.partial(AllPartialModels.CHAIN_CONVEYOR_SHAFT));

		setupGuards();

		boxes = new SmartRecycler<>(key -> instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.PACKAGES.get(key))).createInstance());
		rigging = new SmartRecycler<>(key -> instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.PACKAGE_RIGGING.get(key))).createInstance());
	}

	@Override
	public void update(float pt) {
		super.update(pt);

		setupGuards();
	}

	@Override
	public void tick(TickableVisual.Context context) {
		blockEntity.tickBoxVisuals();
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		var partialTicks = ctx.partialTick();

		boxes.resetCount();
		rigging.resetCount();

		for (ChainConveyorPackage box : blockEntity.loopingPackages)
			setupBoxVisual(blockEntity, box, partialTicks);

		for (Map.Entry<BlockPos, List<ChainConveyorPackage>> entry : blockEntity.travellingPackages.entrySet())
			for (ChainConveyorPackage box : entry.getValue())
				setupBoxVisual(blockEntity, box, partialTicks);

		boxes.discardExtra();
		rigging.discardExtra();
	}


	private void setupBoxVisual(ChainConveyorBlockEntity be, ChainConveyorPackage box, float partialTicks) {
		if (box.worldPosition == null)
			return;
		if (box.item == null || box.item.isEmpty())
			return;

		ChainConveyorPackage.ChainConveyorPackagePhysicsData physicsData = box.physicsData(be.getLevel());
		if (physicsData.prevPos == null)
			return;

		Vec3 position = physicsData.prevPos.lerp(physicsData.pos, partialTicks);
		Vec3 targetPosition = physicsData.prevTargetPos.lerp(physicsData.targetPos, partialTicks);
		float yaw = AngleHelper.angleLerp(partialTicks, physicsData.prevYaw, physicsData.yaw);
		Vec3 offset =
			new Vec3(targetPosition.x - this.pos.getX(), targetPosition.y - this.pos.getY(), targetPosition.z - this.pos.getZ());

		BlockPos containingPos = BlockPos.containing(position);
		Level level = be.getLevel();
		int light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, containingPos),
			level.getBrightness(LightLayer.SKY, containingPos));

		if (physicsData.modelKey == null) {
			ResourceLocation key = BuiltInRegistries.ITEM.getKey(box.item.getItem());
			if (key == BuiltInRegistries.ITEM.getDefaultKey())
				return;
			physicsData.modelKey = key;
		}

		TransformedInstance rigBuffer = rigging.get(physicsData.modelKey);
		TransformedInstance boxBuffer = boxes.get(physicsData.modelKey);

		Vec3 dangleDiff = VecHelper.rotate(targetPosition.add(0, 0.5, 0)
			.subtract(position), -yaw, Direction.Axis.Y);
		float zRot = Mth.wrapDegrees((float) Mth.atan2(-dangleDiff.x, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
		float xRot = Mth.wrapDegrees((float) Mth.atan2(dangleDiff.z, dangleDiff.y) * Mth.RAD_TO_DEG) / 2;
		zRot = Mth.clamp(zRot, -25, 25);
		xRot = Mth.clamp(xRot, -25, 25);

		for (TransformedInstance buf : new TransformedInstance[] { rigBuffer, boxBuffer }) {
			buf.setIdentityTransform();
			buf.translate(getVisualPosition());
			buf.translate(offset);
			buf.translate(0, 10 / 16f, 0);
			buf.rotateYDegrees(yaw);

			buf.rotateZDegrees(zRot);
			buf.rotateXDegrees(xRot);

			if (physicsData.flipped && buf == rigBuffer)
				buf.rotateYDegrees(180);

			buf.uncenter();
			buf.translate(0, -PackageItem.getHookDistance(box.item) + 7 / 16f, 0);

			buf.light(light);

			buf.setChanged();
		}
	}

	private void deleteGuards() {
		for (TransformedInstance guard : guards) {
			guard.delete();
		}
		guards.clear();
	}

	private void setupGuards() {
		deleteGuards();

		var wheelInstancer = instancerProvider().instancer(InstanceTypes.TRANSFORMED, SpecialModels.chunkDiffuse(AllPartialModels.CHAIN_CONVEYOR_WHEEL));
		var guardInstancer = instancerProvider().instancer(InstanceTypes.TRANSFORMED, SpecialModels.chunkDiffuse(AllPartialModels.CHAIN_CONVEYOR_GUARD));

		TransformedInstance wheel = wheelInstancer.createInstance();
		
		wheel.translate(getVisualPosition())
			.light(rotatingModel.light)
			.setChanged();
		
		guards.add(wheel);
		
		for (BlockPos blockPos : blockEntity.connections) {
			ChainConveyorBlockEntity.ConnectionStats stats = blockEntity.connectionStats.get(blockPos);
			if (stats == null) {
				continue;
			}

			Vec3 diff = stats.end()
				.subtract(stats.start());
			double yaw = Mth.RAD_TO_DEG * Mth.atan2(diff.x, diff.z);

			TransformedInstance guard = guardInstancer.createInstance();
			guard.translate(getVisualPosition())
				.center()
				.rotateYDegrees((float) yaw)
				.uncenter()
				.light(rotatingModel.light)
				.setChanged();

			guards.add(guard);
		}
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);
		for (TransformedInstance guard : guards) {
			relight(guard);
		}
	}

	@Override
	protected void _delete() {
		super._delete();
		deleteGuards();
		boxes.delete();
		rigging.delete();
	}
}
