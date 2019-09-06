package com.csc108.enginebtc.cache;

import com.csc108.enginebtc.commons.AbstractTdbData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class TdbDataCache <T extends AbstractTdbData> {


    private List<T> dataCache;

    // Last passed position
    private int cursor = -1;



    private void initCursor(int timestamp) {
        if (dataCache.size() <= 1) {
            cursor = -1;
            return;
        }
        int i = 1;
        for (; i < dataCache.size(); i++) {
            if (dataCache.get(i).getTime() > timestamp) {
                break;
            }
        }
        cursor = i-1;
    }

    public void setData(T[] data) {
        this.dataCache = Arrays.asList(data);
    }

    public boolean isValid() {
        return cursor >= 0;
    }


    /**
     * Get data from current cursor to upto
     * @param upto timestamp
     */
    public List<T> getData(int upto) {
        if (cursor == dataCache.size()) {
            return null;
        }

        int pos = cursor + 1;
        for ( ; pos < dataCache.size(); pos++) {
            if (dataCache.get(pos).getTime() > upto) {
                break;
            }
        }

        return dataCache.subList(cursor+1, pos);
    }
}
