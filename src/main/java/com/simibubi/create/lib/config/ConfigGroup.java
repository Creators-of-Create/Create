package com.simibubi.create.lib.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigGroup {
	public List<ConfigValue> configs = new ArrayList<>();
	public Config config;
	public int depth;
	public String name;
	public List<String> comments;

	public ConfigGroup(String name, int depth, String... comments) {
		this.depth = depth;
		this.name = name;
		this.comments = new ArrayList<>(Arrays.asList(comments));
	}

	// values

	public void addConfigValue(ConfigValue value) {
		configs.add(value);
		value.setGroup(this);
		if (config != null) {
			config.set(value);
		}
	}

	public void registerValues() {
		for (ConfigValue<?> value : configs) {
			this.config.set(value);
		}
	}

	public ConfigValue getConfigValue(String key) {
		for (ConfigValue value : configs) {
			if (value.key.equals(key)) {
				return value;
			}
		}
		return null;
	}

	// comments

	public void addComment(String comment) {
		comments.add(comment);
	}

	public void addComments(String... comments) {
		for (String comment : comments) {
			addComment(comment);
		}
	}

	// config

	public void setConfig(Config config) {
		this.config = config;
		config.groups.add(this);
	}

	public Config getConfig() {
		return config;
	}
}
