#!/bin/env python
# * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * #
# Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      #
#                                                                     #
# This file is part of APIWATCH and published under the BSD license.  #
#                                                                     #
# See the "LICENSE" file for more information.                        #
# * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * #

import os, sys, glob

JAVA_LICENSE = """\
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
"""
XML_LICENSE = """\
<!--
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      *
 *                                                                     *
 * This file is part of APIWATCH and published under the BSD license.  *
 *                                                                     *
 * See the "LICENSE" file for more information.                        *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 -->
"""
SCRIPT_LICENSE = """\
# * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * #
# Copyright (c) 2012, Robin Jarry, ABlogiX. All rights reserved.      #
#                                                                     #
# This file is part of APIWATCH and published under the BSD license.  #
#                                                                     #
# See the "LICENSE" file for more information.                        #
# * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * #
"""

LICENSED_FILES = {
    '.java': JAVA_LICENSE,
    '.xml': XML_LICENSE,
    '.properties': SCRIPT_LICENSE,
    '.vm': XML_LICENSE,
}

def check_file(file, do_modify):
    _, ext = os.path.splitext(file)
    missing = 0
    inserted = 0
    if ext in LICENSED_FILES.keys():
        license = LICENSED_FILES[ext]
        
        with open(file, 'r') as fd:
            buffer = fd.read()
        
        if license not in buffer:
            missing = 1
            if do_modify:
                with open(file, 'w') as fd:
                    if buffer.startswith('#!') or buffer.startswith('<?xml') :
                        lines = buffer.splitlines() 
                        header = lines[0] + '\n'
                        buffer = '\n'.join(lines[1:])
                    else:
                        header = ''
                    fd.write(header + license + buffer)
                print "License inserted in", file
                inserted = 1
            else:
                print "No license header in", file
    return missing, inserted

def main():

    do_modify = len(sys.argv) > 1 and sys.argv[1] == "--modify"

    missing = 0
    inserted = 0
    
    for pom in glob.glob('*/pom.xml'):
        miss, ins = check_file(pom, do_modify)
        missing += miss
        inserted += ins
    for source_folder in glob.glob('*/src'):
        for root, dirs, files in os.walk(source_folder):
            for file in files:
                miss, ins = check_file(os.path.join(root, file), do_modify)
                missing += miss
                inserted += ins
                    
    print
    print missing, "license headers missing.", inserted, "inserted"


if __name__ == "__main__":
    main()
