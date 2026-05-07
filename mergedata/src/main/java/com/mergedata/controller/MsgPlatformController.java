package com.mergedata.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mergedata.model.dto.Message.MsgRequest;
import com.mergedata.model.dto.Message.MsgRequestBody;
import com.mergedata.model.dto.Message.MsgRequestData;
import com.mergedata.model.dto.Message.MsgResponse;
import com.mergedata.model.dto.SpdPrescExpressRequest;
import com.mergedata.model.dto.SpdPrescExpressResponse;
import com.mergedata.server.SpdApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/message/rcv")
@RequiredArgsConstructor
public class MsgPlatformController {

    private final ObjectMapper objectMapper;
    private final SpdApiService spdApiService;  // 注入SPD服务

    @PostMapping("/prioPersonMessage")
    public MsgResponse<?> prioPersonMessage(@RequestBody MsgRequest request,
                                            HttpServletRequest httpRequest) {

        try {
            log.info(objectMapper.writeValueAsString(request));

            if (request != null && request.getBody() != null) {
                MsgRequestBody body = request.getBody();

                // 解析 data 字符串
                MsgRequestData data = body.getDataObject();
                if (data == null) {
                    return MsgResponse.failure("data 解析失败或为空");
                }
            } else {
                return MsgResponse.failure("请求体为空");
            }

        } catch (Exception e) {
            log.error("接收消息失败: {}", e.getMessage(), e);
            return MsgResponse.failure("接收消息失败: " + e.getMessage());
        }
        log.info("========== 接收消息完成，返回成功响应 ==========");
        return MsgResponse.success("操作成功");
    }

    @PostMapping("/SPDHospitalRxLogistics")
    public MsgResponse<?> SPDHospitalRxLogistics(
            @RequestBody(required = false) String rawRequestBody,
            HttpServletRequest httpRequest) {

        log.info("========== SPD接口收到请求 ==========");

        try {
            if (rawRequestBody == null || rawRequestBody.isEmpty()) {
                log.warn("请求体为空");
                return MsgResponse.failure("接收消息失败: " + "请求体为空");
            }

            processMessage(rawRequestBody);

        } catch (Exception e) {
            log.error("处理请求失败", e);
            return MsgResponse.failure("接收消息失败: " + e.getMessage());
        }
        log.info("========== 接收消息完成，返回成功响应 ==========");
        return MsgResponse.success("操作成功");
    }

    /**
     * 处理消息
     */
    private void processMessage(String rawRequestBody) throws Exception {
        JsonNode rootNode = objectMapper.readTree(rawRequestBody);
        JsonNode bodyNode = rootNode.get("body");

        if (bodyNode == null) {
            log.warn("请求体中缺少body节点");
            return;
        }

        // 获取消息类型
        String msgCode = getJsonValue(bodyNode, "msg_code");
        String msgName = getJsonValue(bodyNode, "msg_name");

        // 解析data
        String dataStr = getJsonValue(bodyNode, "data");
        if (dataStr.isEmpty()) {
            log.warn("data字段为空");
            return;
        }

        JsonNode dataNode = objectMapper.readTree(dataStr);

        // 提取必要字段
        String drugStoreId = getJsonValue(dataNode, "drug_store_id");
        String registerFlowNo = getJsonValue(dataNode, "register_flow_no");
        String bizRecipeNo = getJsonValue(dataNode, "biz_recipe_no");

        // 打印日志
        log.info("消息类型码: {} 消息名称: {}   biz_recipe_no: {}", msgCode, msgName, bizRecipeNo);

        // 解析并调用SPD接口
        if (!bizRecipeNo.isEmpty()) {
            callSpdApi(drugStoreId, registerFlowNo, bizRecipeNo);
        }
    }

    /**
     * 调用SPD接口
     */
    private void callSpdApi(String drugStoreId, String registerFlowNo, String bizRecipeNo) {
        String[] parts = bizRecipeNo.split("\\|");

        if (parts.length < 4) {
            log.warn("biz_recipe_no格式不正确，期望4个字段，实际: {}", parts.length);
            return;
        }

        // 构建请求
        SpdPrescExpressRequest request = new SpdPrescExpressRequest();
        request.setStoreCode(drugStoreId);      // StoreCode = drug_store_id
        request.setPrescDate(registerFlowNo);   // PrescDate = register_flow_no
        request.setPrescNo(parts[2]);           // PrescNo = 第三部分
        request.setIsExpress(parts[3]);         // IsExpress = 第四部分

        log.info("解析结果: StoreCode={}, PrescDate={}, PrescNo={}, IsExpress={}",
                request.getStoreCode(),
                request.getPrescDate(),
                request.getPrescNo(),
                "1".equals(request.getIsExpress()) ? "快递" : "自取");

        // 调用SPD接口
        SpdPrescExpressResponse response = spdApiService.updatePrescExpressStatus(request);

    }

    /**
     * 安全获取JSON值
     */
    private String getJsonValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName)) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull()) {
                return fieldNode.asText();
            }
        }
        return "";
    }


}