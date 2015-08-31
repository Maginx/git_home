# -*- coding: UTF-8 -*-
from errors import *
import xml.etree.ElementTree as ET


class ConfigParser(object):

    '''config xml file.
    '''

    def __init__(self, source_path, dest_path=""):
        self.config_xml_src = source_path
        self.config_xml_dest = dest_path
        self.tree = ET.parse(self.config_xml_src)

    def modify_text(self, path, text):
        '''Modify the existing text node value
        '''
        elements = self.tree.findall(path)
        if elements.__len__() == 0:
            print 'can not find elements from path: %s' % path
            raise XmlException('not found element')
        else:
            elements[0].text = text
            self.tree.write(self.config_xml_dest)

    def add_node(self, path, node_name, text):
        '''Add a new xml node at the endo of xml file
        '''
        elements = self.tree.findall(path)
        if elements.__len__() == 0:
            print 'can not find elements from path: %s' % path
            raise XmlException('not found element')
        if node_name.__len__() == 0:
            print "Node name is empty"
            raise XmlException('node name empty')
        node = ET.Element(node_name)
        node.text = text
        elements[0].append(node)
        self.tree.write(self.config_xml_dest)

    def add_text(self, path, text):
        '''Add new text value to existing node
        '''
        elements = self.tree.findall(path)
        if elements.__len__() == 0:
            print 'can not find elements from path: %s' % path
            raise XmlException('not found element')
        if elements[0].text.__contains__(text):
            return
        elements[0].text = text + elements[0].text
        self.tree.write(self.config_xml_dest)

    def remove_node(self, path):
        '''Remove node by specific path
        '''
        elements = self.tree.findall(path)
        if elements.__len__() == 0:
            print 'can not find elements from path: %s' % path
            raise XmlException('not found element')
        parent_path = path.rsplit('/', 1)[-2]
        parent_node = self.tree.findall(parent_path)[0]
        parent_node.remove(elements[0])
        self.tree.write(self.config_xml_dest)

    def node_text(self, path):
        '''Get specific node text
        '''
        elements = self.tree.findall(path)
        if elements.__len__() == 0:
            print 'can not find elements from path: %s' % path
            raise XmlException('not found element')
        return elements[0].text
