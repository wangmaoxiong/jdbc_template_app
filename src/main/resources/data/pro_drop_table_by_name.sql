
--表名作为参数，如果表已经存在，则删除它。
create or replace procedure pro_drop_table_by_name(tableName in user_tables.TABLE_NAME%type)
is
  flag number := 0; --表是否存在的表示，大于0表示表已经存在
begin
    --user_tables 是系统表，存储着当前用户下的所有表信息
    select count(1) into flag from user_tables where table_name = upper(tableName) ;-- user_tables 表中的表名区分大小写，而且是大写
    if flag > 0 then
        execute immediate 'drop table '|| tableName ;--如果表已经存在，则删除它
    end if;
end;


-- 调用存储过程：call pro_drop_table_by_name('student1');