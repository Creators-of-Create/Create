## Methods

| Method                                               | Description                                               |
|------------------------------------------------------|-----------------------------------------------------------|
| [`getHeldAddress()`](#getheldaddress)                | Gets the address of the held package.                     |
| [`setHeldAddress(address)`](#setheldaddressaddress)  | Sets the address of the held package.                     |
| [`getHeldOrderID()`](#getheldorderid)                | Gets the order ID of the held package.                    |
| [`getHeldItems()`](#gethelditems)                    | Gets the table of item stacks in the held package.        |
| [`getHeldContext()`](#getheldcontext)                | Gets the table of the order context for the held package. |

---
### `getHeldAddress()`
Gets the address of the package that is about to be taken out of it's inventory.

**Returns**
- `string`: The address of the held package.

**Throws**
- If no package is currently held.

---
### `setHeldAddress(address)`
Sets the address of the package that is about to be taken out of it's inventory. Even if the address is changed repackaging will still combine them. The resulting address will be the same as the last package address.

**Parameters**
- `address` (`string`): The address of the package to hold.

**Throws**
- If no package is currently held.

---
### `getHeldOrderID()`
Gets the ID of the order. Requests that are split into multiple packages will have the same order ID. If the order ID isn't set it will return -1.

**Returns**
- `number` The order ID 

**Throws**
- If no package is currently held.

---
### `getHeldItems()`
Gets the Lua table of item stacks in the held package.

**Returns**
- `table` The items in the held package.

**Throws**
- If no package is currently held.

**See also**
- [Lua Package Items](#Lua-Packager-Items) How package items are represented in Lua.

---
### `getHeldContext()`
Gets a Lua table of the order context stored in the held package. If empty then the package context is in another package with the same order ID. The order context is used to determine what items are needed for the order and how many crafts of each recipe there are.

**Returns**
- `table`: The order context table.

**Throws**
- If no package is currently held.

**See also**
- [Lua Order Context](#Lua-Packager-Order-Context) How order context is represented in Lua.


---
## Events

| Event                                                  | Description                                                           |
|--------------------------------------------------------|-----------------------------------------------------------------------|
| [`Create_Packager_Holding_Package`](#create_packager_holding_package) | Fired when the packager is about to send out a package |
| [`Create_Packager_Receive`](#create_packager_receive)  | Fired when a package is unpacked into it's inventory.                 |

---
### `Create_Packager_Holding_Package`
**Arguments**
1. `event`         (`string`)
2. `side`          (`string`)
3. `address`       (`string` or `nil`)
4. `orderID`       (`number`)
5. `items`         ([`table`](#Lua-Packager-Items))
6. `orderContext`  ([`table`](#Lua-Packager-Order-Context))

---
### `Create_Packager_Receive`
**Arguments**
1. `event`         (`string`)
2. `side`          (`string`)
3. `address`       (`string` or `nil`)
4. `orderID`       (`number`)
5. `items`         ([`table`](#Lua-Packager-Items))
6. `orderContext`  ([`table`](#Lua-Packager-Order-Context))

---


## Item Detail Provider

Calling `getItemDetails` On a package will include the following extra information about the package.
- `package_address`       (`string` or `nil`)
- `package_orderID`       (`number`)
- `package_items`         ([`table`](#Lua-Packager-Items))
- `package_orderContext`  ([`table`](#Lua-Packager-Order-Context))
