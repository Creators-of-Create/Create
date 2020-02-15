package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.BearingContraption;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.ClockworkContraption;
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.MountedContraption;
import com.simibubi.create.modules.contraptions.components.contraptions.piston.PistonContraption;
import com.simibubi.create.modules.contraptions.components.contraptions.pulley.PulleyContraption;

public enum AllContraptionTypes {
	
	PISTON(PistonContraption::new),
	BEARING(BearingContraption::new),
	PULLEY(PulleyContraption::new),
	CLOCKWORK(ClockworkContraption::new),
	MOUNTED(MountedContraption::new),
	
	;

	Supplier<? extends Contraption> factory;
	String id;
	
	private AllContraptionTypes(Supplier<? extends Contraption> factory) {
		this.factory = factory;
		id = Lang.asId(name());
	}
	
	public static Contraption fromType(String type) {
		for (AllContraptionTypes allContraptionTypes : values()) {
			if (type.equals(allContraptionTypes.id))
				return allContraptionTypes.factory.get();
		}
		return null;
	}

}
