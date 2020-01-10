"""
@author: LiJT
@file: TradePersist.py
@time: 2020/1/8 18:01
@note: 
"""

import pandas as pd
import numpy as np
from py import utils, config
from py.config import TradeSql
import datetime as dt


class TradeReader(object):

    def __init__(self):
        pass

    @staticmethod
    def get_src_conn():
        return utils.connect_source(config.ProdTradeDB)

    def read_src(self, sql):
        src_conn = self.get_src_conn()
        cur = src_conn.cursor()
        cur.execute(sql)
        arr = cur.fetchall()
        arr = np.array(arr)[:, :]
        data = pd.DataFrame(arr, columns=[x[0] for x in cur.description])
        src_conn.close()
        return data

    def persist_orders(self, date):
        sql = TradeSql.format(date)
        data = self.read_src(sql)
        data['effectiveTime'] = [dt.datetime.strftime(x, '%Y-%m-%d %H:%M:%S') + '.000' for x in data['effectiveTime']]
        data['expireTime'] = [dt.datetime.strftime(x, '%Y-%m-%d %H:%M:%S') + '.000' for x in data['expireTime']]
        data.to_csv(config.PersistFile, header=False, index=False)
        print("Selected orders written to " + config.PersistFile)


if __name__ == '__main__':
    reader = TradeReader()
    reader.persist_orders('20190101')
