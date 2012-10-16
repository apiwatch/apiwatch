/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import java.util.Map;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Severity;
import org.apiwatch.models.Visibility;

public class ReducedVisibility implements APIStabilityRule {

    static final String ID = "VIS001";
    static final String NAME = "Reduced Visibility";
    static final String MESSAGE = "Reduced visibility of %s '%s' (%s -> %s)";
    
    private Severity severity = Severity.CRITICAL;
    
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
    public void configure(Map<String, String> properties) throws IllegalArgumentException {
        String severity = properties.get("severity");
        if (severity != null) {
            this.severity = Severity.valueOf(severity);
        }
    }

    @Override
    public boolean isApplicable(APIDifference diff) {
        return diff.changeType == ChangeType.CHANGED && "visibility".equals(diff.attribute);
    }

    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        Visibility visibilityA = diff.elementA.visibility;
        Visibility visibilityB = diff.elementB.visibility;
        if (visibilityA.compareTo(visibilityB) > 0) {
            String message = String.format(MESSAGE, diff.element().getClass().getSimpleName(), diff
                    .element().name(), visibilityA, visibilityB);
            return new APIStabilityViolation(diff, this, severity, message);
        } else {
            return null;
        }
    }

}
