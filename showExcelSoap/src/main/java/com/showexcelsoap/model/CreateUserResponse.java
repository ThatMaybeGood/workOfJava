package com.showexcelsoap.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"user", "message"})
@XmlRootElement(name = "CreateUserResponse" , namespace = "http://example.com/webservice")
public class CreateUserResponse {

    @XmlElement(required = true ,namespace = "http://example.com/webservice")
    protected User user;

    @XmlElement(required = true, namespace = "http://example.com/webservice")
    protected String message;

    public User getUser() {
        return user;
    }

    public void setUser(User value) {
        this.user = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        this.message = value;
    }
}
