package com.github.freeacs.tr069.methods.request;

import com.github.freeacs.tr069.SessionData;
import com.github.freeacs.tr069.xml.ParameterValueStruct;

import java.util.List;

public interface DataReportService {
    void report(SessionData sessionData, List<ParameterValueStruct> parameterValues);
}
