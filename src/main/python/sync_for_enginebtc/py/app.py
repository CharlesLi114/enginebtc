"""
@author: LiJT
@file: app.py
@time: 2020/1/8 18:00
@note: 
"""
import sys

from py.config import OrderDate
from py.sync import JobSync
from py.tradereader import TradeReader

if __name__ == '__main__':
    if len(sys.argv) == 2:
        mode = sys.argv[1]
    else:
        mode = 'default'

    # Could be simplified, leave it to keep it easy to read.
    if len(sys.argv) == 2:
        if mode.upper() == 'COPYTODAY' or mode.upper() == 'COPYLAST':
            job = JobSync([mode.upper()])
            job.sync_group()
            job.sync_issue()
            job.sync_dailyfacts()
            job.sync_distribution()

            job.sync_facts21()
            job.sync_volumebin()
            print("Job Done.")
        else:
            # input is a number.
            orderdate = int(mode)
            reader = TradeReader()
            reader.persist_orders(orderdate)

            job = JobSync([orderdate])
            job.sync_group()
            job.sync_issue()
            job.sync_dailyfacts()
            job.sync_distribution()

            job.sync_facts21()
            job.sync_volumebin()
            print("Job Done.")
    else:
        # Use system date.
        reader = TradeReader()
        reader.persist_orders(OrderDate)

        job = JobSync([OrderDate])
        job.sync_group()
        job.sync_issue()
        job.sync_dailyfacts()
        job.sync_distribution()

        job.sync_facts21()
        job.sync_volumebin()
        print("Job Done.")