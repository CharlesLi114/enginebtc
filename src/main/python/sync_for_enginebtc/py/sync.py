"""
@author: LiJT
@file: sync.py
@time: 2019/10/31 14:16
@note:
What group engine is needed is fixed in code.
If update engine version may need to add another group. Add that group to BinsTime and handle correspondingly in @link sync_distribution.


"""

import sys
sys.path.append('D:\projects\J\enginebtc\src\main\python\sync_for_enginebtc')

import numpy as np
import pandas as pd
import datetime as dt
from sqlalchemy import create_engine

from py import utils, config


class JobSync(object):

    def __init__(self, argv):
        print('Working.')
        self.group_date = 0
        self.facts21_date = 0

        self.group_dst_date = 0
        self.facts21_dst_date = 0

        self.trade_days = []
        self.get_dates()

        today = int(dt.date.today().strftime('%Y%m%d'))

        if len(argv) == 1:
            # Default, copy last_day from source database to
            self.group_date = self.get_days_with_offset(today, -1)
            self.facts21_date = self.get_days_with_offset(today, -2)

            self.group_dst_date = today
            self.facts21_dst_date = self.get_days_with_offset(today, -1)


        elif len(argv) == 2:
            # Mode 1, input is trading day of orders, copy to last day.
            # For special trading day, the input is date to use.
            trading_day = int(argv[1])
            self.group_date = trading_day
            self.facts21_date = self.get_days_with_offset(self.group_date, -1)

            self.group_dst_date = today
            self.facts21_dst_date = self.get_days_with_offset(today, -1)

        print("TradingDay " + str(self.group_date))
        print("Today " + str(self.group_dst_date))




    def get_days_with_offset(self, date, offset):
        idx = self.trade_days.index(date)
        last_day = self.trade_days[idx+offset]
        return last_day

    def get_dates(self):
        """
        Get trading days from database, and format as int array.
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
        src_conn.close()

        self.trade_days = days

    @staticmethod
    def get_src_conn():
        return utils.connect_source(config.ProdDB)

    @staticmethod
    def get_dst_conn():
        return utils.connect_source(config.QADB)


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
        print('Data write to ' + table)

    def modify_date(self, data, tradingDay=None):
        if tradingDay is not None:
            data['tradingDay'] = dt.datetime.strptime(str(tradingDay), '%Y%m%d')
        else:
            data['tradingDay'] = dt.datetime.strptime(str(self.facts21_dst_date), '%Y%m%d')

        if 'lastUpdId' in data.columns:
            data['lastUpdId'] = 'Backtest'


    def sync_dailyfacts(self):
        print("Processing DailyFacts.")
        sql = "Select * from DailyFacts where tradingDay = '{0}'".format(self.facts21_date)
        data = self.read_src(sql)

        sql = "Delete from DailyFacts where tradingDay = '{0}'".format(self.facts21_dst_date)
        self.clean_dst(sql)

        self.modify_date(data)
        self.write_dst('DailyFacts', data)


    def sync_facts21(self):
        print("Processing Facts21.")
        sql = "Select * from Facts21 where tradingDay = '{0}'".format(self.facts21_date)
        data = self.read_src(sql)

        sql = "Delete from Facts21 where tradingDay = '{0}'".format(self.facts21_dst_date)
        self.clean_dst(sql)

        self.modify_date(data)
        self.write_dst('Facts21', data)


    def sync_volumebin(self):
        pass


    def sync_distribution(self):
        print("Processing Distribution.")
        sql = "Select * from VolumeDistribution WHERE tradingDay = '{0}'".format(self.facts21_date)
        data = self.read_src(sql)

        sql = "Delete from VolumeDistribution WHERE tradingDay = '{0}'".format(self.facts21_dst_date)
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

        self.modify_date(data)
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
        sql = "Select * from DailyGroups where tradingDay = '{0}'".format(self.group_date)
        data = self.read_src(sql)

        sql = "Delete from DailyGroups where tradingDay = '{0}'".format(self.group_dst_date)
        self.clean_dst(sql)

        self.modify_date(data, self.group_dst_date)
        self.write_dst('DailyGroups', data)


if __name__ == '__main__':
    print(sys.argv)
    # job = JobSync(sys.argv)
    job = JobSync(['0', 20191014])
    job.sync_group()
    job.sync_issue()
    job.sync_dailyfacts()
    job.sync_distribution()

    job.sync_facts21()
    job.sync_volumebin()
    print("Job Done.")


