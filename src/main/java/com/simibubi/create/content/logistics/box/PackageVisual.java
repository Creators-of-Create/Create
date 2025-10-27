package com.simibubi.create.content.logistics.box;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PackageVisual extends AbstractEntityVisual<PackageEntity> implements SimpleDynamicVisual {
	public final TransformedInstance instance;

	public PackageVisual(VisualizationContext ctx, PackageEntity entity, float partialTick) {
		super(ctx, entity, partialTick);

		ItemStack box = entity.box;
		if (box.isEmpty() || !PackageItem.isPackage(box))
			box = AllBlocks.CARDBOARD_BLOCK.asStack();
		PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));

		instance = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(model))
			.createInstance();

		animate(partialTick);
	}

	@Override
	public void beginFrame(Context ctx) {
		animate(ctx.partialTick());
	}

	private void animate(float partialTick) {
		float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());

		Vec3 pos = PackageVisual.this.entity.position();
		var renderOrigin = renderOrigin();
		var x = (float) (Mth.lerp(partialTick, this.entity.xo, pos.x) - renderOrigin.getX());
		var y = (float) (Mth.lerp(partialTick, this.entity.yo, pos.y) - renderOrigin.getY());
		var z = (float) (Mth.lerp(partialTick, this.entity.zo, pos.z) - renderOrigin.getZ());

		long randomBits = (long) entity.getId() * 31L * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;

		instance.setIdentityTransform()
			.translate(x - 0.5 + xNudge, y + yNudge, z - 0.5 + zNudge)
			.rotateYCenteredDegrees(-yaw - 90)
			.light(computePackedLight(partialTick))
			.setChanged();
	}

	@Override
	protected void _delete() {
		instance.delete();
	}
}
