package com.jozufozu.flywheel.backend.loading;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstancedArraysShaderTemplate {

	private static final String delimiter = "#flwbeginbody";
	private static final Pattern headerFinder = Pattern.compile(delimiter);

	private static final Pattern prefixer = Pattern.compile("#FLWPrefixFields\\((\\S+),\\s*([^\\n]+)\\)");
	private static final Pattern assigner = Pattern.compile("#FLWAssignFields\\(([\\w\\d_]+),\\s*([\\w\\d_.]+),\\s*([\\w\\d_.]+)\\)");

	public static final String[] required = {"FLWInstanceData", "FLWVertexData", "FLWFragment"};

	final String header;
	final String body;

	public InstancedArraysShaderTemplate(String templateSrc) {
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

		for (String name : required) {
			TaggedStruct struct = shader.getTag(name);

			s = s.replace(name, struct.name);
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
			String prefix = prefixMatches.group(2);

			TaggedStruct struct = shader.getStruct(structName);

			StringBuilder builder = new StringBuilder();
			for (String field : struct.fields.keySet()) {
				builder.append(prefix);
				builder.append(field);
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
