package com.csc108.enginebtc.fix;

import org.apache.commons.math.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.fix42.ExecutionReport;

import java.util.concurrent.ExecutionException;

/**
 * Created by LI JT on 2019/12/2.
 * Description:
 */
public class FixMsgHelper {

    private static final Logger logger = LoggerFactory.getLogger(FixMsgHelper.class);

    public static void displayOrderDetail(ExecutionReport report, SessionID session) {

    }

    public static void displayOrderStatus(ExecutionReport report, SessionID session) throws FieldNotFound {
        StringBuilder sb = new StringBuilder();
        sb.append(report.getOrderID().getValue()).append(": ");
        sb.append(FixMsgHelper.getFixStatusDisplay(report.getOrdStatus())).append(" ");
        sb.append("Cum: ").append(report.getCumQty()).append(" ");
        sb.append("Leaves: ").append(report.getLeavesQty()).append(" ");
        sb.append("AvgPx: ").append(MathUtils.round(report.getAvgPx().getValue(), 3));
        logger.info(sb.toString());

    }


    private static String getFixStatusDisplay(ExecType execType) {
        if (execType.getValue() == '0') return "New";
        if (execType.getValue() == '1') return "PartialFilled";
        if (execType.getValue() == '2') return "Filled";
        if (execType.getValue() == '3') return "Done for day";
        if (execType.getValue() == '4') return "Canceled";
        if (execType.getValue() == '5') return "Replaced";
        if (execType.getValue() == '6') return "PendingCancel";
        if (execType.getValue() == '7') return "Stopped";
        if (execType.getValue() == '8') return "Rejected";
        if (execType.getValue() == '9') return "Suspended";
        if (execType.getValue() == 'A') return "PendingNew";
        if (execType.getValue() == 'B') return "Calculated";
        if (execType.getValue() == 'C') return "Expired";
        if (execType.getValue() == 'D') return "Restated";
        if (execType.getValue() == 'E') return "Pending Replace";
        return "<UNKNOWN>";
    }

    private static String getFixStatusDisplay(OrdStatus ordStatus) {
        if (ordStatus.getValue() == '0') return "New";
        if (ordStatus.getValue() == '1') return "PartialFilled";
        if (ordStatus.getValue() == '2') return "Filled";
        if (ordStatus.getValue() == '3') return "Done for day";
        if (ordStatus.getValue() == '4') return "Canceled";
        if (ordStatus.getValue() == '5') return "Replaced";
        if (ordStatus.getValue() == '6') return "PendingCancel";
        if (ordStatus.getValue() == '7') return "Stopped";
        if (ordStatus.getValue() == '8') return "Rejected";
        if (ordStatus.getValue() == '9') return "Suspended";
        if (ordStatus.getValue() == 'A') return "PendingNew";
        if (ordStatus.getValue() == 'B') return "Calculated";
        if (ordStatus.getValue() == 'C') return "Expired";
        if (ordStatus.getValue() == 'D') return "Accepted for bidding";
        if (ordStatus.getValue() == 'E') return "Pending Replace";
        return "<UNKNOWN>";
    }


}
