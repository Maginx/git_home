import ConfigParser
from ConfigParser import NoSectionError, NoOptionError
from serial.serialutil import SerialBase

class IniConfigure(object):
    '''config the ini configure file
    '''

    def __init__(self, file_name="serial_port.ini"):
        '''initial some necessary arguments.
        '''
        self.file_name = file_name 
        if not self.file_name.endswith('.ini'):
            print "the file format is error '{0}'".format(file_name)
        self.__config = ConfigParser.ConfigParser();

    def get_section(self, name, option):
        '''get ini file option value.
        '''
        if not (name or option):
            print "the parameters are empty!"
            return
        try:
            with open(self.file_name) as ini:
                self.__config.readfp(ini)
                if name == "parity":
                    return self.__config.get(name,self.__enum_parity(option))
                if name == "stopbits":
                    return self.__config.get(name,self.__enum_stopbits(option))
                return self.__config.get(name,option)
        except Exception, e: 
            print e

    def __enum_parity(self, content):
        '''get parity enum value by input arguments
        '''
        for key,value in enumerate(PARITY_NAMES):
            if str.capitalize(content) == value :
                return key

    def __enum_stopbits(self, content):
        '''get stopbits enum value by input arguments
        '''
        for key,value in enumerate(SerialBase.STOPBITS):
            if content == value:
                return key