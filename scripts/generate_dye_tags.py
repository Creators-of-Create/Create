import os
colors = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"}
tagsPath = "../src/main/resources/data/c/tags/items/dyes/"
tagTemplate = '''{
  "replace": false,
  "values": [
    "minecraft:%s_dye"
  ]
}
'''

for color in colors:
    tagName = tagsPath + color + ".json"
    if not os.path.exists(tagsPath):
        os.makedirs(tagsPath)
    with open(tagName, "w") as tagFile:
        tagFile.write(tagTemplate % color)
        tagFile.close()
    print(tagName)
