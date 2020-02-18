package com.simibubi.create.modules.contraptions.components.contraptions.chassis;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;

public class ChassisTileEntity extends SmartTileEntity {

	ScrollValueBehaviour range;

	public ChassisTileEntity() {
		super(AllTileEntities.CHASSIS.type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		int max = AllConfigs.SERVER.kinetics.maxChassisRange.get();
		range = new ScrollValueBehaviour(Lang.translate("generic.range"), this, new CenteredSideValueBoxTransform());
		range.requiresWrench();
		range.between(1, max);
		range.value = max / 2;
		behaviours.add(range);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getBlockState().getBlock() instanceof RadialChassisBlock)
			range.setLabel(Lang.translate("generic.radius"));
	}

	public int getRange() {
		return range.getValue();
	}

}
