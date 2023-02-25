# Adding to GameTests

#### Adding Tests
All tests must be static, take a `CreateGameTestHelper`, return void, and be annotated with `@GameTest`.
Non-annotated methods will be ignored. The annotation must also specify a structure template.
Classes holding registered tests must be annotated with `GameTestGroup`.

#### Adding Groups/Classes
Added test classes must be added to the list in `CreateGameTests`. They must be annotated with
`@GameTestGroup` and given a structure path.

#### Exporting Structures
Structures can be quickly exported using the `/create test export` command (or `/c test export`).
Select an area with the Schematic and Quill, and run it to quickly export a test structure
directly to the correct directory.
