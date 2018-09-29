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
                parameters = @Parameter(name = "value", type = byte.class),
                expression = "b == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = byte.class),
                expression = "b != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameters = @Parameter(name = "value", type = byte.class),
                expression = "b < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameters = @Parameter(name = "value", type = byte.class),
                expression = "b <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameters = @Parameter(name = "value", type = byte.class),
                expression = "b > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameters = @Parameter(name = "value", type = byte.class),
                expression = "b >= getValue()"
        )})
)
@Include(value = short.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameters = @Parameter(name = "value", type = short.class),
                expression = "s == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = short.class),
                expression = "s != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameters = @Parameter(name = "value", type = short.class),
                expression = "s < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameters = @Parameter(name = "value", type = short.class),
                expression = "s <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameters = @Parameter(name = "value", type = short.class),
                expression = "s > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameters = @Parameter(name = "value", type = short.class),
                expression = "s >= getValue()"
        )})
)
@Include(value = int.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameters = @Parameter(name = "value", type = int.class),
                expression = "i == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = int.class),
                expression = "i != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameters = @Parameter(name = "value", type = int.class),
                expression = "i < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameters = @Parameter(name = "value", type = int.class),
                expression = "i <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameters = @Parameter(name = "value", type = int.class),
                expression = "i > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameters = @Parameter(name = "value", type = int.class),
                expression = "i >= getValue()"
        )})
)
@Include(value = long.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameters = @Parameter(name = "value", type = long.class),
                expression = "l == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = long.class),
                expression = "l != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameters = @Parameter(name = "value", type = long.class),
                expression = "l < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameters = @Parameter(name = "value", type = long.class),
                expression = "l <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameters = @Parameter(name = "value", type = long.class),
                expression = "l > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameters = @Parameter(name = "value", type = long.class),
                expression = "l >= getValue()"
        )})
)
@Include(value = float.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameters = @Parameter(name = "value", type = float.class),
                expression = "f == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = float.class),
                expression = "f != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameters = @Parameter(name = "value", type = float.class),
                expression = "f < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameters = @Parameter(name = "value", type = float.class),
                expression = "f <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameters = @Parameter(name = "value", type = float.class),
                expression = "f > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameters = @Parameter(name = "value", type = float.class),
                expression = "f >= getValue()"
        )})
)
@Include(value = double.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameters = @Parameter(name = "value", type = double.class),
                expression = "d == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = double.class),
                expression = "d != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameters = @Parameter(name = "value", type = double.class),
                expression = "d < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameters = @Parameter(name = "value", type = double.class),
                expression = "d <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameters = @Parameter(name = "value", type = double.class),
                expression = "d > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameters = @Parameter(name = "value", type = double.class),
                expression = "d >= getValue()"
        )})
)
@Include(value = char.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameters = @Parameter(name = "value", type = char.class),
                expression = "c == getValue()"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = char.class),
                expression = "c != getValue()"
        ), @Mixin(
                name = "LessThan",
                factoryName = "isLessThan",
                parameters = @Parameter(name = "value", type = char.class),
                expression = "c < getValue()"
        ), @Mixin(
                name = "LessThanEquals",
                factoryName = "isLessThanEqualTo",
                parameters = @Parameter(name = "value", type = char.class),
                expression = "c <= getValue()"
        ), @Mixin(
                name = "GreaterThan",
                factoryName = "isGreaterThan",
                parameters = @Parameter(name = "value", type = char.class),
                expression = "c > getValue()"
        ), @Mixin(
                name = "GreaterThanEquals",
                factoryName = "isGreaterThanEqualTo",
                parameters = @Parameter(name = "value", type = char.class),
                expression = "c >= getValue()"
        ), @Mixin(
                name = "UpperCase",
                factoryName = "isUpperCase",
                expression = "$T.isUpperCase(c)",
                arguments = @Argument(type = Character.class)
        ), @Mixin(
                name = "LowerCase",
                factoryName = "isLowerCase",
                expression = "$T.isLowerCase(c)",
                arguments = @Argument(type = Character.class)
        )})
)
@Include(value = String.class,
        logic = @Logic(mixins = {@Mixin(
                name = "Equals",
                factoryName = "isEqualTo",
                parameters = @Parameter(name = "value", type = String.class),
                expression = "string.equals(getValue())"
        ), @Mixin(
                name = "NotEquals",
                factoryName = "isNotEqualTo",
                parameters = @Parameter(name = "value", type = String.class),
                expression = "!string.equals(getValue())"
        ), @Mixin(
                name = "EqualsIgnoreCase",
                factoryName = "isEqualToIgnoreCase",
                parameters = @Parameter(name = "value", type = String.class),
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
                parameters = @Parameter(name = "pattern", type = Pattern.class),
                expression = "getPattern().matcher(string).matches()"
        ), @Mixin(
                name = "Contains",
                factoryName = "contains",
                parameters = @Parameter(name = "value", type = String.class),
                expression = "string.contains(getValue())"
        )}, methods = {})
)
package io.logic;

import io.logic.Logic.Include;
import io.logic.Logic.Mixin;
import io.logic.Logic.Mixin.Argument;
import io.logic.Logic.Mixin.Parameter;

import java.util.regex.Pattern;