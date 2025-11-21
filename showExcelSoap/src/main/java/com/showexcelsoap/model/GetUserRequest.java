package com.showexcelsoap.model;


import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"userId"})
@XmlRootElement(name = "GetUserRequest", namespace = "http://example.com/webservice")
public class GetUserRequest {

    @XmlElement(required = true , namespace = "http://example.com/webservice")
    protected String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String value) {
        this.userId = value;
    }
}
