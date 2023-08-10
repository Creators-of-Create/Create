package com.simibubi.create.content.contraptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.ClockworkContraption;
import com.simibubi.create.content.contraptions.bearing.StabilizedContraption;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.gantry.GantryContraption;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.contraptions.piston.PistonContraption;
import com.simibubi.create.content.contraptions.pulley.PulleyContraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;

public class ContraptionType {

	public static final Map<String, ContraptionType> ENTRIES = new HashMap<>();
	public static final ContraptionType
		PISTON = register("piston", PistonContraption::new), 
		BEARING = register("bearing", BearingContraption::new),
		PULLEY = register("pulley", PulleyContraption::new),
		CLOCKWORK = register("clockwork", ClockworkContraption::new),
		MOUNTED = register("mounted", MountedContraption::new),
		STABILIZED = register("stabilized", StabilizedContraption::new),
		GANTRY = register("gantry", GantryContraption::new),
		CARRIAGE = register("carriage", CarriageContraption::new),
		ELEVATOR = register("elevator", ElevatorContraption::new);

	Supplier<? extends Contraption> factory;
	String id;

	public static ContraptionType register(String id, Supplier<? extends Contraption> factory) {
		ContraptionType value = new ContraptionType(id, factory);
		ENTRIES.put(id, value);
		return value;
	}

	private ContraptionType(String id, Supplier<? extends Contraption> factory) {
		this.factory = factory;
		this.id = id;
	}

	public static Contraption fromType(String type) {
		for (Entry<String, ContraptionType> allContraptionTypes : ENTRIES.entrySet())
			if (type.equals(allContraptionTypes.getKey()))
				return allContraptionTypes.getValue().factory.get();
		return null;
	}

}
