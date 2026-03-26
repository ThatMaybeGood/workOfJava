package com.mergedata.model.dto.Message;

import lombok.Data;
@Data
public class MsgRequest {
    private MsgRequestHead head;
    private MsgRequestBody body;
}
