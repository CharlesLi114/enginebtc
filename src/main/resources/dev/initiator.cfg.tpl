
[default]
BeginString=FIX.4.2
ConnectionType=initiator

StartTime=00:00:00
EndTime=20:00:00

UseDataDictionary=Y
DataDictionary=dev/fix.xml
ValidateUserDefinedFields=N

RejectInvalidMessage=N
ValidateFieldsHaveValues=N

FileStorePath=${LogBaseDir}/fix/store
FileLogPath=${LogBaseDir}/fix/fixlog

ResetOnLogon=Y
ResetOnLogout=N
ResetOnDisconnect=N

HeartBtInt=60
ReconnectInterval=5
MaxLatency=600
CheckLatency=N
CheckCompID=Y
FileIncludeMilliseconds=Y
FileIncludeTimeStampForMessages=Y


#[session]
#SocketConnectHost=10.101.237.60
#SocketConnectPort=5071
#SenderCompID=OM_QA_CJYL_SG
#TargetCompID=ENG_QA_60_71

[session]
SocketConnectHost=10.101.195.71
SocketConnectPort=51664
SenderCompID=OM_GROUP4_SG
TargetCompID=ENG_GROUP4_8864






