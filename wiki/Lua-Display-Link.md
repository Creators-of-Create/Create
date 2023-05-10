| Method                                | Description                                          |
|---------------------------------------|------------------------------------------------------|
| [`setCursorPos(x, y)`](#setCursorPos) | Sets the cursor position                             |
| [`getCursorPos()`](#getCursorPos)     | Gets the current cursor position                     |
| [`getSize()`](#getSize)               | Gets the display size of the connected target        |
| [`isColor()`](#isColor)               | Whether the connected display target supports color  |
| [`isColour()`](#isColour)             | Whether the connected display target supports colour |
| [`write(text)`](#writetext)           | Writes text at the current cursor position           |
| [`clearLine()`](#clearLine)           | Clears the line at the current cursor position       |
| [`clear()`](#clear)                   | Clears the whole display                             |
| [`update()`](#update)                 | Pushes an update to the display target               |

---
### `setCursorPos(x, y)`
Sets the cursor position. Can be outside the bounds of the connected display.

**Parameters**
- _x:_ `number` The cursor x position.
- _y:_ `number` The cursor y position.

---
### `getCursorPos()`
Gets the current cursor position.

**Returns**
- `number` The cursor x position.
- `number` The cursor y position.

---
### `getSize()`
Gets the size of the connected display target.

**Returns**
- `number` The width of the display.
- `number` The height of the display.

---
### `isColor()`
Checks whether the connected display target supports color.

**Returns**
- `boolean` Whether the display supports color.

---
### `isColour()`
Checks whether the connected display target supports colour.

**Returns**
- `boolean` Whether the display supports colour.

---
### `write(text)`
Writes text at the current cursor position, moving the cursor to the end of the text.
This only writes to an internal buffer. For the changes to show up on the display [`update()`](#update) must be used.
If the cursor is outside the bounds of the connected display, the text will not show up on the display.

This will overwrite any text currently at the cursor position.

**Parameters**
- _text:_ `string` The text to write.

**See also**
- [`update()`](#update) To push the changes to the display target.

---
### `clearLine()`
Clears the line at the current cursor position.

**See also**
- [`update()`](#update) To push the changes to the display target.

---
### `clear()`
Clears the whole display.

**See also**
- [`update()`](#update) To push the changes to the display target.

---
### `update()`
Pushes any changes to the connected display target.
Any changes made are only made to an internal buffer.
For them to show up on the display they must be pushed to the display using this function.
This allows for this peripheral to be better multithreaded and for users to be able to change multiple lines at once by
using multiple [`write(text)`](#writetext) calls and then one [`update()`](#update) call.

**See also**
- [`write(text)`](#writetext) To write text to the display target.
