--创建存储过程，用于分页查询
--传入参数：pageNo 查询的页码，pageSize 每页的条数；输出参数：vrows 使用一个引用游标用于接收多条结果集。普通游标无法做到，只能使用引用游标
create or replace procedure pro_query_emp_limit(pageNo in number,pageSize in number,vrows out sys_refcursor) is
begin
  --存储过程中只进行打开游标，将 select 查询出的所有数据放置到 vrows 游标中，让调用着进行获取
open vrows for select t.empno,t.ename,t.job,t.mgr,t.hiredate,t.sal,t.comm,t.deptno from (select rownum r,t1.* from emp t1) t
     where t.r between ((pageNo-1) * pageSize+1) and pageNo * pageSize;
end;


--使用引用游标读取上面的存储过程返回的值
declare
     vrows sys_refcursor ;--声明引用游标
     vrow emp%rowtype; --定义变量接收遍历到的每一行数据
begin
     pro_query_emp_limit(2,6,vrows);--调用存储过程
     loop
       fetch vrows into vrow; -- fetch into 获取游标的值
       exit when vrows%notfound; -- 如果没有获取到值，则退出循环
       dbms_output.put_line('姓名：'|| vrow.ename || ' 薪水：'|| vrow.sal);
     end loop;
end;
