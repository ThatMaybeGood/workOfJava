package com.example.messagedataservice.dto;


public class ApiResponse {
    private ApiResponseHead head;
    private ApiResponseBody body;



    public ApiResponseHead getHead() {
        return head;
    }

    public void setHead(ApiResponseHead head) {
        this.head = head;
    }

    public ApiResponseBody getBody() {
        return body;
    }

    public void setBody(ApiResponseBody body) {
        this.body = body;
    }
}
