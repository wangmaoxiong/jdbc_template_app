
--表名作为参数，同时指定返回参数，如果表名存在，则返回 1，不存在返回 0
create or replace procedure pro_check_table_by_name(tableName in user_tables.TABLE_NAME%type, ifExists out number) is
begin
    --user_tables 是系统定义的视图，可以查看当前用户下的所有表信息，表中的表名区分大小写，而且是大写
    select count(1) into ifExists from user_tables where table_name = upper(tableName) ;
end;


-- 数据库中调用存储过程：
declare
   tableName varchar2(30) := 'demp';
   ifExists number;
begin
  pro_check_table_by_name(tableName,ifExists);
    dbms_output.put_line(ifExists);
end;
