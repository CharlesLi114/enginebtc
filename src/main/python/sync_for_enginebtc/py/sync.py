"""
@author: LiJT
@file: sync.py
@time: 2019/10/31 14:16
@note: 
"""

import numpy as np
import pandas as pd
import datetime as dt
from sqlalchemy import create_engine

from py import utils, config


class JobSync(object):

    def __init__(self):
        [self.today, self.last_day] = self.get_date()
        if self.today == -1:
            # Today is not a valid trading day.
            return

    @staticmethod
    def get_src_conn():
        return utils.connect_source(config.ProdDB)

    @staticmethod
    def get_dst_conn():
        return utils.connect_source(config.QADB)

    def get_date(self):
        """
        Get date today and last trading day.
        :return:
        """
        src_conn = self.get_src_conn()
        cur = src_conn.cursor()
        sql = "SELECT * FROM dbo.TradeDate WHERE exchange = 'sh'"
        cur.execute(sql)
        arr = cur.fetchall()

        arr = np.array(arr)[:, :]
        days = pd.DataFrame(arr, columns=['exchange', 'tradingDay', 'isHkHalfDay'])
        days = [x.year * 10000 + x.month * 100 + x.day for x in days['tradingDay']]
        today = int(dt.date.today().strftime('%Y%m%d'))

        if today not in days:
            cur.close()
            return [-1, -1]
        idx = days.index(today)
        last_day = days[idx-1]
        cur.close()
        src_conn.close()
        return [today, last_day]



    def sync_dailyfacts(self):
        src_conn = self.get_src_conn()
        cur = src_conn.cursor()
        sql = "Select * from DailyFacts where tradingDay = '{0}'".format(self.last_day)
        cur.execute(sql)
        arr = cur.fetchall()
        arr = np.array(arr)[:, :]
        data = pd.DataFrame(arr, columns=[x[0] for x in cur.description])
        src_conn.close()

        dst_conn = self.get_dst_conn()
        clean_sql = "delete from DailyFacts where tradingDay = '{0}'".format(self.last_day)
        dst_cur = dst_conn.cursor()
        dst_cur.execute(clean_sql)
        dst_conn.commit()
        dst_cur.close()
        dst_conn.close()

        cfg = config.QADB
        engine = create_engine("mssql+pymssql://{}:{}@{}/{}".format(cfg['User'], cfg['Pwd'], cfg['IP']+':'+cfg['Port'], cfg['Database']))
        con = engine.connect()
        data.to_sql('DailyFacts', con=con, if_exists='append', index=False)
        con.close()


    def sync_facts21(self):
        pass


    def sync_volumebin(self):
        pass


    def sync_distribution(self):
        pass


    def sync_issue(self):
        pass


    def sync_group(self):
        pass


if __name__ == '__main__':
    job = JobSync()
    job.sync_dailyfacts()


