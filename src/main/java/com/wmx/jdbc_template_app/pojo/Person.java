package com.wmx.jdbc_template_app.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangmaoxiong
 */
public class Person implements Serializable {
    private Integer pId;
    private String pName;
    private Float salary;
    private Date birthday;
    private String summary;

    public Integer getpId() {
        return pId;
    }

    public void setpId(Integer pId) {
        this.pId = pId;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public Float getSalary() {
        return salary;
    }

    public void setSalary(Float salary) {
        this.salary = salary;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "Person{" +
                "pId=" + pId +
                ", pName='" + pName + '\'' +
                ", salary=" + salary +
                ", birthday=" + birthday +
                ", summary='" + summary + '\'' +
                '}';
    }
}