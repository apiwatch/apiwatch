/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.cli;

import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.http.HttpException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apiwatch.analyser.Analyser;
import org.apiwatch.diff.DifferencesCalculator;
import org.apiwatch.diff.RulesFinder;
import org.apiwatch.diff.ViolationsCalculator;
import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.Severity;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.DirectoryWalker;
import org.apiwatch.util.IO;
import org.apiwatch.util.Logging;

public class APIWatch {

    public static final String DESCRIPTION = Args.VERSION_NAME + "\n\n"
            + "Analyse source code and extract API information from it. The API data will then "
            + "be compared to a reference (from a file or from an APIWATCH server instance).\n\n"
            + "The API differences will be be processed through stability rules to check for "
            + "API stability violations.";

    public static void main(String[] argv) {
        try {
            Namespace args = parseArgs(argv);

            Logger log = Logger.getLogger(APIWatch.class.getName());

            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> rulesConfig = (Map<String, Map<String, String>>) args
                    .get(Args.RULES_CONFIG_OPTION);
            if (rulesConfig != null) {
                RulesFinder.configureRules(rulesConfig);
            }

            APIScope referenceScope = IO.getAPIData(args.getString(REFERENCE_API_DATA),
                    args.getString(Analyser.ENCODING_OPTION), args.getString(Args.USERNAME_OPTION),
                    args.getString(Args.PASSWORD_OPTION));

            DirectoryWalker walker = new DirectoryWalker(
                    args.<String> getList(Args.EXCLUDES_OPTION),
                    args.<String> getList(Args.INCLUDES_OPTION));

            Set<String> files = walker.walk(args.<String> getList(INPUT_PATHS));
            APIScope newScope = Analyser.analyse(files, args.getAttrs());

            log.trace("Calculation of differences...");
            List<APIDifference> diffs = DifferencesCalculator.getDiffs(referenceScope, newScope);

            log.trace("Detection of API stability violations...");
            ViolationsCalculator violationsClac = new ViolationsCalculator(RulesFinder.rules()
                    .values());

            Severity threshold = (Severity) args.get(Args.SEVERITY_THRESHOLD_OPTION);
            List<APIStabilityViolation> violations = violationsClac.getViolations(diffs, threshold);

            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            Serializers.dumpViolations(violations, writer, args.getString(Args.FORMAT_OPTION));
            writer.flush();
            writer.close();

            log.info(violations.size() + " violations.");
        } catch (HttpException e) {
            Logger.getLogger(APIWatch.class.getName()).error(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static final String REFERENCE_API_DATA = "reference_api_data";
    private static final String INPUT_PATHS = "input_paths";

    private static Namespace parseArgs(String[] argv) {
        Logging.configureLogging();

        String prog = APIWatch.class.getSimpleName().toLowerCase();

        ArgumentParser cli = ArgumentParsers.newArgumentParser(prog).description(DESCRIPTION)
                .epilog(Args.EPILOG).version(Args.VERSION);

        cli.addArgument(REFERENCE_API_DATA)
                .help("Reference API data to use for API violations detection (file or URL)")
                .metavar("REFERENCE");
        cli.addArgument(INPUT_PATHS).help("Input file/directory to be analysed")
                .metavar("FILE_OR_DIR").nargs("+");
        Args.listLanguages(cli);
        Args.verbosity(cli);

        ArgumentGroup inputGroup = cli.addArgumentGroup("Input options");
        Args.encoding(inputGroup);
        Args.excludes(inputGroup);
        Args.includes(inputGroup);
        Args.languageExtensions(inputGroup);
        Args.httpAuth(inputGroup);

        ArgumentGroup outputGroup = cli.addArgumentGroup("Output options");
        Args.format(outputGroup, "text");
        Args.rulesConfig(outputGroup);
        Args.severityThreshold(outputGroup);

        Args.analysersOptions(cli);

        Namespace args = null;
        try {
            args = cli.parseArgs(argv);
        } catch (ArgumentParserException e) {
            System.err.println("error: " + e.getMessage());
            System.err.println("use `-h/--help` for syntax");
            System.exit(1);
        }

        Logging.configureLogging((Level) args.get(Args.VERBOSITY_OPTION));

        return args;
    }
}
