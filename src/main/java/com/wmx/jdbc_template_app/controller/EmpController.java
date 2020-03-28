package com.wmx.jdbc_template_app.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangmaoxiong
 * Oracle 数据库操作，调用 存储过程
 */
@RestController
public class EmpController {

    private Logger logger = LoggerFactory.getLogger(EmpController.class);

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

    /**
     * 存储过程删除表
     * http://localhost:8080/emp/dropTable?tableName=emp
     * http://localhost:8080/emp/dropTable?tableName=dept
     * emp 员工表引用了 dept 部门表，如果先删除 dept 表，其中有数据被 emp 表引用，则删除时报错：
     * oracle.jdbc.OracleDatabaseException: ORA-02449: 表中的唯一/主键被外键引用
     *
     * @param tableName
     * @return
     */
    @GetMapping("emp/dropTable")
    public String dropTableByName(@RequestParam String tableName) {
        JsonObject jsonObject = new JsonObject();
        try {
            //sql 和在数据库中完全一样，外围可以花括号括起来
            String sql = "{call pro_drop_table_by_name('" + tableName + "')}";
            jdbcTemplate.execute(sql);

            jsonObject.addProperty("code", 200);
            jsonObject.addProperty("message", sql);
        } catch (DataAccessException e) {
            logger.error(e.getMessage(), e);
            jsonObject.addProperty("code", 500);
            jsonObject.addProperty("message", e.getMessage());
        }
        return jsonObject.toString();
    }

    /**
     * 存储过程检查某个表在数据库中是否已经存在，存在时返回1，否则返回0
     * http://localhost:8080/emp/checkTableByName?tableName=emp
     *
     * @param tableName
     * @return
     */
    @GetMapping("emp/checkTableByName")
    @SuppressWarnings("all")
    public Integer checkTableByName(@RequestParam String tableName) {
        Integer execute = (Integer) jdbcTemplate.execute(new CallableStatementCreator() {

            //创建可回调语句，方法里面就是纯 jdbc 创建调用存储的写法
            @Override
            public CallableStatement createCallableStatement(Connection connection) throws SQLException {
                //存储过程调用 sql，通过 java.sql.Connection.prepareCall 获取回调语句,sql 外围可以花括号括起来
                String sql = "{call pro_check_table_by_name(?,?)}";
                CallableStatement callableStatement = connection.prepareCall(sql);
                //设置第一个占位符参数值，传入参数。参数索引从1开始
                callableStatement.setString(1, tableName);
                //注册第二个参数（返回值）的数据类型，oracle.jdbc.OracleTypes 中定义了全部的数据类型常量
                callableStatement.registerOutParameter(2, OracleTypes.INTEGER);
                return callableStatement;
            }
        }, new CallableStatementCallback<Object>() {
            //正式调用存储过程以及处理返回的值.
            @Override
            public Object doInCallableStatement(CallableStatement callableStatement) throws SQLException {
                //执行调用存储过程
                callableStatement.execute();
                //参数索引从1开始，获取村存储过程的返回值.
                return callableStatement.getInt(2);
            }
        });
        return execute;
    }


    /**
     * 存储过程实现分页查询，传入页码和条数即可进行分页返回
     * http://localhost:8080/emp/pageQuery?pageNo=2&pageSize=5
     *
     * @param pageNo   页码
     * @param pageSize 每页显示的条数
     * @return
     */
    @GetMapping("emp/pageQuery")
    @SuppressWarnings("all")
    public List pageQuery(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        List execute = (List) jdbcTemplate.execute(new CallableStatementCreator() {

            //创建可回调语句，方法里面就是纯 jdbc 创建调用存储的写法
            @Override
            public CallableStatement createCallableStatement(Connection connection) throws SQLException {
                //存储过程调用 sql，通过 java.sql.Connection.prepareCall 获取回调语句,sql 外围可以花括号括起来
                String sql = "{call pro_query_emp_limit(?,?,?)}";
                CallableStatement callableStatement = connection.prepareCall(sql);
                //设置第占位符参数值
                callableStatement.setInt(1, pageNo);
                callableStatement.setInt(2, pageSize);
                //输出参数类型设置为引用游标
                callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
                return callableStatement;
            }
        }, new CallableStatementCallback<Object>() {
            //正式调用存储过程以及处理返回的值.
            @Override
            public Object doInCallableStatement(CallableStatement callableStatement) throws SQLException {
                //存储返回结果
                List<Map<String, Object>> resultMapList = new ArrayList<>(8);
                //遍历时临时对象
                Map<String, Object> temp;
                //执行调用存储过程，将结果转为 java.sql.ResultSet 结果集
                callableStatement.execute();
                ResultSet resultSet = (ResultSet) callableStatement.getObject(3);

                //遍历结果集
                while (resultSet.next()) {
                    temp = new HashMap<>(8);
                    //根据字段名称取值
                    temp.put("empno", resultSet.getInt("empno"));
                    temp.put("ename", resultSet.getString("ename"));
                    temp.put("job", resultSet.getString("job"));
                    temp.put("mgr", resultSet.getInt("mgr"));
                    temp.put("hiredate", resultSet.getDate("hiredate"));
                    temp.put("sal", resultSet.getFloat("sal"));
                    resultMapList.add(temp);
                }
                return resultMapList;
            }
        });
        return execute;
    }

    /**
     * 存储过程检查某个表在数据库中是否已经存在，存在时返回1，否则返回0，使用 call 方法进行调用
     * http://localhost:8080/emp/callCheckTableByName?tableName=emp
     *
     * @param tableName
     * @return
     */
    @GetMapping("emp/callCheckTableByName")
    @SuppressWarnings("all")
    public Map<String, Object> callCheckTableByName(@RequestParam String tableName) {

        //SqlParameter 表示存储过程的传入参数，可以不指定参数名称，但是必须指定参数类型
        //SqlOutParameter 表示存储过程的输出参数，必须指定名称和类型，名称自定义即可，会被作为返回值存放在 map 中
        List<SqlParameter> sqlParameterList = new ArrayList<>(4);
        sqlParameterList.add(new SqlParameter(OracleTypes.VARCHAR));
        sqlParameterList.add(new SqlOutParameter(tableName, OracleTypes.NUMBER));

        //call 方法在 execute 的基础上对返回结果进行进一步的封装，只需要创建 CallableStatement
        //List<SqlParameter> 中的每一个 SqlParameter 按顺序对应占位符参数
        //返回的 map 包含返回参数
        Map<String, Object> call = jdbcTemplate.call(new CallableStatementCreator() {
            @Override
            public CallableStatement createCallableStatement(Connection connection) throws SQLException {
                //存储过程调用 sql，通过 java.sql.Connection.prepareCall 获取回调语句,sql 外围可以花括号括起来
                String sql = "{call pro_check_table_by_name(?,?)}";
                CallableStatement callableStatement = connection.prepareCall(sql);
                //设置第一个占位符参数值，传入参数。参数索引从1开始
                callableStatement.setString(1, tableName);
                //注册第二个参数（返回值）的数据类型，oracle.jdbc.OracleTypes 中定义了全部的数据类型常量
                callableStatement.registerOutParameter(2, OracleTypes.INTEGER);
                return callableStatement;
            }
        }, sqlParameterList);
        return call;
    }

    /**
     * 使用 call 方法调用存储过程进行分页查询，推荐方式
     * http://localhost:8080/emp/callPageQuery?pageNo=2&pageSize=5
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("emp/callPageQuery")
    @SuppressWarnings("all")
    public List<Map<String, Object>> callPageQuery(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        //设置存储过程参数
        //SqlParameter 表示存储过程的传入参数，可以不知道参数名称，但是必须指定参数类型
        //SqlOutParameter 表示存储过程的输出参数，必须指定名称和类型，名称自定义即可，会被作为返回值存放在 map 中
        List<SqlParameter> sqlParameterList = new ArrayList<>(4);
        sqlParameterList.add(new SqlParameter(OracleTypes.NUMBER));
        sqlParameterList.add(new SqlParameter(OracleTypes.NUMBER));
        sqlParameterList.add(new SqlOutParameter("resultSet", OracleTypes.CURSOR));

        //使用了 call 之后对于返回的游标就方便多了，不再需要自己一个一个取值了，它会自动进行转换
        //call 的 key 会是 resultSet，然后它的值会是一个 List<Map>，自动转换好了
        Map<String, Object> call = jdbcTemplate.call(new CallableStatementCreator() {
            //创建可回调语句，方法里面就是纯 jdbc 创建调用存储的写法
            @Override
            public CallableStatement createCallableStatement(Connection connection) throws SQLException {
                //存储过程调用 sql，通过 java.sql.Connection.prepareCall 获取回调语句,sql 外围可以花括号括起来
                String sql = "{call pro_query_emp_limit(?,?,?)}";
                CallableStatement callableStatement = connection.prepareCall(sql);
                //设置第占位符参数值
                callableStatement.setInt(1, pageNo);
                callableStatement.setInt(2, pageSize);
                //输出参数类型设置为引用游标
                callableStatement.registerOutParameter(3, OracleTypes.CURSOR);
                return callableStatement;
            }
        }, sqlParameterList);

        //没有值时就是空 list，不会控制在异常
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) call.get("resultSet");
        return dataList;
    }

}
