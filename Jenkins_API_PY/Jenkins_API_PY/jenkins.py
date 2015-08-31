#  jenkins API
#  @author: Li, Joe (NSN - CN/Cheng Du)
#  @created on: 2015-08-20
#  @version: 1.2
# -*- coding: UTF-8 -*-
import os
import base64, urllib2
import json
import re
import shutil
import socket
import sys
import warnings
from shell import run_shell
from errors import *
from prepare_configure import ConfigParser

USER               = "adaman"
PASS               = "7477cf66b2424ed7b72f961be800a239"
URL                = "https://eslinv70.emea.nsn-net.net:8080"
CONFIG_FILE        = "configs/%s.xml"
BUILD_JOB          = 'job/%(job_name)s/build'
JENKINS_JOB_INFO   = 'job/%(job_name)s/api/json?depth=0'
CONFIG_JOB         = 'job/%(job_name)s/config.xml'
JENKINS_CREATE_JOB = 'createItem?name=%(job_name)s'

class Jenkins(object):
    '''Jenkins class using urllib2
    '''
    def __init__(self, url, user, password):
        ''' initial jenkins necessary pararmeters
        :param url: jenkins server url, ``str``
        :param user: jenkins server user name, ``str``
        :param password: jenkins server user name's passwd, ``str``
        '''
        self.__user = user
        self.__token = password
        self._url = url
        if not self._url[-1] == '/':
            self._url = url + '/'
        self._auth = 'Basic ' + base64.encodestring('%s:%s' % (self.__user, self.__token)).replace('\n', '')

    def __open_jenkins(self, request):
        '''Create a request to jenkins server
        :param request: web request object, ``urllib2.Request``
        '''
        try:
            if self._auth:
                request.add_header('Authorization', self._auth)
                return urllib2.urlopen(request).read()
        except urllib2.HTTPError, e:
            if e.code in [401, 403, 500]:
                raise JenkinsException('Error in request. Possibly authentication failed [%s]'%(e.code))

    def job_exists(self, job_name):
        ''' jenkins job exists or not
        :param job_name: name of jenkins,``str``
        :returns: ``True`` if jenkins job exist
        '''
        try:
            self.get_config_json(job_name)
            return True
        except JenkinsException:
            return False

    def create_job(self, job_name, config_xml):
        '''create jenkins job with config xml file.
        :param job_name: jenkins job name,``str``
        :param config_xml: config.xml path, ``str``
        '''
        if not os.path.exists(config_xml):
            raise JenkinsException("can't find file %s" % config_xml)
        with open(config_xml) as xml:
            data = xml.read()
        if self.job_exists(job_name):
            raise JenkinsException('job[%s] already exists'%(job_name))
        headers = {'Content-Type': 'text/xml'}
        reconfig_url = self._url + JENKINS_CREATE_JOB%locals()
        print self._url + "job/" + job_name
        self.__open_jenkins(urllib2.Request(reconfig_url, data, headers))
        if not self.job_exists(job_name):
            raise JenkinsException('create[%s] failed'%(job_name))

    def reconfig_job(self, job_name, config_xml):
        '''Change configuration of existing Jenkins job.
        :param job_name: name of Jenkins job, ``str``
        :param config_xml: xml configuration path, ``str``
        '''
        with open(config_xml) as xml:
            data = xml.read()
        headers = {'Content-Type': 'text/xml'}
        print self._url + "job/" + job_name
        reconfig_url = self._url + CONFIG_JOB % locals()
        self.__open_jenkins(urllib2.Request(reconfig_url, data, headers))

    def delete_job(self, job_name):
        '''delete jenkins job
        :param job_name: jenkins job name,``str``
        :returns: delete jenkins job name reasult ``str``
        '''
        auth_req = urllib2.Request("%sjob/%s/doDelete" % (self._url, job_name), "")
        return self.__open_jenkins(auth_req)

    def get_config_json(self, job_name):
        '''Get jenkins job json format
        :param job_name: jenkins job name,``str``
        :returns: json format data,``str``
        '''
        try:
            response = self.__open_jenkins(urllib2.Request(self._url + JENKINS_JOB_INFO%locals()))
            if response:
                return json.loads(response)
            else:
                raise JenkinsException('job[%s] does not exist'%job_name)
        except urllib2.HTTPError:
            raise JenkinsException('job[%s] does not exist'%job_name)
        except ValueError:
            raise JenkinsException("Could not parse JSON info for job[%s]"%job_name)

    def get_config_xml(self, job_name):
        '''Get jenkins job xml format
        :param job_name: jenkins job name,``str``
        :returns: xml format result,``str``
        '''
        try:
            response = self.__open_jenkins(urllib2.Request(self._url + CONFIG_JOB%locals()))
            if response:
                file_name = "configs/" + job_name + ".xml"
                self.save_file(file_name,response)
                return file_name
            else:
                raise JenkinsException('job[%s] does not exist'%job_name)
        except urllib2.HTTPError:
            raise JenkinsException('job[%s] does not exist'%job_name)
        except ValueError:
            raise JenkinsException("Could not parse JSON info for job[%s]"%job_name)

    def build(self, job_name):
        '''Build specific jenkins job
        :param job_name: jenkins job name,``str``
        :returns: request result,``str``
        '''
        if not self.job_exists(job_name):
            raise JenkinsException('no such job[%s]'%(job_name))
        return self.__open_jenkins(urllib2.Request(self._url + BUILD_JOB%locals()))

    def save_file(self,file_name,text):
        '''Save text into file.
        :param file_name: file full path,``str``
        :param text: write the content,``str``
        '''
        file = open(file_name,"w")
        file.writelines(text)
        file.close()

    def get_man_job(self,job_name):
        return '.man-'.join(job_name.rsplit('-',1))

    def get_jenkins_name(self,branch,adap_release):
        name_format = 'adaptations_%(branch)s_%(adap_release)s_ris'
        name = name_format % locals()
        if self.job_exists(name):
            return name
        name = 'adaptation_%(branch)s_%(adap_release)s_ris' % locals()
        if self.job_exists(name):
            return name
        if adap_release.__contains__("com.nsn."):
            adap_release = adap_release.split('com.nsn.')[-1]
            name = name_format % locals()
            if self.job_exists(name):
                return name
        if adap_release.__contains__("com.nokia."):
            adap_release = adap_release.split('com.nokia.')[-1]
            name = name_format % locals()
            if self.job_exists(name):
                return name
        if adap_release.__contains__("com.nokianetworks."):
            adap_release = adap_release.split('com.nokianetworks.')[-1]
            name = name_format % locals()
            if self.job_exists(name):
                return name
        raise JenkinsException("jenkins job name doesn't exist %s" % name)

    def get_svn_path(self,job_name):
        if not self.job_exists(job_name):
            raise JenkinsException("jenkins job doesn't exist %s" % job_name)
        try:
            xml = self.get_config_xml(job_name)
            config = ConfigParser(xml)
            svn_path = config.node_text("scm/locations/hudson.scm.SubversionSCM_-ModuleLocation/remote")
            return svn_path
        except SvnException, e:
            raise "xml exception %e" % e

class Trunk(Jenkins):
    def __init__(self, url, user, password):
        super(Trunk,self).__init__(url,user,password)

    def get_jenkins_name(self,adap_release,branch = "trunk"):
        job_name = super(Trunk,self).get_jenkins_name(branch,adap_release)
        return [job_name,super(Trunk,self).get_man_job(job_name)]

    def get_common_part(self,svn_url):
        branch_name = "trunk"
        adaptation_id_value = re.search('trunk/(.*)', svn_url).group(1).strip('/').split('/')[-2]
        adaptation_release_value = re.search('trunk/(.*)', svn_url).group(1).strip('/').split('/')[-1]
        return [branch_name,adaptation_id_value,adaptation_release_value]

    def get_job_by_url(self,url):
        branch_name, adaptation_id, adaptation_release = self.get_common_part(url)
        job_name,man_job_name= self.get_jenkins_name(adaptation_release)
        common_part = "/".join(["/" +branch_name,adaptation_id,adaptation_release])
        return [common_part.strip(),man_job_name.strip(),job_name.strip()]

    def get_job_by_name(self,job_name):
        url = super(Trunk,self).get_svn_path(job_name)
        return [url,self.get_job_by_url(url)]

class Branch(Jenkins):
    def __init__(self, url, user, password):
        super(Branch,self).__init__(url,user,password)

    def get_jenkins_name(self,branch_name,adap_release,branch = "branch"):
        branch =  branch + "_" +branch_name
        job_name =  super(Branch,self).get_jenkins_name(branch,adap_release)
        return [job_name, super(Branch,self).get_Branchob()]

    def get_common_part(self,svn_url):
        branch_name = re.search('branches/(.*)', svn_url).group(1).strip('/').split('/')[-3]
        adaptation_id_value = re.search('branches/(.*)', svn_url).group(1).strip('/').split('/')[-2]
        adaptation_release_value = re.search('branches/(.*)', svn_url).group(1).strip('/').split('/')[-1]
        return [branch_name,adaptation_id_value,adaptation_release_value]

    def get_job_by_url(self,url):
        branch_name, adaptation_id, adaptation_release = self.get_common_part(url)
        job_name,man_job_name = self.get_jenkins_name(branch_name,adaptation_release)
        common_part = "/".join(["/branches/",branch_name,adaptation_id,adaptation_release])
        return [common_part.strip(),man_job_name.strip(),job_name.strip()]

    def get_job_by_name(self,job_name):
        url = super(Branch,self).get_svn_path(job_name)
        return url,self.get_job_by_url(url)