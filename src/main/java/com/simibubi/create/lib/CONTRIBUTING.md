# Library Package Guidelines

1. **Mixin Naming**
	- **Class Names**
	All mixin classes should be named after the class that they mixin to followed by either `Mixin` if the class is a regular mixin, or `Accessor` if it is an accessor mixin. When mixing into inner classes, use a `$` symbol to separate the outer and inner class names. For example, when making a regular mixin for the class `ClassA.ClassB`, the mixin class name would be `ClassA$ClassBMixin`.
	- **Field and Method Names**
	All non-shadowed fields and methods, including those in extension interfaces, should be prefixed with `create$`. Accessor and invoker methods' base name should be the same as the method that is being accessed or invoked. For example, the invoker method name for `renderBakedItemModel` would be `create$renderBakedItemModel`.

2. **Mixin Misc**
	- Mixins that mixin to a class that only exists in a certain environment type should have an `@Environment(EnvType.{type})` annotation. This annotation should go above the `@Mixin` annotation.
	- All mixin classes should be abstract. Regular mixins should have the `abstract` modifier. Accessors should be interfaces, which are already technically abstract.
	- Shadowed methods should always be abstract.
	- The body of static accessor or invoker methods should always throw an `AssertionError`.
	- `@Accessor` methods should always go before `@Invoker` methods.
	- The mixin config JSON file should have all mixins in alphabetical order, with accessor mixins being listed before regular mixins.

3. **Utility Classes**
	- All utility classes should be final and have a private nullary (empty) constructor. This constructor should go at the very end before inner classes, if there are any.

4. **Organization**
	- Regular mixins should go into the `com.simibubi.create.lib.mixin` package.
	- Accessor mixins should go into the `com.simibubi.create.lib.mixin.accessor` package.
	- Extension interfaces should go into the `com.simibubi.create.lib.extensions` package.
	- Helper/utility classes should go into the `com.simibubi.create.lib.util` package.
