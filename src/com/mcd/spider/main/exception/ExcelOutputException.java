package com.mcd.spider.main.exception;

import com.mcd.spider.main.util.OutputUtil;

public class ExcelOutputException extends SpiderException {


    private static final long serialVersionUID = 1L;

    private OutputUtil outputUtil;
    private String methodName;

    public ExcelOutputException(OutputUtil outputUtil, String methodName) {
        this.outputUtil = outputUtil;
        this.methodName = methodName;
    }

    public OutputUtil getOutputUtil() {
        return outputUtil;
    }

    public String getMethodName() {
        return methodName;
    }
}
