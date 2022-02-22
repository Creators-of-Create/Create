package com.simibubi.create.content.contraptions.components.fan;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class FanInstance extends KineticTileInstance<EncasedFanTileEntity> {

    protected final RotatingData shaft;
    protected final RotatingData fan;
    final Direction direction;
    private final Direction opposite;

    public FanInstance(MaterialManager modelManager, EncasedFanTileEntity tile) {
		super(modelManager, tile);

		direction = blockState.getValue(FACING);

		opposite = direction.getOpposite();
		shaft = getRotatingMaterial().getModel(AllBlockPartials.SHAFT_HALF, blockState, opposite).createInstance();
		fan = modelManager.defaultCutout()
				.material(AllMaterialSpecs.ROTATING)
				.getModel(AllBlockPartials.ENCASED_FAN_INNER, blockState, opposite)
				.createInstance();

		setup(shaft);
		setup(fan, getFanSpeed());
	}

    private float getFanSpeed() {
        float speed = blockEntity.getSpeed() * 5;
        if (speed > 0)
            speed = Mth.clamp(speed, 80, 64 * 20);
        if (speed < 0)
            speed = Mth.clamp(speed, -64 * 20, -80);
        return speed;
    }

    @Override
    public void update() {
        updateRotation(shaft);
        updateRotation(fan, getFanSpeed());
    }

    @Override
    public void updateLight() {
        BlockPos behind = pos.relative(opposite);
        relight(behind, shaft);

        BlockPos inFront = pos.relative(direction);
        relight(inFront, fan);
    }

    @Override
    public void remove() {
        shaft.delete();
        fan.delete();
    }
}
