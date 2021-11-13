#!/bin/sh

for file in $(find ../src/main/java -type f -name "*.java"); do
	sed -i \
		-e 's/@Environment/@OnlyIn/g' \
		-e 's/EnvType\.CLIENT/Dist\.CLIENT/g' \
		-e 's/EnvType\.SERVER/EnvType\.DEDICATED_SERVER/g' \
		-e 's/import net\.fabricmc\.api\.Environment;/import net\.minecraftforge\.api\.distmarker\.OnlyIn;/g' \
		-e 's/import net\.fabricmc\.api\.EnvType;/import net\.minecraftforge\.api\.distmarker\.Dist;/g' \
	$file

	echo "Reverted $file"
done

echo 'Done'
