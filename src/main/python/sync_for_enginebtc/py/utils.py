"""
@author: LiJT
@file: utils.py
@time: 2019/10/31 14:37
@note: 
"""

import os
import shutil
import pymssql


def connect_source(config):
    conn = pymssql.connect(host=config['IP'], user=config['User'], password=config['Pwd'], database=config['Database'],
                           port=config['Port'])
    return conn



def makedir(root):
    if not os.path.exists(root):
        os.makedirs(root)
    else:
        shutil.rmtree(root)
        os.makedirs(root)
