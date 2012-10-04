/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Severity;
import org.apiwatch.models.Symbol;
import org.apiwatch.models.Visibility;

public class ModifiersChange implements APIStabilityRule {

    static String ID = "VIS002";
    static String NAME = "Modifiers Changed";
    static String MESSAGE = "Changed modifiers of %s %s '%s' (%s -> %s)";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return MESSAGE;
    }

    @Override
    public boolean isApplicable(APIDifference diff) {
        return diff.changeType == ChangeType.CHANGED && diff.element() instanceof Symbol
                && "modifiers".equals(diff.attribute);
    }

    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        String message = String.format(MESSAGE, diff.visibility(), diff.element().getClass()
                .getSimpleName(), diff.element().name(), diff.valueA, diff.valueB);
        Severity severity;
        if (diff.visibility() == Visibility.PRIVATE) {
            severity = Severity.INFO;
        } else {
            severity = Severity.MAJOR;
        }
        return new APIStabilityViolation(diff, this, severity, message);
    }
}
