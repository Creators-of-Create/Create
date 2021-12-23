# Porting Guidelines

1. <span id="mindiff">**"Min-Diff"**</span>
   As little as possible code should be changed to maximize compatibility with future upstream commits. This includes file names, java names, and code formatting. The only exception to this rule is that adding imports is allowed.


2. **Replacing Code**
   Directly replace code with alternatives from dependencies, such as vanilla, Fabric, or the [lib package](../src/main/java/com/simibubi/create/lib). If there is no current replacement, comment out the code by **highlighting and then pressing** `Ctrl+/` (`⌘+/`). **Do not use block comments or javadoc comments**.
   If the replacement needs mixins or is too large to directly replace (for example, doesn't fit in the same method), write the necessary code in the lib package instead of the main mod. Alternatively, a replacement could also be a dependency on another library mod.
   Generally, the lib package is for code that does not depend on the main mod's code and does not have any "Create specific elements". Code within the lib package that references code outside it should be kept to a minimum. All new mixins and accessors should go into the library mod.
   Some code has no current replacement and doesn't currently need one for the mod to function, but might have one in the future. For cases like these, do not delete the code and instead comment it out like specified before. The only difference now is that it is acceptable to use block comments if a whole line cannot be commented out (for example, a semicolon at the end of a builder pattern).


3. **Code Style**
   Most code style rules are listed under [rule 1](#mindiff), so refer there for more information. Additionally, all new code that is added to the project must follow the code style used by Create. This includes the lib package and replacement code in the main mod.


4. **Library Mod**
   The Contribution guidelines for the library mod can be found [here](../src/main/java/com/simibubi/create/lib/CONTRIBUTING.md).


5. **Uncertainty**
   If you are unsure of what to do with a certain section of code, discuss with the rest of the porting team before making changes.
