package com.wmx.jdbc_template_app.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wmx.jdbc_template_app.pojo.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author wangmaoxiong
 */
@RestController
public class PersonController2 {
    Logger logger = Logger.getAnonymousLogger();
    /**
     * JdbcTemplate 用于简化 JDBC 操作，还能避免一些常见的错误，如忘记关闭数据库连接
     * Spring Boot 默认提供了数据源与 org.springframework.jdbc.core.JdbcTemplate
     * spring boot 默认对 JdbcTemplate 的配置，已经注入了数据源创建好了实例，程序员直接获取使用即可
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询表中总记录数：http://localhost:8080/person/findCount
     *
     * @return
     */
    @GetMapping("person/findCount")
    public String findCount() {
        String sql = "SELECT COUNT(1) FROM PERSON";
        //注意结果只能是一条/个，结果条数 ==0 或者 >1 都会抛异常，只能 ==1
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return sql + " ===> " + count;
    }

    /**
     * 根据 pId 查询姓名：http://localhost:8080/person/findNameById?pId=1
     *
     * @param pId
     * @return
     */
    @GetMapping("person/findNameById")
    public String findNameById(Integer pId) {
        pId = pId == null ? 1 : pId;
        String sql = "SELECT pName FROM PERSON WHERE pId = ?";
        Object[] param = new Object[]{pId};
        //一定要注意 queryForObject 结果必须是 1 条，多余 1 条或者没有1条，都会报错
        String name = null;
        try {
            name = jdbcTemplate.queryForObject(sql, param, String.class);
        } catch (Exception e) {
            logger.warning("查询的 pId 不存在：" + pId);
        }
        return sql + " ===> " + name;
    }

    /**
     * 根据 id 查询实体对象：http://localhost:8080/person/findById?pId=1
     *
     * @param pId
     * @return
     */
    @GetMapping("person/findById")
    public String findByPid(Integer pId) {
        String sql = "SELECT * FROM PERSON WHERE pId = ?";
        Object[] params = new Object[]{pId};

        Person person = new Person();
        try {
            //queryForObject 结果只能是 1 条，小于或者大于1条都会报错
            person = jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(Person.class));
        } catch (Exception e) {
            logger.info("pId " + pId + " 不存在.");
        }
        JsonObject jsonObject = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        jsonObject.addProperty("sql", sql);
        jsonObject.add("person", jsonParser.parse(new Gson().toJson(person)));
        return jsonObject.toString();
    }

    /**
     * 查询所有：http://localhost:8080/person/findAll
     *
     * @return
     */
    @GetMapping("person/findAll")
    public String findAll() {
        String sql = "SELECT * FROM PERSON";
        //如果没有数据，则 list 大小为 0，不会为 null 出现空指针异常
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql);
        String message = new Gson().toJson(mapList);
        return message;
    }

    /**
     * 模糊查询：http://localhost:8080/person/vagueFind?vagueValue=管理员
     *
     * @param vagueValue
     * @return
     */
    @GetMapping("person/vagueFind")
    public String vagueFind(String vagueValue) {
        String sql = "SELECT * FROM PERSON ";
        if (vagueValue != null && !"".equals(vagueValue)) {
            sql += " WHERE pName LIKE '%" + vagueValue + "%' ";
            sql += " OR summary LIKE '%" + vagueValue + "%' ";
        }
        //BeanPropertyRowMapper 要求 sql 查询出来的列和实体属性一一对应，否则应该在 sql 语句中用 as 设置别名
        List<Person> personList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Person.class));
        return personList.toString();
    }

    /**
     * 分页查询：
     * http://localhost:8080/person/pagingFind
     * http://localhost:8080/person/pagingFind?pageNo=1&rows=3
     *
     * @param pageNo :当前查询的页码，从1开始
     * @param rows   ：每页显示的记录条数
     * @return
     */
    @GetMapping("person/pagingFind")
    public String pagingFind(Integer pageNo, Integer rows) {
        //mysql 的 limit 分页，第一个参数为起始索引，从0开始，第二个参数为查询的条数
        String sql = "SELECT * FROM PERSON LIMIT ?,?";
        pageNo = pageNo == null ? 1 : pageNo;
        rows = rows == null ? 2 : rows;
        Integer startIndex = (pageNo - 1) * rows;

        List<Person> personList = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Person.class), startIndex, rows);
        return new Gson().toJson(personList);
    }

}