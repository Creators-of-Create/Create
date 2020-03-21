package com.simibubi.create.config;

import com.simibubi.create.AllBlocks;

public class StressConfigDefaults {
	
	/**
	 * Increment this number if all stress entries should be updated in this update.
	 * Worlds from the previous version will overwrite potentially changed values with the new defaults.
	 */
	public static final int forcedUpdateVersion = 1;

	public static double getDefaultStressCapacity(AllBlocks block) {
	
		switch (block) {
		case CREATIVE_MOTOR:
			return 2048;
		case FURNACE_ENGINE:
			return 1024;
		case MECHANICAL_BEARING:
			return 512;
		case ENCASED_FAN:
		case HAND_CRANK:
			return 32;
		case WATER_WHEEL:
			return 8;
		default:
			return -1;
		}
	}

	public static double getDefaultStressImpact(AllBlocks block) {
	
		switch (block) {
		case CRUSHING_WHEEL:
		case MECHANICAL_PRESS:
			return 8;
	
		case DRILL:
		case SAW:
		case DEPLOYER:
		case MECHANICAL_MIXER:
			return 4;
	
		case MECHANICAL_CRAFTER:
		case TURNTABLE:
		case MECHANICAL_PISTON:
		case MECHANICAL_BEARING:
		case CLOCKWORK_BEARING:
		case ROPE_PULLEY:
		case STICKY_MECHANICAL_PISTON:
			return 2;
			
		case BELT:
		case ENCASED_FAN:
		case CUCKOO_CLOCK:
			return 1;
			
		default:
			return 0;
		}
	}

}
