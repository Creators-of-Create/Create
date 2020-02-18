package com.simibubi.create.foundation.behaviour;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class ValueBox {

	String label = "Value Box";
	String sublabel = "";
	String scrollTooltip = "";
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
	
	public ValueBox subLabel(String sublabel) {
		this.sublabel = sublabel;
		return this;
	}
	
	public ValueBox scrollTooltip(String scrollTip) {
		this.scrollTooltip = scrollTip;
		return this;
	}

	public ValueBox withColors(int passive, int highlight) {
		this.passiveColor = passive;
		this.highlightColor = highlight;
		return this;
	}

	public static class ItemValueBox extends ValueBox {
		ItemStack stack;
		int count;

		public ItemValueBox(String label, AxisAlignedBB bb, ItemStack stack, int count) {
			super(label, bb);
			this.stack = stack;
			this.count = count;
		}

	}
	
	public static class TextValueBox extends ValueBox {
		String text;

		public TextValueBox(String label, AxisAlignedBB bb, String text) {
			super(label, bb);
			this.text = text;
		}
		
	}

}
