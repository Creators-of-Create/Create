package com.simibubi.create.foundation.blockEntity.behaviour.scrollValue;

import java.util.List;
import java.util.function.Function;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class BulkScrollValueBehaviour extends ScrollValueBehaviour {

	Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter;

	public BulkScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot,
		Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
		super(label, be, slot);
		this.groupGetter = groupGetter;
	}

	@Override
	public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
		if (!ctrlDown) {
			super.setValueSettings(player, valueSetting, ctrlDown);
			return;
		}
		if (!valueSetting.equals(getValueSettings()))
			playFeedbackSound(this);
		for (SmartBlockEntity be : getBulk()) {
			ScrollValueBehaviour other = be.getBehaviour(ScrollValueBehaviour.TYPE);
			if (other != null)
				other.setValue(valueSetting.value());
		}
	}

	public List<? extends SmartBlockEntity> getBulk() {
		return groupGetter.apply(blockEntity);
	}

}
