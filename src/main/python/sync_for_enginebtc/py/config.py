"""
@author: LiJT
@file: config.py
@time: 2019/10/31 14:05
@note: 
"""



ProdDB = {
    'IP': '10.101.28.137',
    'Port': 1433,
    'User': 'chaxun',
    'Pwd': 'chaxun',
    'Database': 'DataService3'
}

ProdTradeDB = {
    'IP': '10.101.28.137',
    'Port': 1433,
    'User': 'chaxun',
    'Pwd': 'chaxun',
    'Database': 'AlgoTradeReport'
}

OrderDate = '20200108'
TradeSql = "SELECT top 100 accountId, orderId, exDestination, symbol, tradingDay, side, type, price, algo, effectiveTime, expireTime, orderQty, participationRate FROM dbo.ClientOrderView WHERE tradingDay = '{}' AND securityType = 'EQA' AND algo != 'POV' AND type = 'Market' AND cumQty > 0.9* orderQty ORDER BY cumQty DESC"
PersistFile = r"D:\projects\J\enginebtc\src\main\resources\dev\orders.csv"

QADB = {
    'IP': '10.101.220.123',
    'Port': 1433,
    'User': 'sa',
    'Pwd': 'sa',
    'Database': 'DataService3Bter'
}