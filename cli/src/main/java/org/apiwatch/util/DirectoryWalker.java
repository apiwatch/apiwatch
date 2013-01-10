/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry. All rights reserved.               *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package org.apiwatch.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class DirectoryWalker {

    public static final String[] DEFAULT_EXCLUDES = new String[] { ".svn", ".hg", ".git", ".bzr",
            "CVS", "RCS", "SCCS" };

    private FileFilter filter;
    
    public DirectoryWalker(List<String> excludes, List<String> includes) {
        Set<String> excludeSet = new HashSet<String>();
        excludeSet.addAll(Arrays.asList(DEFAULT_EXCLUDES));
        if (excludes != null) {
            excludeSet.addAll(excludes);
        }
        filter = new GlobFilter(excludeSet, includes);
    }

    public Set<String> walk(List<String> paths) {
        Set<String> files = new TreeSet<String>();

        for (String path : paths) {
            File filePath = new File(path);
            File[] listFiles = filePath.listFiles(filter);
            if (listFiles != null) {
                for (File f : listFiles) {
                    if (f.isDirectory()) {
                        files.addAll(walk(Arrays.asList(f.getPath())));
                    } else {
                        files.add(f.getPath());
                    }
                }
            } else {
                if (filePath.isFile() && filter.accept(filePath)) {
                    files.add(filePath.getPath());
                }
            }
        }

        return files;
    }

    public class GlobFilter implements FileFilter {
        
        private List<Pattern> excludePatterns;
        private List<Pattern> includePatterns;
        
        public GlobFilter(Collection<String> excludes, Collection<String> includes) {
            excludePatterns = new ArrayList<Pattern>();
            includePatterns = new ArrayList<Pattern>();
            if (excludes != null) {
                for (String exc : excludes) {
                    excludePatterns.add(createPatternFromGlob(exc));
                }
            }
            if (includes != null) {
                for (String inc : includes) {
                    includePatterns.add(createPatternFromGlob(inc));
                }
            }
        }
        
        @Override
        public boolean accept(File file) {

            if (excludePatterns.size() == 0 && includePatterns.size() == 0) {
                return true;
            } else {
                String path = file.getPath();
                for (Pattern pattern : excludePatterns) {
                    if (pattern.matcher(path).find()) {
                        return false;
                    }
                }
                if (includePatterns.size() == 0 || file.isDirectory()) {
                    return true;
                } else {
                    for (Pattern pattern : includePatterns) {
                        if (pattern.matcher(path).find()) {
                            return true;
                        }
                    }
                    return false;
                }
            }
        }
        
        /**
         * Translate a shell PATTERN to a regular expression.
         * 
         * There is no way to quote meta-characters.
         */
        private Pattern createPatternFromGlob(String glob) {

            int i = 0;
            int n = glob.length();
            char c;
            
            StringBuilder regexp = new StringBuilder();
            while (i < n) {
                c = glob.charAt(i);
                if (c == '*' && i + 1 < n && glob.charAt(i + 1) == '*') {
                    regexp.append(".*");
                    i += 2;
                } else {
                    switch (c) {
                    case '*':
                        regexp.append("[^/\\\\]*");
                        break;
                    case '?':
                        regexp.append("[^/\\\\]");
                        break;
                    case '.':
                        regexp.append("\\.");
                        break;
                    case '\\':
                        regexp.append("\\\\");
                        break;
                    default:
                        regexp.append(c);
                    }
                    i += 1;
                }
            }
            regexp.append('$');

            return Pattern.compile(regexp.toString());
        }

    }


}
