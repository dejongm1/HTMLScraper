package com.mcd.spider.main.exception;

import com.mcd.spider.main.util.ExcelWriter;

public class ExcelOutputException extends SpiderException {


    private static final long serialVersionUID = 1L;

    private ExcelWriter excelWriter;
    private String methodName;

    public ExcelOutputException(ExcelWriter excelWriter, String methodName) {
        this.excelWriter = excelWriter;
        this.methodName = methodName;
    }

    public ExcelWriter getExcelWriter() {
        return excelWriter;
    }

    public String getMethodName() {
        return methodName;
    }
}
