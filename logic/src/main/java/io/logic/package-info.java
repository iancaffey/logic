@Include(value = boolean.class,
        logic = @Logic(mixins = {@Mixin(
                name = "True",
                factoryName = "isTrue",
                expression = "b"
        ), @Mixin(
                name = "False",
                factoryName = "isFalse",
                expression = "!b"
        )})
)
@Include(value = byte.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = byte.class,
                expression = "b == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = byte.class,
                expression = "b != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameterNames = "value",
                parameterTypes = byte.class,
                expression = "b < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameterNames = "value",
                parameterTypes = byte.class,
                expression = "b <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameterNames = "value",
                parameterTypes = byte.class,
                expression = "b > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameterNames = "value",
                parameterTypes = byte.class,
                expression = "b >= getValue()"
        )})
)
@Include(value = short.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = short.class,
                expression = "s == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = short.class,
                expression = "s != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameterNames = "value",
                parameterTypes = short.class,
                expression = "s < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameterNames = "value",
                parameterTypes = short.class,
                expression = "s <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameterNames = "value",
                parameterTypes = short.class,
                expression = "s > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameterNames = "value",
                parameterTypes = short.class,
                expression = "s >= getValue()"
        )})
)
@Include(value = int.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = int.class,
                expression = "i == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = int.class,
                expression = "i != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameterNames = "value",
                parameterTypes = int.class,
                expression = "i < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameterNames = "value",
                parameterTypes = int.class,
                expression = "i <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameterNames = "value",
                parameterTypes = int.class,
                expression = "i > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameterNames = "value",
                parameterTypes = int.class,
                expression = "i >= getValue()"
        )})
)
@Include(value = long.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = long.class,
                expression = "l == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = long.class,
                expression = "l != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameterNames = "value",
                parameterTypes = long.class,
                expression = "l < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameterNames = "value",
                parameterTypes = long.class,
                expression = "l <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameterNames = "value",
                parameterTypes = long.class,
                expression = "l > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameterNames = "value",
                parameterTypes = long.class,
                expression = "l >= getValue()"
        )})
)
@Include(value = float.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = float.class,
                expression = "f == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = float.class,
                expression = "f != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameterNames = "value",
                parameterTypes = float.class,
                expression = "f < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameterNames = "value",
                parameterTypes = float.class,
                expression = "f <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameterNames = "value",
                parameterTypes = float.class,
                expression = "f > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameterNames = "value",
                parameterTypes = float.class,
                expression = "f >= getValue()"
        )})
)
@Include(value = double.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = double.class,
                expression = "d == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = double.class,
                expression = "d != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameterNames = "value",
                parameterTypes = double.class,
                expression = "d < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameterNames = "value",
                parameterTypes = double.class,
                expression = "d <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameterNames = "value",
                parameterTypes = double.class,
                expression = "d > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameterNames = "value",
                parameterTypes = double.class,
                expression = "d >= getValue()"
        )})
)
@Include(value = char.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = char.class,
                expression = "c == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = char.class,
                expression = "c != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameterNames = "value",
                parameterTypes = char.class,
                expression = "c < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameterNames = "value",
                parameterTypes = char.class,
                expression = "c <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameterNames = "value",
                parameterTypes = char.class,
                expression = "c > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameterNames = "value",
                parameterTypes = char.class,
                expression = "c >= getValue()"
        ), @Mixin(
                name = "UpperCase",
                factoryName = "isUpperCase",
                expression = "java.lang.Character.isUpperCase(c)"
        ), @Mixin(
                name = "LowerCase",
                factoryName = "isLowerCase",
                expression = "java.lang.Character.isLowerCase(c)"
        )})
)
@Include(value = String.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameterNames = "value",
                parameterTypes = String.class,
                expression = "string.equals(getValue())"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameterNames = "value",
                parameterTypes = String.class,
                expression = "!string.equals(getValue())"
        ), @Mixin(
                name = "EqualsIgnoreCase",
                factoryName = "isEqualToIgnoreCase",
                parameterNames = "value",
                parameterTypes = String.class,
                expression = "string.equalsIgnoreCase(getValue())"
        ), @Mixin(
                name = "Empty",
                factoryName = "isEmpty",
                expression = "string.isEmpty()"
        ), @Mixin(
                name = "NonEmpty",
                factoryName = "isNotEmpty",
                expression = "!string.isEmpty()"
        ), @Mixin(
                name = "Matches",
                factoryName = "matches",
                parameterNames = "pattern",
                parameterTypes = Pattern.class,
                expression = "getPattern().matcher(string).matches()"
        ), @Mixin(
                name = "Contains",
                factoryName = "contains",
                parameterNames = "value",
                parameterTypes = String.class,
                expression = "string.contains(getValue())"
        )}, methods = {})
)
package io.logic;

import io.logic.Logic.Include;
import io.logic.Logic.Mixin;

import java.util.regex.Pattern;