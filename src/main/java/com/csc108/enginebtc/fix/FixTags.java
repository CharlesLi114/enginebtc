package com.csc108.enginebtc.fix;

/**
 * Created by LI JT on 2017/7/11.
 * Description:
 */
public class FixTags {

    public static final int AccountID = 1;
    public static final int ClOrdID = 11;

    // This tag saves confirmation number from downstream application.
    // Sometimes, downstream application requires this confirmation number to identify an order, not ClOrdID(11) from our system.
    // Not used in code, left here for .
    public static final int OrderID = 37;



    public static final int Price = 44;
    public static final int Text = 58;
    public static final int ExDestination = 100;
    public static final int ClientID = 109;
    public static final int SecurityType = 167;
    public static final int SecurityExchange = 207;
    public static final int SecondaryClOrdID = 526;


    public static final int AlgoType = 6061;
    public static final int EffectiveTime = 6062;
    public static final int ExpireTime = 6063;
    public static final int ParticipationRate = 6064;
    public static final int OPG = 6075;
    public static final int MOC = 6076;
    public static final int MaxPriceLevels = 6305;


    public static final int CounterAccountID = 7001;
    public static final int OnBehalfOfOrderID = 7011;
    public static final int ManualMasterOrdID = 11000;

    public static final int StrategyStatusType = 11200;

    public static final int PeggingFiled = 15012;

}
