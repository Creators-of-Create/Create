| Method                                          | Description                            |
|-------------------------------------------------|----------------------------------------|
| [`setTargetSpeed(speed)`](#setTargetSpeedspeed) | Sets the target rotation speed         |
| [`getTargetSpeed()`](#getTargetSpeed)           | Gets the current target rotation speed |

---
### `setTargetSpeed(speed)`
Sets the rotation speed controller's target speed.

**Parameters**
- _speed:_ `number` The target speed in RPM. Must be an integer within the range of [-256..256]. Values outside of this range will be clamped.

---
### `getTargetSpeed()`
Gets the rotation speed controller's current target speed.

**Returns**
- `number` The current target rotation speed in RPM.
