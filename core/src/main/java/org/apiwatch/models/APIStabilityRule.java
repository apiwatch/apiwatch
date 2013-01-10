/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.models;

import java.util.Map;

public interface APIStabilityRule {

    String id();

    String name();

    String description();

    void configure(Map<String, String> properties) throws IllegalArgumentException;

    boolean isApplicable(APIDifference diff);

    APIStabilityViolation evaluate(APIDifference diff);

}
