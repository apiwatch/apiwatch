/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
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
import org.apiwatch.models.Function;
import org.apiwatch.models.Severity;
import org.apiwatch.models.Visibility;

public class FunctionTypeChange implements APIStabilityRule {

    static String ID = "TYP002";
    static String NAME = "Function Return Type Change";
    static String MESSAGE = "Changed return type of %s function '%s' (%s -> %s)";

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
        return diff.changeType == ChangeType.CHANGED && diff.element() instanceof Function
                && "returnType".equals(diff.attribute);
    }

    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        String message = String.format(MESSAGE, diff.visibility(), diff.name(),
                diff.valueA, diff.valueB);
        Severity severity;
        if (diff.visibility() == Visibility.PRIVATE) {
            severity = Severity.INFO;
        } else {
            severity = Severity.CRITICAL;
        }
        return new APIStabilityViolation(diff, this, severity, message);
    }
    
}
