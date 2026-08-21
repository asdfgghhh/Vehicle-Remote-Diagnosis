package com.vrd.diagnosis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vrd.diagnosis.dto.UdsRequest;
import com.vrd.diagnosis.dto.UdsResponse;
import com.vrd.diagnosis.entity.UdsDiagnosisSession;

public interface UdsDiagnosisService
extends IService<UdsDiagnosisSession> {
    public UdsResponse diagnosticSessionControl(UdsRequest var1);

    public UdsResponse ecuReset(UdsRequest var1);

    public UdsResponse securityAccessRequestSeed(UdsRequest var1);

    public UdsResponse securityAccessSendKey(UdsRequest var1);

    public UdsResponse readDataByIdentifier(UdsRequest var1);

    public UdsResponse writeDataByIdentifier(UdsRequest var1);

    public UdsResponse readDtcInformation(UdsRequest var1);

    public UdsResponse clearDiagnosticInformation(UdsRequest var1);

    public UdsResponse routineControl(UdsRequest var1);

    public UdsResponse readMemoryByAddress(UdsRequest var1);

    public UdsResponse writeMemoryByAddress(UdsRequest var1);

    public UdsResponse inputOutputControl(UdsRequest var1);

    public UdsResponse testerPresent(UdsRequest var1);

    public UdsResponse executeRequest(UdsRequest var1);

    public Object querySessions(String var1, Integer var2, Integer var3);

    public void completeResponse(UdsResponse var1);
}

