package com.csc108.enginebtc.cache;

import com.csc108.enginebtc.commons.AbstractTdbData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LI JT on 2019/9/2.
 * Description:
 */
public class TdbDataList<T extends AbstractTdbData> {

    private String stockId;
    private List<T> dataList;

    // Last passed position
    private int cursor = -1;

    public TdbDataList(String stockId) {
        this.stockId = stockId;
        this.dataList = new ArrayList<T>();
    }

    public void initCursor(int timestamp) {
        if (dataList.size() <= 1) {
            cursor = -1;
            return;
        }
        int i = 1;
        for (; i < dataList.size(); i++) {
            if (dataList.get(i).getTime() > timestamp) {
                break;
            }
        }
        cursor = i-1;
    }

    public void setData(T[] data) {
        this.dataList = Arrays.asList(data);
    }

    public void setData(List<T> data) {
        this.dataList = data;
    }

    public boolean isValid() {
        return cursor >= 0;
    }


    /**
     * Get data from current cursor to upto
     * @param upto timestamp
     */
    public List<T> getData(int upto) {
        if (cursor == dataList.size()) {
            return null;
        }

        int pos = cursor + 1;
        for (; pos < dataList.size(); pos++) {
            if (dataList.get(pos).getTime() > upto) {
                break;
            }
        }

        return dataList.subList(cursor+1, pos);
    }
}
