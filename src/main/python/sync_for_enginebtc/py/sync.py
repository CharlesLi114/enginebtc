"""
@author: LiJT
@file: sync.py
@time: 2019/10/31 14:16
@note:
What group engine is needed is fixed in code.
If update engine version may need to add another group. Add that group to BinsTime and handle correspondingly in @link sync_distribution.


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
        print("Prepare Date.")
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

    def read_src(self, sql):
        src_conn = self.get_src_conn()
        cur = src_conn.cursor()
        cur.execute(sql)
        arr = cur.fetchall()
        arr = np.array(arr)[:, :]
        data = pd.DataFrame(arr, columns=[x[0] for x in cur.description])
        src_conn.close()
        return data

    def clean_dst(self, sql):
        dst_conn = self.get_dst_conn()
        dst_cur = dst_conn.cursor()
        dst_cur.execute(sql)
        dst_conn.commit()
        dst_cur.close()
        dst_conn.close()

    def write_dst(self, table, data):
        cfg = config.QADB
        engine = create_engine("mssql+pymssql://{}:{}@{}/{}".format(cfg['User'], cfg['Pwd'], str(cfg['IP']) + ':' + str(cfg['Port']), cfg['Database']))
        con = engine.connect()
        data.to_sql(table, con=con, if_exists='append', index=False)
        con.close()

    def sync_dailyfacts(self):
        print("Processing DailyFacts.")
        sql = "Select * from DailyFacts where tradingDay = '{0}'".format(self.last_day)
        data = self.read_src(sql)

        sql = "Delete from DailyFacts where tradingDay = '{0}'".format(self.last_day)
        self.clean_dst(sql)

        self.write_dst('DailyFacts', data)


    def sync_facts21(self):
        print("Processing Facts21.")
        sql = "Select * from Facts21 where tradingDay = '{0}'".format(self.last_day)
        data = self.read_src(sql)

        sql = "Delete from Facts21 where tradingDay = '{0}'".format(self.last_day)
        self.clean_dst(sql)

        self.write_dst('Facts21', data)


    def sync_volumebin(self):
        pass


    def sync_distribution(self):
        print("Processing Distribution.")
        sql = "Select * from VolumeDistribution WHERE tradingDay = '{0}'".format(self.last_day)
        data = self.read_src(sql)

        sql = "Delete from VolumeDistribution WHERE tradingDay = '{0}'".format(self.last_day)
        self.clean_dst(sql)

        max_bin_index = 0
        bin_cols = []
        for col in data.columns:
            if col.startswith('bin'):
                max_bin_index = int(col.replace("bin", ''))
                bin_cols.append(col)
        for index, row in data.iterrows():
            # Don't support HK, so skip it.
            if row['groupFlag'] == 'HK':
                continue
            v_bins = list(row[bin_cols][row[bin_cols] != 0])
            while len(v_bins) < max_bin_index:
                v_bins.append(-1.0)
            row[bin_cols] = v_bins
        self.write_dst('VolumeDistribution', data)


    def sync_issue(self):
        print("Processing IssueType.")
        sql = 'Select * from IssueType'
        data = self.read_src(sql)

        sql = 'Delete from IssueType'
        self.clean_dst(sql)
        data['stockName'] = data['symbol']

        self.write_dst('IssueType', data)


    def sync_group(self):
        print("Processing DailyGroups.")
        sql = "Select * from DailyGroups where tradingDay = '{0}'".format(self.today)
        data = self.read_src(sql)

        sql = "Delete from DailyGroups where tradingDay = '{0}'".format(self.today)
        self.clean_dst(sql)

        self.write_dst('DailyGroups', data)


if __name__ == '__main__':
    job = JobSync()
    job.sync_distribution()
    job.sync_dailyfacts()
    job.sync_facts21()

    job.sync_group()
    job.sync_issue()
    job.sync_volumebin()
    print("Job Done.")


