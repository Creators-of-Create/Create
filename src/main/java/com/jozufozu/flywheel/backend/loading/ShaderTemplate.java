package com.jozufozu.flywheel.backend.loading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderTemplate {

	private static final String delimiter = "#flwbeginbody";
	private static final Pattern headerFinder = Pattern.compile(delimiter);

	private static final Pattern prefixer = Pattern.compile("#FLWPrefixFields\\((\\w+),\\s*(\\w+),\\s*([\\w\\d]+)\\)");
	private static final Pattern assigner = Pattern.compile("#FLWAssignFields\\(([\\w\\d_]+),\\s*([\\w\\d_.]+),\\s*([\\w\\d_.]+)\\)");

	final String[] requiredStructs;

	final String header;
	final String body;

	public ShaderTemplate(String[] requiredStructs, String templateSrc) {
		this.requiredStructs = requiredStructs;
		Matcher matcher = headerFinder.matcher(templateSrc);

		if (!matcher.find()) {
			throw new RuntimeException("Shader template must have a header and footer delimited by '" + delimiter + "'");
		}

		this.header = templateSrc.substring(0, matcher.start());
		this.body = templateSrc.substring(matcher.end());

	}

	public String apply(ParsedShader shader) {

		return header +
				shader.src +
				processBody(shader);
	}

	public String processBody(ParsedShader shader) {
		String s = body;

		List<String> missing = new ArrayList<>();

		for (String name : requiredStructs) {
			TaggedStruct struct = shader.getTag(name);

			if (struct != null) {
				s = s.replace(name, struct.name);
			} else {
				missing.add(name);
			}
		}

		if (!missing.isEmpty()) {
			String err = shader.loc + " is missing: " + String.join(", ", missing);
			throw new RuntimeException(err);
		}

		s = fillPrefixes(shader, s);
		s = fillAssigns(shader, s);

		return s;
	}

	private String fillPrefixes(ParsedShader shader, String s) {
		Matcher prefixMatches = prefixer.matcher(s);

		StringBuffer out = new StringBuffer();
		while (prefixMatches.find()) {
			String structName = prefixMatches.group(1);
			String modifier = prefixMatches.group(2);
			String prefix = prefixMatches.group(3);

			TaggedStruct struct = shader.getStruct(structName);

			StringBuilder builder = new StringBuilder();
			for (Map.Entry<String, String> field : struct.fields.entrySet()) {
				builder.append(modifier);
				builder.append(' ');
				builder.append(field.getValue());
				builder.append(' ');
				builder.append(prefix);
				builder.append(field.getKey());
				builder.append(";\n");
			}

			prefixMatches.appendReplacement(out, builder.toString());
		}
		prefixMatches.appendTail(out);
		return out.toString();
	}

	private String fillAssigns(ParsedShader shader, String s) {
		Matcher assignMatches = assigner.matcher(s);

		StringBuffer out = new StringBuffer();
		while (assignMatches.find()) {
			String structName = assignMatches.group(1);
			String lhs = assignMatches.group(2);
			String rhs = assignMatches.group(3);

			TaggedStruct struct = shader.getStruct(structName);

			StringBuilder builder = new StringBuilder();
			for (String field : struct.fields.keySet()) {
				builder.append(lhs);
				builder.append(field);
				builder.append(" = ");
				builder.append(rhs);
				builder.append(field);
				builder.append(";\n");
			}

			assignMatches.appendReplacement(out, builder.toString());
		}
		assignMatches.appendTail(out);
		return out.toString();
	}
}
