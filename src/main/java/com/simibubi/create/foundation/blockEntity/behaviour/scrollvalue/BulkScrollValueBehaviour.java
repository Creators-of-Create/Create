package com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue;

import java.util.List;
import java.util.function.Function;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.minecraft.network.chat.Component;

public class BulkScrollValueBehaviour extends ScrollValueBehaviour {

	Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter;

	public BulkScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot,
									Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
		super(label, be, slot);
		this.groupGetter = groupGetter;
	}

	List<? extends SmartBlockEntity> getBulk() {
		return groupGetter.apply(blockEntity);
	}

}
