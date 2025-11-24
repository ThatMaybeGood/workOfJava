package com.showexcelsoap.endpoint;

import com.showexcelsoap.model.*;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.UUID;

@Endpoint
public class UserServiceEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/webservice";

    /**
     * 根据用户ID获取用户信息
     *
     * @param request GetUserRequest请求对象，包含用户ID
     * @return GetUserResponse响应对象，包含查询到的用户信息
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetUserRequest")
    @ResponsePayload
    public GetUserResponse getUser(@RequestPayload GetUserRequest request) {
        System.out.println("GetUserRequest received: {}" + request.getUserId());


        GetUserResponse response = new GetUserResponse();

        // 模拟从数据库获取用户
        User user = new User();
        user.setId(request.getUserId());
        user.setName("张三");
        user.setEmail("zhangsan@example.com");
        user.setPhone("13800138000");

        response.setUser(user);
        return response;
    }

    /**
     * 创建新用户
     *
     * @param request CreateUserRequest请求对象，包含用户基本信息
     * @return CreateUserResponse响应对象，包含创建的用户信息和操作结果消息
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateUserRequest")
    @ResponsePayload
    public CreateUserResponse createUser(@RequestPayload CreateUserRequest request) {
        CreateUserResponse response = new CreateUserResponse();

        // 创建新用户
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        response.setUser(user);
        response.setMessage("用户创建成功");

        return response;
    }
}