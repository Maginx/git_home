##
#  @file errors.py
#  @brief : Define exception for ci modules
#  @author: Li, Joe (NSN - CN/Cheng Du)
#  @created on: 2014-01-01
#  @version: 1.0
#

class SvnException(Exception): pass

class ShellException(Exception): pass

class JenkinsException(Exception): pass

class XmlException(Exception): pass

class CommandException(Exception): pass