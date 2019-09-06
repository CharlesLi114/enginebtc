package com.csc108.enginebtc.tdb.models;

/**
 * This enum represents the status of exchange.
 * 
 * @author LI JT
 */
public enum ExchangeStatus {
    /**
     * Auction.
     */
    AUCT,

    /**
     * Trading 
     */
    OBTRD,

    /**
     * Lunch breaks
     */
    BREAK,

    /**
     * Close
     */
    CLOSE,

    /**
     * Suspended
     */
    SUSP,

    /**
     * Halt temporarily
     */
    HALT,

    /**
     * Circuit Break
     */
    CIRCUITBREAK

}
