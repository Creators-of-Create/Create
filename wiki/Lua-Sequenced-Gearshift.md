| Method                                                 | Description                                                  |
|--------------------------------------------------------|--------------------------------------------------------------|
| [`rotate(angle, [modifier])`](#rotateangle-modifier)   | Rotates shaft by a set angle                                 |
| [`move(distance, [modifier])`](#movedistance-modifier) | Rotates shaft to move Piston/Pulley/Gantry by a set distance |
| [`isRunning()`](#isRunning)                            | Whether the gearshift is currently spinning                  |

---
### `rotate(angle, [modifier])`
Rotates connected components by a set angle.

**Parameters**
- _angle:_ `number` Angle to rotate the shaft by in degrees. Must be a positive integer. To do backwards rotation, set _modifier_ to a negative value.
- _modifier?:_ `number = 1` Speed modifier which can be used to reverse rotation. Must be an integer within the range of [-2..2]. Values out of this range are ignored and the default of 1 is used.

---
### `move(distance, [modifier])`
Rotates connected components to move connected piston, pulley or gantry contractions by a set distance.

**Parameters**
- _distance:_ `number` Distance to move connected piston, pulley or gantry contraptions by. Must be a positive integer. To do backwards movement, set _modifier_ to a negative value.
- _modifier?:_ `number = 1` Speed modifier which can be used to reverse direction. Must be an integer within the range of [-2..2]. Values out of this range are ignored and the default of 1 is used.

---
### `isRunning()`
Checks if the sequenced gearshift is currently spinning.

**Returns**
- `boolean` Whether the sequenced gearshift is currently spinning.
