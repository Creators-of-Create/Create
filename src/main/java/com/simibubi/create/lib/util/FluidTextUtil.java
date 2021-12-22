package com.simibubi.create.lib.util;

import com.google.common.math.LongMath;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.config.AllConfigs;

/**
 * A few helpers to display fluids.
 * Stolen from Modern-Industrialization
 */
public class FluidTextUtil {

	/**
	 * Return a unicode string representing a fraction, like ¹⁄₈₁.
	 */
	public static String getUnicodeFraction(long numerator, long denominator, boolean simplify) {
		if (numerator < 0 || denominator < 0)
			throw new IllegalArgumentException("Numerator and denominator must be non negative.");

		if (simplify && denominator != 0) {
			long g = LongMath.gcd(numerator, denominator);
			numerator /= g;
			denominator /= g;
		}

		StringBuilder numString = new StringBuilder();

		while (numerator > 0) {
			numString.append(SUPERSCRIPT[(int) (numerator % 10)]);
			numerator /= 10;
		}

		StringBuilder denomString = new StringBuilder();

		while (denominator > 0) {
			denomString.append(SUBSCRIPT[(int) (denominator % 10)]);
			denominator /= 10;
		}

		return numString.reverse().toString() + FRACTION_BAR + denomString.reverse().toString();
	}

	/**
	 * Convert a non negative fluid amount in droplets to a unicode string
	 * representing the amount in millibuckets. For example, passing 163 will result
	 * in
	 *
	 * <pre>
	 * 2 ¹⁄₈₁
	 * </pre>
	 *
	 * .
	 */
	public static String getUnicodeMillibuckets(long droplets) {
		FluidUnit fluidUnit = AllConfigs.CLIENT.fluidUnitType.get();
		if(fluidUnit == FluidUnit.DROPLETS)
			return IHaveGoggleInformation.format(droplets);
		String result = IHaveGoggleInformation.format(droplets / fluidUnit.getOneBucketAmount());

		if (droplets % 81 != 0 && !AllConfigs.CLIENT.simplifyFluidUnit.get()) {
			result += " " + getUnicodeFraction(droplets % fluidUnit.getOneBucketAmount(), fluidUnit.getOneBucketAmount(), true);
		}

		return result;
	}

	private static final char[] SUPERSCRIPT = new char[] { '\u2070', '\u00b9', '\u00b2', '\u00b3', '\u2074', '\u2075', '\u2076', '\u2077', '\u2078',
			'\u2079' };
	private static final char FRACTION_BAR = '\u2044';
	private static final char[] SUBSCRIPT = new char[] { '\u2080', '\u2081', '\u2082', '\u2083', '\u2084', '\u2085', '\u2086', '\u2087', '\u2088',
			'\u2089' };

}
