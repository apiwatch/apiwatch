/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.cli;

import java.io.OutputStreamWriter;
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
import org.apiwatch.models.APIScope;
import org.apiwatch.serialization.Serializers;
import org.apiwatch.util.DirectoryWalker;
import org.apiwatch.util.Logging;


public class APIScan {

    public static final String DESCRIPTION = ArgsUtil.VERSION_NAME + "\n\n" +
    		"Analyse source code and extract API information from it. The API data can then " +
    		"be exported to a file or directly uploaded to an APIWATCH server instance.";
    public static final String EPILOG = "You may have to add -- before the positional arguments " +
    		"to separate them from the -i and -x options."; 
    
    public static void main(String[] argv) {
        try {
            Namespace args = parseArgs(argv);
            
            Logger log = Logger.getLogger(APIDiff.class.getName());
            
            log.trace("Finding files to analyse...");
            DirectoryWalker walker = new DirectoryWalker(args.<String> getList("excludes"),
                    args.<String> getList("includes"));
            
            Set<String> files = walker.walk(args.<String>getList("input_paths"));
            APIScope scope = Analyser.analyse(files, args.getAttrs());
            
            if (args.get("output_location") != null) {
                ArgsUtil.putAPIData(scope, args.getString("format"), args.getString("encoding"),
                        args.getString("output_location"), args.getString("username"),
                        args.getString("password"));
            } else {
                OutputStreamWriter writer = new OutputStreamWriter(System.out);
                Serializers.dumpAPIScope(scope, writer, args.getString("format"));
                writer.flush();
                writer.close();
            }
        } catch (HttpException e) {
            Logger.getLogger(APIDiff.class.getName()).error(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Namespace parseArgs(String[] argv) {
        Logging.configureLogging();
        
        String prog = APIScan.class.getSimpleName().toLowerCase();
        ArgumentParser cli = ArgumentParsers.newArgumentParser(prog)
                .description(DESCRIPTION)
                .epilog(EPILOG)
                .version(ArgsUtil.VERSION);
        
        cli.addArgument("input_paths")
                .help("Input file/directory to be analysed")
                .metavar("FILE_OR_DIR")
                .nargs("+");

        cli.addArgument("-l", "--list-languages")
                .help("List supported languages and their default associated file extensions")
                .action(new ArgsUtil.ListLanguagesAction());
        cli.addArgument("-j", "--jobs")
                .help("Number of parallel jobs (default: nbCPU)")
                .dest("jobs")
                .setDefault(Runtime.getRuntime().availableProcessors())
                .type(new ArgsUtil.IntegerArgument());
        
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
        
        ArgumentGroup outputGroup = cli.addArgumentGroup("Output options");
        cli.addArgument("-v", "--verbosity")
                .help("Display logging events starting from this level (default: INFO)")
                .dest("verbosity")
                .setDefault(Level.INFO)
                .choices(ArgsUtil.LOG_LEVELS)
                .type(new ArgsUtil.LogLevelArgument());
        outputGroup.addArgument("-f", "--format")
                .help("API data output format (default: json)")
                .dest("format")
                .setDefault("json")
                .choices(Serializers.availableFormats(APIScope.class));
        outputGroup.addArgument("-o", "--output-location")
                .help("Write analysed API data to this location. Local file paths and URLs are " +
                	  "accepted (If the location is a directory, it will be created if necessary " +
                	  "and the data will be written to a 'api.<format>' file). By default, the " +
                	  "API data is displayed on the standard output.")
                .dest("output_location");
        AuthFileReader auth = new AuthFileReader();
        outputGroup.addArgument("-u", "--server-user")
                .help("Username for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.username)
                .dest("username");
        outputGroup.addArgument("-p", "--server-password")
                .help("User password for HTTP authentication (by default read from ~/.apiwatchrc)")
                .setDefault(auth.password)
                .dest("password");

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
