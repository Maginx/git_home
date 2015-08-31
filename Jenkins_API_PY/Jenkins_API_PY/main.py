# Modify jenkins setting and create man-jenkins for ADM feature.
# @Author : Jeremy Wang(j69wang)*
# @Email : jeremy.wang@nokia.com
# -*- coding: UTF-8 -*-
import re
import os
import sys
import shutil
import datetime
from jenkins import *
from prepare_configure import *
from errors import *
from shell import run_shell
from file import *

SUCCESS_LOG = "jenkins_success"
FAILED_LOG  = "jenkins_failed"
SVN_FAILED  = "svn_failed"
NO_MAN = '''
scm/excludedRegions==%(common_part)so2ml/content/*.man
%(common_part)so2ml/content/amanual/*.man,
scm/includedRegions==%(common_part)so2ml/content/.*
%(common_part)srobot/src/.*
%(common_part)srobot/robotsources.xml,
publishers/hudson.tasks.junit.JUnitResultArchiver/testResults==TEST-unittest.suite.TestSuite.xml,
command==echo \"mpp_build_type=o2ml\" > build-type.conf;'''
MAN = '''
scm/excludedRegions==,
scm/includedRegions==%(common_part)so2ml/content/*.man
%(common_part)so2ml/content/amanual/*.man,
role==hudson.model.Item.Read:I_EXT_TERMINATORS,
role==hudson.model.Item.Workspace:I_EXT_TERMINATORS,
role==hudson.model.Item.Build:I_EXT_TERMINATORS,
role==hudson.model.Item.Configure:I_EXT_TERMINATORS,
publishers/hudson.plugins.robot.RobotPublisher==,
publishers/hudson.tasks.junit.JUnitResultArchiver/testResults==TEST-unittest.suite.TestSuite.xml,
command==echo \"mpp_build_type=man\" > build-type.conf;'''


def update_jenkins(job_name, properties):
    print "Jenkins with o2ml : " + job_name
    jenkins = Jenkins(URL, USER, PASS)
    if not jenkins.job_exists(job_name):
        print job_name + "doesn't exits"
        File.log(FAILED_LOG, job_name + " doesn't exist")
        return
    temp_file = jenkins.get_config_xml(job_name)
    config_name = jenkins.get_man_job(job_name)
    dest_file = CONFIG_FILE % config_name
    if os.path.exists(dest_file):
        os.remove(dest_file)
    shutil.copyfile(temp_file, dest_file)
    for pro in properties.split(','):
        modify_xml(pro.strip().split('==')[0], pro.strip().split('==')[1], temp_file)
    try:
        jenkins.reconfig_job(job_name, temp_file)
        File.log(SUCCESS_LOG, job_name)
    except JenkinsException:
        File.log(FAILED_LOG, job_name)
        print "UPDATE JENKINS FAILED " + job_name
    return temp_file


def create_jenkins(job_name, properties):
    print "Jenkins with man : " + job_name
    jenkins = Jenkins(URL, USER, PASS)
    dest_file = CONFIG_FILE % job_name
    for pro in properties.split(','):
        modify_xml(pro.strip().split('==')[0], pro.strip().split('==')[1], dest_file)
    try:
        if jenkins.job_exists(job_name):
            jenkins.delete_job(job_name)
        jenkins.create_job(job_name, dest_file)
        File.log(SUCCESS_LOG, job_name)
    except JenkinsException:
        File.log(FAILED_LOG, job_name)
        print "CREATE JENKINS FAILED " + job_name


def modify_xml(tag_name, text, temp_file):
    config = ConfigParser(temp_file, temp_file)
    try:
        if tag_name == "role":
            config.add_node(
                "properties/hudson.security.AuthorizationMatrixProperty",
                "permission", text)
            return
        if tag_name == "command":
            config.add_text("builders/hudson.tasks.Shell/command", text)
            return
        config.modify_text(tag_name, text)
    except XmlException:
        print "XML EXCEPTION : " + tag_name + "=" + text


def exist_man_page(path):
    retcode, stdout, stderr = run_shell("svn ls " + path, True)
    if not stdout.__contains__("pom.xml"):
        return True
    print "ERROR : " + path.strip() + " still has the pom.xml \n"
    File.log(SVN_FAILED, path.strip() + " still has the pom.xml")
    return False


def get_job_by_name(item):
    if item.__contains__("trunk"):
        return Trunk(URL, USER, PASS).get_job_by_name(item)
    else:
        return Branch(URL, USER, PASS).get_job_by_name(item)


def get_job_by_url(item):
    if item.__contains__("trunk"):
        return item, Trunk(URL, USER, PASS).get_job_by_url(item)
    else:
        return item, Branch(URL, USER, PASS).get_job_by_url(item)


def get_job(mode, item):
    if mode == "-j":
        return get_job_by_name(item)
    elif mode == "-u":
        return get_job_by_url(item)
        if job_name == 0:
            print "ERROR : don't find out " + item + " jenkins name \n"
            return
    else:
        raise CommandException("Command invalid")

if __name__ == '__main__':
    print sys.argv
    if len(sys.argv) != 5:
        raise ValueError("Arguments wrong")
    mode = sys.argv[1]
    file_path = sys.argv[2]
    USER = sys.argv[3]
    PASS = sys.argv[4]
    i = 1
    items = file_path.split(',')
    if os.path.exists(FAILED_LOG):
        os.remove(FAILED_LOG)
    print "--------begin--------"
    print items
    for item in items:
        svn_path, common_part, man_job_name, job_name = get_job(mode, item)
        # verify the svn exist pom.xml
        if not exist_man_page(svn_path):
            continue
        common_part = ""
        # change to noman jenkins
        tmp_str = NO_MAN % locals()
        if not job_name.strip():
            print "no jenkins job " + item
            continue
        print "-----%(i)s : %(job_name)s-----" % locals()
        config_file = update_jenkins(job_name, tmp_str)
        # create man jenkins if svn has pom-man.xml
        tmp_str = MAN % locals()
        create_jenkins(man_job_name, tmp_str)
        printi = i + 1
    if os.path.exists(FAILED_LOG):
        raise "There are some component failed."
    print "--------end--------"
