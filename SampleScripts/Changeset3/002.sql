insert into some_html (id, html) values ('1', '<html><body><span style="color: red; background-color: black;">Damn semicolons</span></body></html>');
insert into some_html (id, html) values ('2', '<html><body><span style="color: red; background-color: black;">Damn semicolons</span></body></html>');

--down
delete from some_html where id = '2';
delete from some_html where id = '1';