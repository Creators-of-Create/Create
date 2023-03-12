|                     Method                      | Description                            |
|:-----------------------------------------------:|----------------------------------------|
| [`setTargetSpeed(speed)`](#setTargetSpeedspeed) | Sets the target rotation speed         |
|      [`getTargetSpeed()`](#getTargetSpeed)      | Gets the current target rotation speed |

---
### `setTargetSpeed(speed)`
Sets the rotation speed controller's target speed.

**Parameters**
- _speed:_ `number` Target speed. Must be an integer within range of [-256..256]. Values outside of this range will be clamped.

---
### `getTargetSpeed()`
Gets the rotation speed controller's current target speed.

**Returns**
- `number` Current target rotation speed.
