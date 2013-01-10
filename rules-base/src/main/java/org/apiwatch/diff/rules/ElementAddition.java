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

public class ElementAddition extends RuleBase implements APIStabilityRule {

    static final String ID = "ADD001";
    static final String NAME = "Element Addition";
    static final String MESSAGE = "Added %s %s";

    public ElementAddition() {
        super();
        this.privateSeverity = Severity.INFO;
        this.scopeSeverity = Severity.INFO;
        this.protectedSeverity = Severity.INFO;
        this.publicSeverity = Severity.INFO;
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
        return diff.changeType == ChangeType.ADDED;
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
