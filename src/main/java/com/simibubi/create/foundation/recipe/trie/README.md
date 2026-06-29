# Optimizing Basin Recipe Lookups

By Slimeist (techno-sam)

## Definitions

- **Recipe<?>**: A Minecraft recipe
- **AbstractVariant**: A representation of a "thing" (i.e. an item or a fluid)
- **AbstractIngredient**: A set of `AbstractVariant`s
- **AbstractRecipe**: A set of `AbstractIngredient`s, mapping one-to-many to `Recipe<?>`
- **Basin**: A set of `AbstractVariant`s
- **SuperBasin**: A set of `AbstractIngredient`s

An `AbstractIngredient` is satisfied by any `AbstractVariant` it contains. (OR semantics)
An `AbstractRecipe` is satisfied by a set containing all `AbstractIngredient`s it contains. (AND semantics)

A `SuperBasin` can be constructed from a `Basin` by defining it as the set of all
`AbstractIngredient`s that can be satisfied by at least one `AbstractVariant` in the `Basin`.

`AbstractVariant`s and `AbstractIngredient`s have unique int IDs within the type (i.e. there may be an `AV{42}` and an
`AI{42}`, but not two `AV{42}`s).

## Problem Statement

### Base

Given a `Basin`, find all `AbstractRecipe`s that can be satisfied by the `Basin`'s contents.

### Revised

Given a `SuperBasin`, find all `AbstractRecipe`s whose ingredient sets are subsets of the `SuperBasin`.

## Solution

### Setup

We pre-construct a sparse trie map from a sorted string of `AbstractVariant` IDs (i.e. `int[]`) to a list of
`Recipe<?>`s.

### Lookup

1. Given a `Basin`, convert it to a `SuperBasin`. This may be useful to cache.
2. Traverse the trie map to find all satisfiable `AbstractRecipe`s.
3. Convert the `AbstractRecipe`s to `Recipe<?>`s.
4. Profit
