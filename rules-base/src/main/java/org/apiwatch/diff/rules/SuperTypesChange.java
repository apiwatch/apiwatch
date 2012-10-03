/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import java.util.Set;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.ComplexType;
import org.apiwatch.models.Severity;
import org.apiwatch.models.Visibility;

public class SuperTypesChange implements APIStabilityRule {

    static String ID = "TYP003";
    static String NAME = "Super Type Change";
    static String MESSAGE = "Changed super type of %s type '%s' (%s -> %s)";

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
        return diff.changeType == ChangeType.CHANGED && diff.element() instanceof ComplexType
                && "superTypes".equals(diff.attribute);
    }

    @SuppressWarnings("unchecked")
    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        Set<String> superTypesA = (Set<String>) diff.valueA;
        Set<String> superTypesB = (Set<String>) diff.valueB;

        if (!superTypesB.containsAll(superTypesA)) {
            String message = String.format(MESSAGE, diff.visibility(), diff.name(),
                    superTypesA, superTypesB);
            Severity severity;
            if (diff.visibility() == Visibility.PRIVATE) {
                severity = Severity.INFO;
            } else {
                severity = Severity.MAJOR;
            }
            return new APIStabilityViolation(diff, this, severity, message);
        } else {
            return null;
        }
    }
}
