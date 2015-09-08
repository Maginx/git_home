# encoding: utf-8
'''
Created on 2015年6月1日

@author: j20li
'''
import csv
import re
import json
import sys

class JsonGenerator(object):
    def __init__(self, csvfile, component_width="10%"):
        self.component_width = int(component_width.rstrip("%"))
        if not 0 <= self.component_width <= 100:
            raise ValueError('component_width should not bigger than 100')
        self.job_width = 100 - self.component_width
        self.data_map = {}
        self.json = []
        self.job_list = []
        self._parse(csvfile)
    def generate(self):
        if not self.json:
            self._header()
            self._fragment()
        return self.json
    
    def write(self, outfile):
        with open(outfile, 'w') as f:
            json.dump(self.json, f, indent=2)
    
    def write_jobs(self, outfile):
        with open(outfile, 'w') as f:
            f.write('\n'.join(self.job_list))

    def _parse(self, csvfile):
        line_no = 0
        self.product_list = None
        with open(csvfile, 'rb') as csf:
            spamreader = csv.reader(csf)
            for row in spamreader:
                if line_no == 0:
                    self.product_list = row[1:]
                    line_no += 1
                else:
                    self.data_map[row[0]] = {}
                    for i, k in enumerate(self.product_list):
                        self.data_map[row[0]][k] = row[i+1]
                        if row[i+1]:
                            self.job_list.append(re.search(r'.*job/(.*)',row[i+1].rstrip('/')).group(1))
                        
    def _header(self):
        self.json.append({
        "header" : [{
                "width" : "%s%%" % self.component_width, 
                "names" : ["Component"]
            }, {
                "width" : "%s%%" % self.job_width,
                "names" : self.product_list
            }
        ]
        })
        
    def _fragment(self):
        for component, link_map in self.data_map.items():
            temp_dict = {}
            temp_dict["project_name"] = component
            temp_dict["header"] = {
            "width" : "%s%%" % self.component_width,
            "name" : component,
            "jumplink" : "http://"
            }
            temp_dict["l2jobs"] = {}
            temp_dict["l2jobs"]["width"] = "%s%%" % self.job_width
            temp_dict["l2jobs"]["l2components"] = []
            for product in self.product_list:
                temp_dict["l2jobs"]["l2components"].append({
                    "name" : product,
                    "jobs" : [{
                            "weight" : 3,
                            "name" : component,
                            "url" : re.sub(r'.*job/(.*)', r"jobs/\1.txt", link_map[product].rstrip('/'))
                        }
                    ]
                })
                
            self.json.append(temp_dict)