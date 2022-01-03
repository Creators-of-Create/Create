package com.simibubi.create.content.contraptions.fluids;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PumpCogInstance extends SingleRotatingInstance implements DynamicInstance {

	private final PumpTileEntity blockEntity = (PumpTileEntity) super.blockEntity;
	private final ModelData[] arrows = new ModelData[2];
	private final Direction direction = blockState.getValue(PumpBlock.FACING);

    public PumpCogInstance(MaterialManager modelManager, PumpTileEntity tile) {
        super(modelManager, tile);
    }

	@Override
	public void init() {
		super.init();

		materialManager.defaultSolid()
				.material(Materials.TRANSFORMED)
				.getModel(AllBlockPartials.MECHANICAL_PUMP_ARROW, blockState)
				.createInstances(arrows);
	}

	@Override
	public void beginFrame() {
		float angle = Mth.lerp(blockEntity.arrowDirection.getValue(AnimationTickHolder.getPartialTicks()), 0, 90) - 90;
		for (int i = 0, arrowsLength = arrows.length; i < arrowsLength; i++) {
			arrows[i].loadIdentity()
					.translate(getInstancePosition())
					.centre()
					.rotateY(AngleHelper.horizontalAngle(direction) + 180)
					.rotateX(-AngleHelper.verticalAngle(direction) - 90)
					.unCentre()
					.translate(.5, 14 / 16f, .5)
					.rotateY(90 * i)
					.rotateZ(angle)
					.translateBack(.5, 14 / 16f, .5);
		}
	}

	@Override
	public void updateLight() {
		super.updateLight();
		relight(pos, arrows);
	}

	@Override
    protected Instancer<RotatingData> getModel() {
		BlockState referenceState = blockEntity.getBlockState();
		Direction facing = referenceState.getValue(BlockStateProperties.FACING);
		return getRotatingMaterial().getModel(AllBlockPartials.MECHANICAL_PUMP_COG, referenceState, facing);
	}

	@Override
	public void remove() {
		super.remove();

		for (ModelData arrow : arrows) {
			arrow.delete();
		}
	}
}
