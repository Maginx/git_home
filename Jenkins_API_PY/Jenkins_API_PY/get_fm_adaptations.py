# Modify jenkins setting and create man-jenkins for ADM feature.
# @Author : Jeremy Wang(j60wang)
# @Email : jeremy.wang@nokia.com
# -*- coding: UTF-8 -*-
import re
import os
import sys
import shutil
import datetime
from jenkins import Jenkins as Jenkins_Url
from prepare_configure import *
from errors import *
from shell import run_shell
USER   = "adaman"
PASS   = "7477cf66b2424ed7b72f961be800a239"
URL    = "https://eslinv70.emea.nsn-net.net:8080"
JOBS   = "jenkins-job"
FAILED = "failed"

def get_job_name(adaptation_release):
  tmp = 'adaptations_trunk_' + adaptation_release +'_ris'
  jenkins = Jenkins_Url(URL,USER,PASS)
  if jenkins.job_exists(tmp):
    return tmp
  tmp = 'adaptation_trunk_' + adaptation_release +'_ris'
  if jenkins.job_exists(tmp):
    return tmp
  if adaptation_release.__contains__("com.nsn."):
    tmp = 'adaptations_trunk_' + adaptation_release.split('com.nsn.')[-1] +'_ris'
    if jenkins.job_exists(tmp):
      return tmp
  if adaptation_release.__contains__("com.nokia."):
    tmp = 'adaptations_trunk_' + adaptation_release.split('com.nokia.')[-1] +'_ris'
    if jenkins.job_exists(tmp):
      return tmp
  if adaptation_release.__contains__("com.nokianetworks."):
    tmp = 'adaptations_trunk_' + adaptation_release.split('com.nokianetworks.')[-1] +'_ris'
    if jenkins.job_exists(tmp):
      return tmp
  print "Jenkins doesn't exist: " + tmp
  return 0

def log(file_name,text):
  now = datetime.datetime.now()
  f=file(file_name,"a+")
  f.writelines( text + "\n")
  f.close()

def get_release_urls(path):
  i = 1
  release_names = {}
  retcode, stdout, stderr = run_shell("svn ls " + path,True)
  for adaptation_id in stdout.strip(' ').split('\n'):
    if len(adaptation_id) == 0:
      continue
    code,releases,stderr = run_shell("svn ls " + path + adaptation_id,True)
    for release_id in releases.strip(' ').split('\n'):
      if len(release_id) == 0:
        continue
      else:
        if not re.match(r'.*(ADAPSUP|PM|root|robot|mediation|\.project|mei)',release_id,re.I):
          release_names[release_id] = path + adaptation_id + release_id
          print  "%(i)s " % locals() + path + adaptation_id + release_id
          i = i + 1
  return release_names

if __name__ == '__main__':
  svn_path = ""
  if os.path.exists(JOBS):
    os.remove(JOBS)
  if os.path.exists(FAILED):
    os.remove(FAILED)
  print sys.argv
  if len(sys.argv) == 2:
    svn_path = sys.argv[1]
  if len(sys.argv) == 4:
    svn_path = sys.argv[1]
    USER = sys.argv[2]
    PASS = sys.argv[3]

  releases = get_release_urls(svn_path)
  print releases
  for release in releases:
    job_name = get_job_name(release.strip('/'))
    if not job_name == 0:
      log(JOBS,job_name)
    else:
      log(FAILED,releases[release])
