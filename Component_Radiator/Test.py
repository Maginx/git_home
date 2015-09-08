from string import Template
import lib.T as support
import time,os.path
from setuptools.compat import func_code

class Test(object):
    '''
      for test
    '''
    __name = ""
    name = ""
    def __init__(self, dict, name="wangjian"):
        self.__name = name
        self.name = "j69wang"
        self.dict = dict
      
    def __eq__(self,other):
    
        print __name__
        print self.__hash__()
        print other.__hash__()
        if self.__hash__() == other.__hash__():
            print "They have the same name"

    def get_item(self,i):
        print i
class BatchRename(Template):
    delimiter = '%'

if __name__=="__main__":
    fmt = "Enter rename style (%d-date %n-seqnum %f-format): Ashley_%n%f"
    photos = ['img_1074.jpg','img_1076.jpg','img_1077.jpg']
    t = BatchRename(fmt)
    date = time.strftime('%d%b%y')
    for i,filename in enumerate(photos):
        base,ext = os.path.splitext(filename)
        newname = t.substitude(d=date,n=i,f=ext)
        print "{0}-->{1}".format(filename,newname)
    t1 = Test([1,2,3])
    tt= t1.get_item
    tt(1)
    t1.__doc__
    try:
        #t1.__name
        t1.name
    except Exception as e:
        raise e
    print t1 == Test([1,2,3])
    print t1
    print id(t1)
    print type(t1)
    support.print_content("123")