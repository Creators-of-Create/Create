| Method                                                 | Description                                                  |
|--------------------------------------------------------|--------------------------------------------------------------|
| [`rotate(angle, [modifier])`](#rotateangle-modifier)   | Rotates shaft by a set angle                                 |
| [`move(distance, [modifier])`](#movedistance-modifier) | Rotates shaft to move Piston/Pulley/Gantry by a set distance |
| [`isRunning()`](#isRunning)                            | Checks if the gearshift is spinning                          |

---
### `rotate(angle, [modifier])`
Rotates connected components by a set angle.

**Parameters**
- _angle:_ `number` Angle to rotate shaft by. Must be a positive integer.
- _modifier?:_ `number = 1` Speed modifier which can be used to reverse rotation. Must be an integer within range of [-2..2]. Values out of this range will cause the modifier to revert to its default of 1.

---
### `move(distance, [modifier])`
Rotates connected components to move connected piston, pulley or gantry contractions by a set distance.

**Parameters**
- _distance:_ `number` Distance to move connected piston, pulley or gantry contraptions by. Must be a positive integer.
- _modifier?:_ `number = 1` Speed modifier which can be used to reverse rotation. Must be an integer within range of [-2..2]. Values out of this range will cause the modifier to revert to its default of 1.

---
### `isRunning()`
Checks if the sequenced gearshift is currently spinning.

**Returns**
- `boolean` Whether the sequenced gearshift is currently spinning.
