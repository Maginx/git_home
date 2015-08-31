##
#  @file shell.py
#  @brief : execute shell command module
#  @author: Li, Joe (NSN - CN/Cheng Du)
#  @created on: 2014-05-06
#  @version: 1.0
#

import subprocess
from errors import ShellException

def run_shell(cmd, shell=False, input=None):
    p = subprocess.Popen(cmd, stdin = subprocess.PIPE,
                              stdout = subprocess.PIPE,
                              stderr = subprocess.PIPE, shell=shell)
    stdout,stderr = p.communicate(input)
    retcode = p.poll()
    return (retcode, stdout, stderr)

def check_shell(cmd, shell=False, input=None):
    retcode, stdout, stderr = run_shell(cmd, shell, input)
    if retcode:
        if stderr:
            msg = stderr
        else:
            msg = "exec command : %s failed with status %s" % (cmd, retcode)
        raise ShellException(msg)
    return stdout
