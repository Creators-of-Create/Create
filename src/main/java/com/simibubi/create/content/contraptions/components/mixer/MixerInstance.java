package com.simibubi.create.content.contraptions.components.mixer;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.ShaftlessCogInstance;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.OrientedData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Direction;

public class MixerInstance extends ShaftlessCogInstance implements IDynamicInstance {

	private final InstanceKey<RotatingData> mixerHead;
	private final InstanceKey<OrientedData> mixerPole;
	private final MechanicalMixerTileEntity mixer;

	public MixerInstance(InstancedTileRenderer<?> dispatcher, MechanicalMixerTileEntity tile) {
		super(dispatcher, tile);
		this.mixer = tile;

		mixerHead = getRotatingMaterial().getModel(AllBlockPartials.MECHANICAL_MIXER_HEAD, blockState)
				.createInstance();

		mixerHead.getInstance()
				.setRotationAxis(Direction.Axis.Y);

		mixerPole = getOrientedMaterial()
				.getModel(AllBlockPartials.MECHANICAL_MIXER_POLE, blockState)
				.createInstance();


		float renderedHeadOffset = getRenderedHeadOffset();

		transformPole(renderedHeadOffset);
		transformHead(renderedHeadOffset);
	}

	@Override
	public void beginFrame() {

		float renderedHeadOffset = getRenderedHeadOffset();

		transformPole(renderedHeadOffset);
		transformHead(renderedHeadOffset);
	}

	private void transformHead(float renderedHeadOffset) {
		float speed = mixer.getRenderedHeadRotationSpeed(AnimationTickHolder.getPartialTicks());

		mixerHead.getInstance()
				.setPosition(getInstancePosition())
				.nudge(0, -renderedHeadOffset, 0)
				.setRotationalSpeed(speed * 2);
	}

	private void transformPole(float renderedHeadOffset) {
		mixerPole.getInstance()
				.setPosition(getInstancePosition())
				.nudge(0, -renderedHeadOffset, 0);
	}

	private float getRenderedHeadOffset() {
		return mixer.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
	}

	@Override
	public void updateLight() {
		super.updateLight();

		relight(pos.down(), mixerHead.getInstance());
		relight(pos, mixerPole.getInstance());
	}

	@Override
	public void remove() {
		super.remove();
		mixerHead.delete();
		mixerPole.delete();
	}
}
