package com.showexcelsoap.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "user" })
@XmlRootElement(name = "GetUserResponse", namespace = "http://example.com/webservice")
public class GetUserResponse {

    @XmlElement(required = true , namespace = "http://example.com/webservice")
    protected User user;

    public User getUser() { return user; }
    public void setUser(User value) { this.user = value; }
}