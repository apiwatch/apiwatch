/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, ABlogiX. All rights reserved.                   *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.cli;

import java.io.OutputStreamWriter;
import java.util.List;
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
import org.apiwatch.cli.ArgsUtil.AuthFileReader;
import org.apiwatch.diff.DifferencesCalculator;
import org.apiwatch.diff.RulesFinder;
import org.apiwatch.diff.ViolationsCalculator;
import org.apiwatch.models.APIDifference;
import org.apiwatch.models.APIScope;
import org.apiwatch.models.APIStabilityViolation;
import org.apiwatch.models.Severity;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.DirectoryWalker;
import org.apiwatch.util.Logging;

public class APIWatch {

    public static final String DESCRIPTION = ArgsUtil.VERSION_NAME + "\n\n" +
            "Analyse source code and extract API information from it. The API data will then " +
            "be compared to a reference (from a file or from an APIWATCH server instance).\n\n" + 
            "The API differences will be be processed through stability rules to check for " +
            "API stability violations.";
    private static final String EPILOG = "You may have to add -- before the positional arguments " +
            "to separate them from the -i and -x options."; 
    
    /**
     * @param args
     */
    public static void main(String[] argv) {
        try {
            Namespace args = parseArgs(argv);
            
            Logger log = Logger.getLogger(APIWatch.class.getName());

            APIScope referenceScope = ArgsUtil.getAPIData(args.getString("reference_api_data"),
                    args.getString("encoding"), args.getString("username"),
                    args.getString("password"));
            
            
            DirectoryWalker walker = new DirectoryWalker(args.<String> getList("excludes"),
                    args.<String> getList("includes"));
            
            Set<String> files = walker.walk(args.<String>getList("input_paths"));
            APIScope newScope = Analyser.analyse(files, args.getAttrs());
            
            log.trace("Calculation of differences...");
            List<APIDifference> diffs = DifferencesCalculator.getDiffs(referenceScope, newScope);

            log.trace("Detection of API stability violations...");
            ViolationsCalculator violationsClac = new ViolationsCalculator(RulesFinder.rules()
                    .values());

            Severity threshold = (Severity) args.get("severity_threshold");
            List<APIStabilityViolation> violations = violationsClac.getViolations(diffs, threshold);

            OutputStreamWriter writer = new OutputStreamWriter(System.out);
            Serializers.dumpViolations(violations, writer, args.getString("format"));
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

    private static Namespace parseArgs(String[] argv) {
        Logging.configureLogging();
        
        String prog = APIWatch.class.getSimpleName().toLowerCase();
        
        ArgumentParser cli = ArgumentParsers.newArgumentParser(prog)
                .description(DESCRIPTION)
                .epilog(EPILOG)
                .version(ArgsUtil.VERSION);
        
        cli.addArgument("reference_api_data")
                .help("Reference API data to use for API violations detection (file or URL)")
                .metavar("REFERENCE");
        cli.addArgument("input_paths")
                .help("Input file/directory to be analysed")
                .metavar("FILE_OR_DIR")
                .nargs("+");

        ArgumentGroup inputGroup = cli.addArgumentGroup("Input options");
        inputGroup.addArgument("-e", "--encoding")
                .help("Source files encoding (default: UTF-8)")
                .dest("encoding")
                .setDefault("UTF-8");
        inputGroup.addArgument("-x", "--exclude")
                .help("Exclude from analysis")
                .dest("excludes")
                .metavar("PATTERN")
                .nargs("+");
        inputGroup.addArgument("-i", "--include")
                .help("Only include in analysis")
                .dest("includes")
                .metavar("PATTERN")
                .nargs("+");
        AuthFileReader auth = new AuthFileReader();
        inputGroup.addArgument("-u", "--server-user")
                .help("Username for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.username)
                .dest("username");
        inputGroup.addArgument("-p", "--server-password")
                .help("User password for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.password)
                .dest("password");
        
        ArgumentGroup outputGroup = cli.addArgumentGroup("Output options");
        outputGroup.addArgument("-v", "--verbosity")
                .help("Display logging events starting from this level (default: INFO)")
                .dest("verbosity")
                .setDefault(Level.INFO)
                .choices(ArgsUtil.LOG_LEVELS)
                .type(new ArgsUtil.LogLevelArgument());
        outputGroup.addArgument("-f", "--format")
                .help("Output format for API stability violations (default: text)")
                .dest("format")
                .setDefault("text")
                .choices(Serializers.availableFormats(APIStabilityViolation.class));
        outputGroup.addArgument("-r", "--only-rules")
                .help("Only consider these API stability rules")
                .dest("only_rules")
                .nargs("+")
                .metavar("RULE_ID");
        outputGroup.addArgument("-R", "--ignored-rules")
                .help("Ignore these API stability rules")
                .dest("ignored_rules")
                .nargs("+")
                .metavar("RULE_ID");
        outputGroup.addArgument("-s", "--severity-threshold")
                .help("Exclude all API stablity violations below this severity level")
                .dest("severity_threshold")
                .setDefault(Severity.INFO)
                .choices(Severity.values())
                .type(new ArgsUtil.SeverityArgument());
        

        Namespace args = null;
        try {
            args = cli.parseArgs(argv);
        } catch (ArgumentParserException e) {
            System.err.println("error: " + e.getMessage());
            System.err.println("use `-h/--help` for syntax");
            System.exit(1);
        }
        
        Logging.configureLogging((Level) args.get("verbosity"));
        
        return args;
    }
}
