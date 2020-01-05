package com.simibubi.create.foundation.behaviour;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class ValueBox {

	String label = "Value Box";
	Vec3d labelOffset = Vec3d.ZERO;
	int passiveColor;
	int highlightColor;
	AxisAlignedBB bb = new AxisAlignedBB(0, 0, 0, 0, 0, 0);

	public ValueBox(String label, AxisAlignedBB bb) {
		this.label = label;
		this.bb = bb;
	}

	public ValueBox offsetLabel(Vec3d offset) {
		this.labelOffset = offset;
		return this;
	}

	public ValueBox withColors(int passive, int highlight) {
		this.passiveColor = passive;
		this.highlightColor = highlight;
		return this;
	}

	public static class ItemValueBox extends ValueBox {
		int count;

		public ItemValueBox(String label, AxisAlignedBB bb, int count) {
			super(label, bb);
			this.count = count;
		}

	}

}
