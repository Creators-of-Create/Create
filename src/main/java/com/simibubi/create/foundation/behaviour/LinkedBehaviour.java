package com.simibubi.create.foundation.behaviour;

import com.simibubi.create.foundation.behaviour.base.IBehaviourType;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;

public class LinkedBehaviour extends TileEntityBehaviour {

	public static IBehaviourType<LinkedBehaviour> TYPE = new IBehaviourType<LinkedBehaviour>() {
	};

	public LinkedBehaviour(SmartTileEntity te) {
		super(te);
	}

	@Override
	public IBehaviourType<?> getType() {
		return TYPE;
	}

}
