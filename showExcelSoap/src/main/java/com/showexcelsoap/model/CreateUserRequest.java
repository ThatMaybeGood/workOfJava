package com.showexcelsoap.model;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"name", "email", "phone"})
@XmlRootElement(name = "CreateUserRequest" , namespace = "http://example.com/webservice")
public class CreateUserRequest {

    @XmlElement(required = true , namespace = "http://example.com/webservice")
    protected String name;

    @XmlElement(required = true , namespace = "http://example.com/webservice")
    protected String email;

    protected String phone;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String value) {
        this.phone = value;
    }
}
