
--ChangeSet1/0001.CreateTables.sql
Create table persons (id varchar(50))
insert into porp_schema_log (id, changeset, script_name, md5, date_applied, up_script, down_script) values ('8e23a07e-5183-41ff-9401-8cd5640e91a6', 'ChangeSet1', '0001.CreateTables.sql', 'f9e58ff8d0f35e9326b71ac4dac6381d', '2012-12-10', 'Create table persons (id varchar(50));', 'drop table persons;');

--ChangeSet1/0002.InsertData.sql
insert into persons (id) values ('1234')

insert into persons (id) values ('1235')

insert into persons (id) values ('1236')

insert into persons (id) values ('1237')

insert into persons (id) values ('1238')
insert into porp_schema_log (id, changeset, script_name, md5, date_applied, up_script, down_script) values ('fd50d271-a3e2-4226-8034-d9cbf64e568e', 'ChangeSet1', '0002.InsertData.sql', '57ef02c580251fddc0c94e546e12a3e8', '2012-12-10', 'insert into persons (id) values ('1234');
insert into persons (id) values ('1235');
insert into persons (id) values ('1236');
insert into persons (id) values ('1237');
insert into persons (id) values ('1238');', 'delete from persons where id = '1234';
delete from persons where id = '1235';
delete from persons where id = '1236';
delete from persons where id = '1237';
delete from persons where id = '1238';');

--ChangeSet1/0003.MoreData.sql
insert into persons (id) values ('4234')

insert into persons (id) values ('4235')

insert into persons (id) values ('4236')

insert into persons (id) values ('4237')

insert into persons (id) values ('4238')
insert into porp_schema_log (id, changeset, script_name, md5, date_applied, up_script, down_script) values ('28331a49-c3b3-416e-ab8a-e5b8bc996dff', 'ChangeSet1', '0003.MoreData.sql', '9b2ad104afaef7afcb4b94d99faf3d8d', '2012-12-10', 'insert into persons (id) values ('4234');
insert into persons (id) values ('4235');
insert into persons (id) values ('4236');
insert into persons (id) values ('4237');
insert into persons (id) values ('4238');', 'delete from persons where id = '4234';
delete from persons where id = '4235';
delete from persons where id = '4236';
delete from persons where id = '4237';
delete from persons where id = '4238';');
