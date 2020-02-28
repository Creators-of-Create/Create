package com.simibubi.create.foundation.behaviour.scrollvalue;

import com.simibubi.create.foundation.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;

public class ScrollOptionBehaviour<E extends Enum<E> & INamedIconOptions> extends ScrollValueBehaviour {

	private E[] options;

	public ScrollOptionBehaviour(Class<E> enum_, String label, SmartTileEntity te, ValueBoxTransform slot) {
		super(label, te, slot);
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
