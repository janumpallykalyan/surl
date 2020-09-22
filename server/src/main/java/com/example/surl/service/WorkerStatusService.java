package com.example.surl.service;

import com.example.surl.exception.KeyOverFlowException;

public interface WorkerStatusService {

    Long getNewKey(String workerId) throws KeyOverFlowException;
}
