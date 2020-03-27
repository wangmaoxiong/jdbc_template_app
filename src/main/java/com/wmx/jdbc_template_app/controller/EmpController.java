package com.wmx.jdbc_template_app.controller;

import com.google.gson.Gson;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author wangmaoxiong
 * Oracle 数据库操作
 */
@RestController
public class EmpController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 分页查询：
     * http://localhost:8080/emp/pagingFind
     * http://localhost:8080/emp/pagingFind?pageNo=1&rows=3
     *
     * @param pageNo :当前查询的页码，从1开始
     * @param rows   ：每页显示的记录条数
     * @return
     */
    @GetMapping("emp/pagingFind")
    public String pagingFind(Integer pageNo, Integer rows) {
        pageNo = pageNo == null ? 1 : pageNo;
        rows = rows == null ? 2 : rows;
        //Oracle 的分页需要借助伪列(rownum)，第一个参数为起始索引，从1开始，第二个参数为查询的条数
        String sql = "select t2.* from( select rownum r,t.* from emp t) t2 where t2.r between " + pageNo + " and " + rows;
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql);
        return new Gson().toJson(mapList);
    }

}
