/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.server.views;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apiwatch.diff.DifferencesCalculator;
import org.apiwatch.diff.RulesFinder;
import org.apiwatch.diff.ViolationsCalculator;
import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.ChangeType;
import org.apiwatch.models.Severity;
import org.apiwatch.server.models.Version;
import org.apiwatch.util.PrettyPrinter;
import org.apiwatch.util.errors.Http404;
import org.apiwatch.util.errors.SerializationError;

public class DiffView extends View {



    private String component;
    private String versionA;
    private String versionB;

    public DiffView(HttpServletRequest req, HttpServletResponse resp) {
        super(req, resp);
    }

    @Override
    public void get() throws ServletException, IOException, Http404 {
        try {
            Severity threshold;
            try {
                threshold = Severity.valueOf(request.getParameter("threshold"));
            } catch (Exception e) {
                threshold = Severity.INFO;
            }

            Version verA = Utils.getVersion(component, versionA);
            Version verB = Utils.getVersion(component, versionB);

            Version realVersionA = Utils.resolveRealVersion(verA);
            Version realVersionB = Utils.resolveRealVersion(verB);

            APIScope apiScopeA = realVersionA.getAPIScope();
            APIScope apiScopeB = realVersionB.getAPIScope();

            List<APIDifference> diffs = DifferencesCalculator.getDiffs(apiScopeA, apiScopeB);
            ViolationsCalculator calc = new ViolationsCalculator(RulesFinder.rules().values());

            context.put("page_title", verA + " V.S. " + verB);
            context.put("versionA", verA);
            context.put("versionB", verB);
            context.put("violations", calc.getViolations(diffs, threshold));
            context.put("Severity", Severity.class);
            context.put("ADDED", ChangeType.ADDED);
            context.put("REMOVED", ChangeType.REMOVED);
            context.put("pretty", new PrettyPrinter());

            renderToTemplate(context);
        } catch (SQLException e) {
            throw new ServletException(e);
        } catch (SerializationError e) {
            throw new ServletException(e);
        }
    }

    private static final Pattern URL_REGEXP = Pattern
            .compile("^/([^/]+?)/([^/]+?)/diff/([^/]+?)/$");
    
    @Override
    protected void parseUrl() {
        Matcher matcher = URL_REGEXP.matcher(url);
        urlMatches = matcher.matches();
        if (urlMatches) {
            component = matcher.group(1);
            versionA = matcher.group(2);
            versionB = matcher.group(3);
        }
    }

}
