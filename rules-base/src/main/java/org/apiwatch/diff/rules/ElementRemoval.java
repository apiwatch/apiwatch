/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
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

public class ElementRemoval implements APIStabilityRule {

    static String ID = "REM001";
    static String NAME = "Element Removal";
    static String MESSAGE = "Removed %s %s";

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
            severity = Severity.INFO;
            break;
        case SCOPE:
        case PROTECTED:
            severity = Severity.CRITICAL;
            break;
        default:
            severity = Severity.BLOCKER;
        }
        
        String message = String.format(MESSAGE, element.visibility, element.ident());
        
        return new APIStabilityViolation(diff, this, severity, message);
    }

}
