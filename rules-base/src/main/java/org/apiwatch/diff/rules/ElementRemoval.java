/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIElement;
import org.apiwatch.models.APIStabilityRule;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Severity;

public class ElementRemoval extends RuleBase implements APIStabilityRule {

    static final String ID = "REM001";
    static final String NAME = "Element Removal";
    static final String MESSAGE = "Removed %s %s";

    public ElementRemoval() {
        super();
        privateSeverity = Severity.INFO;
        scopeSeverity = Severity.CRITICAL;
        protectedSeverity = Severity.CRITICAL;
        publicSeverity = Severity.BLOCKER;
    }

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
        return diff.changeType == ChangeType.REMOVED;
    }

    @Override
    public APIStabilityViolation evaluate(APIDifference diff) {
        APIElement element = diff.element();

        Severity severity;

        switch (element.visibility) {
        case PRIVATE:
            severity = privateSeverity;
            break;
        case SCOPE:
            severity = scopeSeverity;
            break;
        case PROTECTED:
            severity = protectedSeverity;
            break;
        case PUBLIC:
        default:
            severity = publicSeverity;
        }

        String message = String.format(MESSAGE, element.visibility, element.ident());

        return new APIStabilityViolation(diff, this, severity, message);
    }

}
