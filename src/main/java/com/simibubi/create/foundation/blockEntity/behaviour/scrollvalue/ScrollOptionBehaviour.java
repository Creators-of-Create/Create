package com.simibubi.create.foundation.blockEntity.behaviour.scrollvalue;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.minecraft.network.chat.Component;

public class ScrollOptionBehaviour<E extends Enum<E> & INamedIconOptions> extends ScrollValueBehaviour {

	private E[] options;

	public ScrollOptionBehaviour(Class<E> enum_, Component label, SmartBlockEntity be, ValueBoxTransform slot) {
		super(label, be, slot);
		options = enum_.getEnumConstants();
		between(0, options.length - 1);
		withStepFunction((c) -> -1);
	}

	INamedIconOptions getIconForSelected() {
		return get();
	}
	
	public E get() {
		return options[scrollableValue];
	}

}
