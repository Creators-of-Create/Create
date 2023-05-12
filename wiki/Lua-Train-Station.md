| Method                                                          | Description                                        |
|-----------------------------------------------------------------|----------------------------------------------------|
| [`assemble()`](#assemble)                                       | Assembles a new train at the station               |
| [`disassemble()`](#disassemble)                                 | Disassembles the currently present train           |
| [`setAssemblyMode(assemblyMode)`](#setAssemblyModeassemblyMode) | Sets the station's assembly mode                   |
| [`isInAssemblyMode()`](#isInAssemblyMode)                       | Whether the station is in assembly mode            |
| [`getStationName()`](#getStationName)                           | Gets the station's current name                    |
| [`setStationName(name)`](#setStationNamename)                   | Sets the station's name                            |
| [`isTrainPresent()`](#isTrainPresent)                           | Whether a train is present at the station          |
| [`isTrainImminent()`](#isTrainImminent)                         | Whether a train is imminent to the station         |
| [`isTrainEnroute()`](#isTrainEnroute)                           | Whether a train is enroute to the station          |
| [`getTrainName()`](#getTrainName)                               | Gets the currently present train's name            |
| [`setTrainName(name)`](#setTrainNamename)                       | Sets the currently present train's name            |
| [`hasSchedule()`](#hasSchedule)                                 | Whether the currently present train has a schedule |
| [`getSchedule()`](#getSchedule)                                 | Gets the currently present train's schedule        |
| [`setSchedule(schedule)`](#setScheduleschedule)                 | Sets the currently present train's schedule        |

---
### `assemble()`
Assembles a new train at the station. The station must be in assembly mode prior to calling this function.
This function also causes the station to exit assembly mode after the train is done assembing.

**Throws**
- If the station is not in assembly mode.
- If the station is not connected to a track.
- If the train failed to assemble.
- If the station failed to exit assembly mode.

**See also**
- [`setAssemblyMode(assemblyMode)`](#setAssemblyModeassemblyMode) To set the assembly mode of the station.

---
### `disassemble()`
Disassembles the station's currently present train. The station must not be in assembly mode.

**Throws**
- If the station is in assembly mode.
- If the station is not connected to a track.
- If there is currently no train present at the station.
- If the train failed to disassemble.

**See also**
- [`setAssemblyMode(assemblyMode)`](#setAssemblyModeassemblyMode) To set the assembly mode of the station.

---
### `setAssemblyMode(assemblyMode)`
Sets the station's assembly mode.

**Parameters**
- _assemblyMode:_ `boolean` Whether the station should be in assembly mode.

**Throws**
- If the station fails to enter or exit assembly mode.
- If the station is not connected to a track.

---
### `isInAssemblyMode()`
Checks whether the station is in assembly mode.

**Returns**
- `boolean` Whether the station is in assembly mode.

---
### `getStationName()`
Gets the station's current name.

**Returns**
- `string` The station's current name.

**Throws**
- If the station is not connected to a track.

---
### `setStationName(name)`
Sets the station's name.

**Parameters**
- _name:_ `string` What to set the station's name to.

**Throws**
- If the station name fails to be set.
- If the station is not connected to a track.

---
### `isTrainPresent()`
Checks whether a train is currently present at the station.

**Returns**
- `boolean` Whether a train is present at the station.

**Throws**
- If the station is not connected to a track.

---
### `isTrainImminent()`
Checks whether a train is imminently arriving at the station.
Imminent is defined as being within 30 blocks of the station.
This will not be true if the train has arrived and stopped at the station.

**Returns**
- `boolean` Whether a train is imminent to the station.

**Throws**
- If the station is not connected to a track.

**See also**
- [`isTrainPresent()`](#isTrainPresent) To check if a train is present at the station.

---
### `isTrainEnroute()`
Checks whether a train is enroute and navigating to the station.

**Returns**
- `boolean` Whether a train is enroute to the station.

**Throws**
- If the station is not connected to a track.

---
### `getTrainName()`
Gets the currently present train's name.

**Returns**
- `string` The currently present train's name.

**Throws**
- If the station is not connected to a track.
- If there is currently no train present at the station.

---
### `setTrainName(name)`
Sets the currently present train's name.

**Parameters**
- _name:_ `string` What to set the currently present train's name to.

**Throws**
- If the station is not connected to a track.
- If there is currently no train present at the station.

---
### `hasSchedule()`
Checks whether the currently present train has a schedule.

**Returns**
- `boolean` Whether the currently present train has a schedule.

**Throws**
- If the station is not connected to a track.
- If there is currently no train present at the station.

---
### `getSchedule()`
Gets the currently present train's schedule.

**Returns**
- `table` The train's schedule

**Throws**
- If the station is not connected to a track.
- If there is currently no train present at the station.
- If the present train doesn't have a schedule.

**See also**
- [Lua Train Schedules](#Lua-Train-Schedules) How train schedules are represented in Lua.

---
### `setSchedule(schedule)`
Sets the currently present train's schedule. This will overwrite the currently set schedule.

**Parameters**
- _schedule:_ `table` The schedule to set the present train to.

**Throws**
- If the station is not connected to a track.
- If there is currently no train present at the station.

**See also**
- [Lua Train Schedules](#Lua-Train-Schedules) How train schedules are represented in Lua.
