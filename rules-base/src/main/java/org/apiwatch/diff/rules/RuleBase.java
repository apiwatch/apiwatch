/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.diff.rules;

import java.util.Map;

import org.apiwatch.models.Severity;

public class RuleBase {

    protected Severity privateSeverity = Severity.INFO;
    protected Severity scopeSeverity = Severity.CRITICAL;
    protected Severity protectedSeverity = Severity.CRITICAL;
    protected Severity publicSeverity = Severity.CRITICAL;
    
    public void configure(Map<String, String> properties) throws IllegalArgumentException {
        String privateSeverity = properties.get("privateSeverity");
        if (privateSeverity != null) {
            this.privateSeverity = Severity.valueOf(privateSeverity);
        }
        String scopeSeverity = properties.get("scopeSeverity");
        if (scopeSeverity != null) {
            this.scopeSeverity = Severity.valueOf(scopeSeverity);
        }
        String protectedSeverity = properties.get("protectedSeverity");
        if (protectedSeverity != null) {
            this.protectedSeverity = Severity.valueOf(protectedSeverity);
        }
        String publicSeverity = properties.get("publicSeverity");
        if (publicSeverity != null) {
            this.publicSeverity = Severity.valueOf(publicSeverity);
        }
    }
    
}
