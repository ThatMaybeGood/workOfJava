package com.showexcelsoap.model;

import jakarta.xml.bind.annotation.*;

import java.time.LocalDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "user", propOrder = {
        "id",
        "name",
        "email",
        "phone",
        "createTime"
})
public class User {

    @XmlElement(required = true)
    protected String id;

    @XmlElement(required = true)
    protected String name;

    @XmlElement(required = true)
    protected String email;

    protected String phone;

    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected LocalDateTime createTime;

    // 构造方法
    public User() {}

    public User(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createTime = LocalDateTime.now();
    }

    // Getter 和 Setter 方法
    public String getId() { return id; }
    public void setId(String value) { this.id = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public String getEmail() { return email; }
    public void setEmail(String value) { this.email = value; }

    public String getPhone() { return phone; }
    public void setPhone(String value) { this.phone = value; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { this.createTime = value; }
}