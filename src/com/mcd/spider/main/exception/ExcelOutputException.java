package com.mcd.spider.main.exception;

import com.mcd.spider.main.util.RecordOutputUtil;

public class ExcelOutputException extends SpiderException {


    private static final long serialVersionUID = 1L;

    private RecordOutputUtil recordOutputUtil;
    private String methodName;

    public ExcelOutputException(RecordOutputUtil recordOutputUtil, String methodName) {
        this.recordOutputUtil = recordOutputUtil;
        this.methodName = methodName;
    }

    public RecordOutputUtil getOutputUtil() {
        return recordOutputUtil;
    }

    public String getMethodName() {
        return methodName;
    }
}
