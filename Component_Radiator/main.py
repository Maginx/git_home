#!/usr/local/bin/python2.7
# encoding: utf-8
'''
main -- this tool is for generating  json file of radiator 

@author:     Li, Joe (Nokia - CN/Chengdu)

@copyright:  2015 organization_name. All rights reserved.

@license:    license

@contact:    Li, Joe (Nokia - CN/Chengdu)
@deffield    updated: Updated
'''

import sys
import os
import T
from argparse import ArgumentParser
from argparse import RawDescriptionHelpFormatter
from lib.generator import JsonGenerator
import pprint


__all__ = []
__version__ = 0.1
__date__ = '2015-06-02'
__updated__ = '2015-06-02'

DEBUG = 1
TESTRUN = 0
PROFILE = 0

class CLIError(Exception):
    '''Generic exception to raise and log different fatal errors.'''
    def __init__(self, msg):
        super(CLIError).__init__(type(self))
        self.msg = "E: %s" % msg
    def __str__(self):
        return self.msg
    def __unicode__(self):
        return self.msg

def main(argv=None): # IGNORE:C0111
    '''Command line options.'''
    if argv is None:
        argv = sys.argv
    else:
        sys.argv.extend(argv)
    program_name = os.path.basename(sys.argv[0])
    program_version = "v%s" % __version__
    program_build_date = str(__updated__)
    program_version_message = '%%(prog)s %s (%s)' % (program_version, program_build_date)
    program_shortdesc = __import__('__main__').__doc__.split("\n")[1]
    program_license = '''%s

  Created by Li, Joe (Nokia - CN/Chengdu) on %s.
  Copyright 2015 Nokia. All rights reserved.

USAGE
''' % (program_shortdesc, str(__date__))

    try:
        # Setup argument parser
        parser = ArgumentParser(description=program_license, formatter_class=RawDescriptionHelpFormatter)
        parser.add_argument("-C", "--csv", dest="csvfile", required=True, help="input the csv file which include jenkins jobs belong different products")
        parser.add_argument("-J", "--jsonfile", dest="jsonfile", help="output the json to file which include the jenkins jobs belong different products, if not set, will print the json")
        parser.add_argument("-O", "--joblistfile", dest="jobfile", help="output all jenkins jobs name to the file, if not set, will print the jobs")
        parser.add_argument("-W", "--job-name-width", dest="name_width", default="10%",help="set the job columns width")
        parser.add_argument('-V', '--version', action='version', version=program_version_message)
        # Process arguments
        args = parser.parse_args()

        csvfile = args.csvfile
        jsonfile = args.jsonfile
        jobfile = args.jobfile
        name_width = args.name_width
        j = JsonGenerator(csvfile, name_width)
        j.generate()
        if jsonfile:
            j.write(jsonfile)
        else:
            pprint.pprint(j.generate())
        if jobfile:
            j.write_jobs(jobfile)
        else:
            pprint.pprint(j.job_list)
    except KeyboardInterrupt:
        ### handle keyboard interrupt ###
        return 0
    except Exception, e:
        indent = len(program_name) * " "
        sys.stderr.write(program_name + ": " + repr(e) + "\n")
        sys.stderr.write(indent + "  for help use --help")
        return 2

if __name__ == "__main__":
    sys.exit(main())