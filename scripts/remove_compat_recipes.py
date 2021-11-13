import os

dataDir = "../src/generated/resources/data/"
mods = {"silents_mechanisms", "immersiveengineering", "mekanism", "eidolon", "mysticalworld", "thermal", "iceandfire"}
toMove = []

def iterateDir(dir):
    for fileName in os.listdir(dir):
        fullName = dir + fileName
        if os.path.isdir(fullName):
            iterateDir(fullName + "/")
        else:
            for modName in mods:
                if modName in fullName:
                    print(modName + " in " + fullName)
                    toMove.append(fullName)


iterateDir(dataDir)
for fileName in toMove:
    newName = fileName.replace(dataDir, "../src/generated/resources/compat_recipes/")
    os.makedirs(os.path.dirname(newName), exist_ok=True)
    os.rename(fileName, newName)
    print("Moved " + fileName + " to " + newName)
