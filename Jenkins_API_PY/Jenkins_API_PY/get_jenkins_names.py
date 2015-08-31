import re
import os
import sys
import shutil
import datetime
from jenkins import Jenkins as Jenkins_Url
from prepare_configure import *
from errors import *
from shell import run_shell
USER = "j69wang"
PASS = "P@ssword123"
URL = "https://eslinv70.emea.nsn-net.net:8080"

def get_branch_job_name(branch_name,adaptation_release):
  tmp = 'adaptation_branch_' + branch_name  + '_' + adaptation_release +'_ris'
  jenkins = Jenkins_Url(URL,USER,PASS)
  if jenkins.job_exists(tmp):
    return tmp
  tmp = 'adaptations_branch_' + branch_name  + '_' + adaptation_release +'_ris'
  if jenkins.job_exists(tmp):
    return tmp
  print "Jenkins doesn't exist : " + tmp
  log("FAILED"," jenkins name dosen't exits " + tmp)
  return 0

def get_job_name(adaptation_release):
  tmp = 'adaptations_trunk_' + adaptation_release +'_ris'
  jenkins = Jenkins_Url(URL,USER,PASS)
  if jenkins.job_exists(tmp):
    return tmp
  tmp = 'adaptation_trunk_' + adaptation_release +'_ris'
  if jenkins.job_exists(tmp):
    return tmp
  print "Jenkins doesn't exist: " + tmp
  log("FAILED"," jenkins name dosen't exits " + tmp)
  return 0

def trunk_jenkins(release_name):
	return get_job_name(release_name)

def n16_jenkins(release_name):
  return get_branch_job_name("n16",release_name)

def n15_5_jenkins(release_name):
	return get_branch_job_name("n15-5",release_name)

def log(file_name,text):
  f = file(file_name,"a+")
  f.writelines(text + "\n")
  f.close()

def generate_jenkins(release):
	jenkins = Jenkins_Url(URL,USER,PASS)
	trunk = trunk_jenkins(release)
	if jenkins.job_exists(trunk):
	  log("trunk","https://eslinv70.emea.nsn-net.net:8080/job/" + trunk)
	else:
		log("failed","trunk " + release)

	n15_5 = n15_5_jenkins(release)
	if jenkins.job_exists(n15_5):
	  log("n15.5","https://eslinv70.emea.nsn-net.net:8080/job/" + n15_5)
	else:
		log("failed","n15.5 " + release)

	n16 = n16_jenkins(release)
	if jenkins.job_exists(n16):
	  log("n16","https://eslinv70.emea.nsn-net.net:8080/job/" + n16)
	else:
		log("failed","n16 " + release)

def main():
	text = os.sys.argv[1]
	releases = text.split(',')
	print releases
	for release in releases:
		generate_jenkins(release)

if __name__ == '__main__':
	main()