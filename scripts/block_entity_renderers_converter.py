rendererLineStart = ".renderer(() -> "
allTileEntities = "../src/main/java/com/simibubi/create/AllTileEntities.java"
lines = []
file = open(allTileEntities)
for line in file:
    if rendererLineStart in line:
        if "//" in line:
            lines.append(line)
            continue
        toReplace = line.split(rendererLineStart)[1].split(")")[0]
        rendererClass = toReplace.split("::")[0]
        newLine = line.replace(toReplace, "ctx -> new %s(ctx.getBlockEntityRenderDispatcher())" % rendererClass)
        lines.append(newLine)
    else: lines.append(line)

out = open("../src/main/java/com/simibubi/create/AllTileEntitiesNew.java", "a")
out.writelines(lines)
