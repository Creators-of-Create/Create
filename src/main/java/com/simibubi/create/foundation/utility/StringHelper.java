package com.simibubi.create.foundation.utility;

import java.util.Locale;

public class StringHelper {

	public static String snakeCaseToCamelCase(String text) {
		StringBuilder builder = new StringBuilder();
		builder.append(text.substring(0, 1).toUpperCase(Locale.ROOT));

		for (int i = 1; i < text.length(); i++) {
			int j = text.indexOf('_', i);

			if (j == -1) {
				builder.append(text.substring(i));
				break;
			}

			builder.append(text.substring(i, j).toLowerCase(Locale.ROOT));
			builder.append(text.substring(j + 1, j + 2).toUpperCase(Locale.ROOT));

			i = j + 1;
		}

		return builder.toString();
	}

	public static String camelCaseToSnakeCase(String text) {
		StringBuilder builder = new StringBuilder();

		for (char c : text.toCharArray()) {
			if (Character.isUpperCase(c)) {
				builder.append('_');
				builder.append(Character.toLowerCase(c));
			} else {
				builder.append(c);
			}
		}

		if (builder.length() > 0 && builder.charAt(0) == '_')
			builder.deleteCharAt(0);

		return builder.toString();
	}

}
