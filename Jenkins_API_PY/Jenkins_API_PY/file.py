class File(object):
    @classmethod
    def log(Class,file_name,text):
        now = datetime.datetime.now()
        f=file(file_name,"a+")
        try:
            f.writelines(now.strftime("%Y-%m-%d %H:%M:%S ") + text + "\n")
        except IOException,e:
            print "File IO exception " + e
        finally:
            f.close()

    @classmethod
    def open_files(Class,file_path):
        fp = open(file_path,"r+")
        urls = fp.readlines()
        print "Jenkins or URLS"
        print urls
        return urls