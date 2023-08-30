package com.simibubi.create.foundation.data;

import java.util.Map;
import java.util.function.UnaryOperator;

public interface LanguageProviderExtension {
	void create$addPostprocessor(UnaryOperator<Map<String, String>> postprocessor);
}
