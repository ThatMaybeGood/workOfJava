package com.demo.integration.core.adapter.impl;

import com.demo.integration.core.adapter.AbstractPostAdapter;
import com.demo.integration.dto.spd.SpdRequestDTO;
import com.demo.integration.dto.spd.SpdResponseDTO;
import org.springframework.stereotype.Component;

/**
 * @author Mine
 * @version 1.0
 * 描述:
 * @date 2026/5/9 23:10
 */

@Component
public class SpdPostAdapter extends AbstractPostAdapter<SpdRequestDTO, SpdResponseDTO> {

    @Override
    public String bizCode() {
        return "spd";
    }

    @Override
    public SpdRequestDTO buildRequest(Object bizData) {

        SpdRequestDTO dto = new SpdRequestDTO();

        dto.setOrderNo("TEST001");
        dto.setPatientName("张三");

        return dto;
    }

    @Override
    protected Class<SpdResponseDTO> responseClass() {
        return SpdResponseDTO.class;
    }
}
