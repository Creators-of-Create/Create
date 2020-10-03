package com.simibubi.create.content.contraptions.goggles;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.util.text.ITextComponent;

/*
* Implement this Interface in the TileEntity class that wants to add info to the screen
* */
public interface IHaveGoggleInformation {

	DecimalFormat decimalFormat = new DecimalFormat("#.##");
	String spacing = "    ";

	/**
	* this method will be called when looking at a TileEntity that implemented this interface
	 *
	 * @return {{@code true}} if the tooltip creation was successful and should be displayed,
	 * or {{@code false}} if the overlay should not be displayed
	* */
	default boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking){
		return false;
	}

	static String format(double d) {
		return decimalFormat.format(d);
	}

}
