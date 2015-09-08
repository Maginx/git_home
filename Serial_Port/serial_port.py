import serial.serialwin32
from setting import IniConfigure

class SerialPort(serial.serialwin32.Win32Serial):
    '''Serial port access object
    '''
    __wirte_code = ""
    def __init__(self,*args,**kwargs):
        '''intial serial port arguments
        '''
        super(SerialPort,self).__init__(*args,**kwargs)
        
    def read_lines(self):
        '''read serial port according to the line
        '''
        try:
            result = super().write(self.__wirte_code)
            if result == 0:
                print "wite data error %s " % self.__wirte_code
        except SerialException as e:
            print "the {0} has a exception {1}".format(self.getPort(),e)
        except writeTimeoutError as e:
            print "write data to {0} failed for wite time out {1},exception details {2}".format(self.getPort(),self.getWriteTimeout(),e)
        try:
            result = super().read()
            if len(result) == 0:
                print "read data failed for {0}".format(self.getPort())
        except SerialException as e:
            print "read data from {0} failed for {1}".format(self.getPort(),e)
        
        

if __name__=="__main__":
    pars = IniConfigure()
    pars.get_section("default","serial_port")