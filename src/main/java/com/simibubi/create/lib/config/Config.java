package com.simibubi.create.lib.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.datafixers.util.Pair;

public class Config {
	public static Map<String, ConfigValue> valuesAndStrings = new HashMap<>();

	public File configFile;
	public ArrayList<ConfigGroup> groups = new ArrayList<>();
	public ArrayList<ConfigValue> allValues = new ArrayList<>();
	public Properties properties;
	private static final Logger LOGGER = LogManager.getLogger("Create");
	public String name;

	public Config(String name) {
		this.name = name;
		configFile = new File(Configs.PATH_TO_CONFIGS + name + ".properties");
		properties = new Properties();
		try {
			configFile.createNewFile();
		} catch (IOException e) {
			LOGGER.fatal("There was an error initializing Create's \"{}\" config!", name, e);
		}
	}

	public Map<String, Object> valueMap() {
		Map<String, Object> map = new HashMap<>();
		for (ConfigValue<?> value : allValues) {
			map.put(value.key, value.value);
		}
		return map;
	}

	public void set(ConfigValue value) {
		if (allValues.contains(value)) {
			allValues.set(allValues.indexOf(value), value);
		} else {
			allValues.add(value);
		}
		if (properties.containsKey(value.key)) {
			properties.setProperty(value.key, value.value.toString());
		} else {
			properties.putIfAbsent(value.key, value.value.toString());
		}
	}

	public Object get(String key) {
		for (ConfigValue value : allValues) {
			if (value.key.equals(key)) {
				return value;
			}
		}
		return null;
	}

	public void updateValuesList() {
		ArrayList<ConfigValue> values = new ArrayList<>();
		for (ConfigGroup group : groups) {
			for (ConfigValue value : group.configs) {
				values.add(value);
			}
		}
		allValues = values;
	}

	public List<ConfigValue> getAllValues() {
		return allValues;
	}

	public void init() {
		updateValuesList();
		parseProperties();
		writeAll();
	}

	/**
	 * Saves all values changed since launch/last save
	 */
	public void writeAll() {
		try {
			configFile.delete(); // clear the file to remove any remaining garbage
			configFile.createNewFile();

			FileOutputStream out = new FileOutputStream(configFile);
			properties.store(out, null);
			out.close();
			LOGGER.info("\"{}\" config was successfully saved.", name);
		} catch (IOException e) {
			LOGGER.fatal("There was an error saving Create's \"{}\" config!", name, e);
		}
	}

	public static List<Pair<String, String>> getKeysAndValues(Properties properties) {
		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();
		List<Pair<String, String>> pairs = new ArrayList<>();
		for (Object valueObject : properties.values()) {
			String value = (String) valueObject;
			values.add(value);
		}
		for (Object keyObject : properties.keySet()) {
			String key = (String) keyObject;
			keys.add(key);
		}
		for (int i = 0; i < keys.toArray().length; i++) {
			Pair<String, String> pair = new Pair<>(keys.get(i), values.get(i));
			pairs.add(pair);
		}
		// some reason the array is backwards so I have to do this
		Pair<String, String>[] pairs2 = new Pair[pairs.size()];
		for (int i = 0; i < pairs.size(); i++) {
			pairs2[pairs2.length - i - 1] = pairs.get(i);
		}

		return Arrays.asList(pairs2);
	}

	public void parseProperties() {
		Properties fileProperties = new Properties();
		try {
			FileInputStream in = new FileInputStream(configFile);
			fileProperties.load(in);
			in.close();
		} catch (IOException e) {
			LOGGER.fatal("There was an error reading Create's \"{}\" config!", name, e);
		}
		for (Pair pair : getKeysAndValues(fileProperties)) {
			String value = (String) pair.getSecond(); // gets the value as a string
			Object newValue = null;
			// booleans
			if (value.equals("true")) {
				newValue = true;
			}
			if (value.equals("false")) {
				newValue = false;
			}
			// ints
			if (newValue == null && !value.contains(".")) {
				try {
					newValue = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					// not a number
				}
			}
			// floats
			if (newValue == null && value.contains(".")) {
				newValue = Float.parseFloat(value);
			}
			// enums
			if (newValue == null) {
				for (ConfigValue configValue : allValues) {
					Object[] enumValues = configValue.value.getClass().getEnumConstants();
					if (enumValues != null && enumValues.length != 0) {
						for (Object enumValue : enumValues) {
							if (enumValue.toString().equals(value)) {
								newValue = enumValue;
							}
						}
					}
				}
			}
			// setting final value
			for (ConfigValue configValue : allValues) {
				if (configValue.key.equals(pair.getFirst())) { // if the keys are equal
					if (newValue == configValue.defaultValue) {
						break;
					}
					if (configValue.value.getClass() == Double.class) { // double handling
						newValue = Double.parseDouble(value);
					}
					configValue.set(newValue);
					break;
				}
			}
		}
		LOGGER.info("\"{}\" config was successfully loaded.", name);
	}
}
