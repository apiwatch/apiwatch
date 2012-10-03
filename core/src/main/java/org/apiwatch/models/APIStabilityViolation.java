/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

public class APIStabilityViolation {

    public APIDifference difference;
    public APIStabilityRule rule;
    public Severity severity;
    public String message;

    public APIStabilityViolation(APIDifference difference, APIStabilityRule rule,
            Severity severity, String message)
    {
        this.difference = difference;
        this.rule = rule;
        this.severity = severity;
        this.message = message;
    }

    public static final String MSG_FORMAT = "[%s] <%s> %s @ '%s'";

    @Override
    public String toString() {
        return String.format(MSG_FORMAT, severity, rule.id(), message,
                difference.element().sourceFile);
    }

}
