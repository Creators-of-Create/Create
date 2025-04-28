This table contains the data for the order of a package. If it returns an empty table then the package context was not in this package it is in another package with the same order ID. The order context is used to determine what items are needed for the order and how many crafts of each recipe there are.

```lua
orderContext = {
  ordered_crafts = {
    { -- Requests 5 Oak Fences
      pattern = {
        entries = { -- Each entry represents a slot in the crafting grid
          {
            item = {
              count = 1, -- Items required for the recipe in this slot
              id = "minecraft:oak_planks", -- Registry name of required item
            },
            amount = 1, -- Always 1
          },
          { item = { count = 1, id = "minecraft:stick" }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:stick" }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:air" }, amount = 1 },
          { item = { count = 1, id = "minecraft:air" }, amount = 1 },
          { item = { count = 1, id = "minecraft:air" }, amount = 1 },
        },
      },
      count = 5,
    },
    { -- Requests 4 Oak Doors
      pattern = {
        entries = {
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:air"        }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:air"        }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:oak_planks" }, amount = 1 },
          { item = { count = 1, id = "minecraft:air"        }, amount = 1 },
        },
      },
      count = 4,
    },
  },
  ordered_stacks = {
    entries = { -- Required items for the order
      {
        item = {
          count = 1, -- Should be 1 for each item type
          id = "minecraft:oak_planks",
        },
        amount = 44, -- Total count needed for the order
      },
      {
        item = {
          count = 1,
          id = "minecraft:stick",
        },
        amount = 10,
      },
    },
  },
}
```

---
## Order Context

| Field            | Description                |
|------------------|----------------------------|
| `ordered_crafts` | Lists crafting entries     |
| `ordered_stacks` | Order required item counts |

### `ordered_crafts`
List of crafting entries

Each entry:
- `pattern.entries` (table list): Slots in the crafting grid. Will be empty if not a crafting request.
  - Each entry:
    - `item.id` (`string`): Registry name of required item.
    - `item.count` (`number`): Items required for the recipe in this slot (usually 1).
    - `amount` (`number`): Always 1.
- `count` (`number`): Number of recipe crafts to perform.

### `ordered_stacks`
Order required item counts

- `entries` (table list): each entry:
  - `item.id` (`string`): Registry name of required item.
  - `item.count` (`number`): Always 1.
  - `amount` (`number`): Total count of items needed for order.
