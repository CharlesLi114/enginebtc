package com.csc108.enginebtc.sessions;

import com.csc108.enginebtc.commons.Exchange;
import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.tdb.models.AuctType;
import com.csc108.enginebtc.tdb.models.ExchangeStatus;
import com.csc108.enginebtc.utils.ConfigUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public class SessionGroup {


    private static final String SESSION_PROPERTY_FILE_NAME = "sessions.properties";
    private static final String SESSION_PROPERTY_NAME = "session.";

    // Index of start time in the line of session property.
    private static final int START_TIME_INDEX = 0;

    // Index of end time in the line of session property.
    private static final int END_TIME_INDEX = 1;

    // Index of exchange status in the line of session property.
    private static final int EXCHANGE_STATUS_INDEX = 2;

    // Index of auction type in the line of session property.
    private static final int AUCTION_TYPE_INDEX = 3;



//    private Exchange
    private Exchange exchange;
    private List<Session> sessions;
    private Map<Integer, Session> sessionMap;


    public SessionGroup(Exchange exchange) {
        this.exchange = exchange;
        this.sessions = new ArrayList<>();
        this.sessionMap = new HashMap<>();
        String configFile = ConfigUtil.getConfigPath(SESSION_PROPERTY_FILE_NAME);
        try {
            Configuration config = new PropertiesConfiguration(configFile);
            List<Object> lines = config.getList(SESSION_PROPERTY_NAME + exchange.toString().toLowerCase());
            for (Object line : lines) {
                Session session = createSession((String) line);
                sessions.add(session);
                for (int i = session.getStartMinute(); i <= session.getEndMinute(); i++) {
                    int minute = i % 100;
                    if (minute < 60) {
                        sessionMap.put(i, session);
                    }
                }
            }
        } catch (ConfigurationException e) {
            throw new InitializationException("Failed to read session configurations.", e);
        }
    }

    /**
     * Create a session based on a line of property file.
     *  #session.sh=StartTime;EndTime;Status;AuctionType
     session.sh=0;914;CLOSE;
     * @param line input session params
     * @return a newly created session.
     */
    private Session createSession(String line) {
        String segments[] = line.split(";", -1);
        Session session = new Session();
        session.setStartMinute(Integer.valueOf(segments[START_TIME_INDEX]));
        session.setEndMinute(Integer.valueOf(segments[END_TIME_INDEX]));
        if (StringUtils.isNotBlank(segments[EXCHANGE_STATUS_INDEX])) {
            session.setStatus(ExchangeStatus.valueOf(segments[EXCHANGE_STATUS_INDEX]));
        }
        if (StringUtils.isNotBlank(segments[AUCTION_TYPE_INDEX])) {
            session.setAuctionType(AuctType.valueOf(segments[AUCTION_TYPE_INDEX]));
        }
        return session;
    }

    public Session getSession(int minute) {
        return this.sessionMap.get(minute);
    }

}
