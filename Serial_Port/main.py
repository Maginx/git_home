import serial_port
import serial
from setting import IniConfigure
from serial_port import SerialPort
import serial.tools.list_ports_windows
from time import sleep

def get_serial_ports():
    '''get available serial port from *.ini file
    return :serial_port: return all of serial port objects , `list`
    '''
    seri = pars.get_section("default","serial_port")
    if  len(seri) == 0:
        raise ValueError("the serial port config is empty, please check the *.ini file")
    ports = seri.split(',')
    serial_port = []
    for index,port in enumerate(ports):
        serial_port.append(generate_serial_obj(port))
    return serial_port

def generate_serial_obj(port_name):
    '''create a seiral port object and return
    param :port_name: serial port name , `str`
    '''
    buffer_size = pars.get_section(port_name,"buffer_size")
    baudrate = pars.get_section(port_name,"baudrate")
    stopbits = pars.get_section(port_name,"stopbits")
    parity = pars.get_section(port_name,"parity")
    writeTimeout = pars.get_section(port_name,"writeTimeout")
    timeout = pars.get_section(port_name,"timeout")
    serial = SerialPort(port=port_name,baudrate=baudrate,stopbits=stopbits,parity=parity,writeTimeout=writeTimeout,timeout=timeout)
    return serial


if __name__ == "__main__":
    pars = IniConfigure()
    ports = get_serial_ports()
    if len(ports) == 0 :
        raise "no available ports"
    while True:
        for port in enumerate(ports):
            port.read_lines()
        sleep(1)