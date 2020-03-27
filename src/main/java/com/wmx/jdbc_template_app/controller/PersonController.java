package com.wmx.jdbc_template_app.controller;

import com.wmx.jdbc_template_app.pojo.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @author wangmaoxoing
 */
@RestController
public class PersonController {
    Logger logger = Logger.getAnonymousLogger();
    /**
     * JdbcTemplate 用于简化 JDBC 操作，还能避免一些常见的错误，如忘记关闭数据库连接
     * Spring Boot 默认提供了数据源与 org.springframework.jdbc.core.JdbcTemplate
     * spring boot 默认对 JdbcTemplate 的配置，已经注入了数据源创建好了实例，程序员直接获取使用即可
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 创建 person 表：http://localhost:8080/person/createTable
     *
     * @return
     */
    @GetMapping("person/createTable")
    public String createTable() {
        String sql = "create table if not EXISTS person(" +
                "pId int primary key auto_increment," +
                "pName varchar(18) not null," +
                "birthday date not null," +
                "salary float(10,2)," +
                "summary varchar(256)" +
                ")";
        //execute 可以执行任意 sql(不宜做查询)
        jdbcTemplate.execute(sql);
        return sql;
    }


    /**
     * 删除 person 表：http://localhost:8080/person/dropTable
     *
     * @return
     */
    @GetMapping("person/dropTable")
    public String dropTable() {
        String sql = "DROP TABLE if EXISTS PERSON";
        jdbcTemplate.execute(sql);
        return sql;
    }

    /**
     * 保存用户
     * 为了方便使用的是 get 请求：
     * http://localhost:8080/person/save?pName=admin&summary=重要人物&salary=9999.00
     *
     * @param person
     * @return
     */
    @GetMapping("person/save")
    public String savePerson(Person person) {
        String message = "保存用户：" + person;
        logger.info(message);
        person.setpName(person.getpName() == null ? "scott" : person.getpName());
        person.setSummary(person.getSummary() == null ? "" : person.getSummary().trim());

        String sql = "INSERT INTO PERSON(pName,birthday,salary,summary) VALUES (?,?,?,?)";
        Object[] params = new Object[4];
        params[0] = person.getpName();
        params[1] = new Date();
        params[2] = person.getSalary();
        params[3] = person.getSummary();
        //update 方法用于执行新增、修改、删除等语句
        jdbcTemplate.update(sql, params);
        return sql;
    }

    /**
     * 修改用户描述
     * 为了方便使用的是 get 请求：http://localhost:8080/person/update?summary=大咖&pId=1
     *
     * @param person
     * @return
     */
    @GetMapping("person/update")
    public String updatePerson(Person person) {
        String message = "修改用户描述：" + person;
        logger.info(message);
        person.setSummary(person.getSummary() == null ? "" : person.getSummary().trim());
        person.setpId(person.getpId() == null ? 0 : person.getpId());
        StringBuffer sqlBuff = new StringBuffer("UPDATE PERSON SET ");
        //sql 中的字符串必须加单引号
        sqlBuff.append(" SUMMARY='" + person.getSummary() + "' ");
        sqlBuff.append(" WHERE pId=" + person.getpId());
        logger.info("SQL 确认：" + sqlBuff.toString());
        //update 方法用于执行新增、修改、删除等语句
        jdbcTemplate.update(sqlBuff.toString());
        return sqlBuff.toString();
    }

    /**
     * 根据 id 删除一个或者多条数据，多个 id 时用 "," 隔开
     * http://localhost:8080/person/delete?ids=2,3,4
     *
     * @param ids
     * @return
     */
    @GetMapping("person/delete")
    public String deletePerson(String ids) {
        String message = "删除用户：" + ids;
        logger.info(message);
        if (ids == null || "".equals(ids)) {
            return message;
        }
        String[] id_arr = ids.split(",");
        String[] sql_arr = new String[id_arr.length];
        for (int i = 0; i < id_arr.length; i++) {
            sql_arr[i] = "DELETE FROM PERSON WHERE pId = " + id_arr[i];
        }
        logger.info("SQL 确认：" + Arrays.asList(sql_arr));
        //batchUpdate 方法用于执行批处理增加删除、修改等 sql
        jdbcTemplate.batchUpdate(sql_arr);
        return Arrays.asList(sql_arr).toString();
    }

    /**
     * 根据单个 id 删除单条数据：http://localhost:8080/person/deleteById?pId=4
     *
     * @param pId
     * @return
     */
    @GetMapping("person/deleteById")
    public String deletePersonById(Integer pId) {
        String message = "根据 pId 删除：" + pId;
        logger.info(message);
        if (pId == null) {
            return message;
        }
        String sql = "DELETE FROM PERSON WHERE pId = " + pId;
        //execute 同样可以执行任意 DDL 语句
        jdbcTemplate.execute(sql);
        return sql;
    }

    /**
     * 删除整表数据：http://localhost:8080/person/deletesAll
     *
     * @return
     */
    @GetMapping("person/deletesAll")
    public String deleteAll() {
        String sql = "TRUNCATE TABLE PERSON";
        jdbcTemplate.execute(sql);
        return sql;
    }

}