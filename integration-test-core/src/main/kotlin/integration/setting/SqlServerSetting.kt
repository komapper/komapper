package integration.setting

interface SqlServerSetting<CONFIG> : Setting<CONFIG> {
    override val dbms: Dbms get() = Dbms.SQLSERVER
    override val createSql: String
        get() = """
        create sequence sequence_strategy_id start with 1 increment by 100;
        create sequence my_sequence_strategy_id start with 1 increment by 100;
        create sequence person_id_sequence start with 1 increment by 100;

        create table department_archive(department_id int not null primary key, department_no int not null unique,department_name varchar(20),location varchar(20) default 'tokyo', version int);
        create table department(department_id int not null primary key, department_no int not null unique,department_name varchar(20),location varchar(20) default 'tokyo', version int);
        create table address(address_id int not null primary key, street varchar(20) unique, version int);
        create table address_archive(address_id int not null primary key, street varchar(20) unique, version int);
        create table employee(employee_id int not null primary key, employee_no int not null ,employee_name varchar(20),manager_id int,hiredate date,salary numeric(7,2),department_id int,address_id int, version int, constraint fk_department_id foreign key(department_id) references department(department_id),constraint fk_address_id foreign key(address_id) references address(address_id));
        create table person(person_id int not null primary key, name varchar(20), created_at datetime2, updated_at datetime2, version int);
        create table [order]([order_id] int not null primary key, [value] varchar(20));

        create table comp_key_department(department_id1 int not null, department_id2 int not null, department_no int not null unique,department_name varchar(20),location varchar(20) default 'tokyo', version int, constraint pk_comp_key_department primary key(department_id1, department_id2));
        create table comp_key_address(address_id1 int not null, address_id2 int not null, street varchar(20), version int, constraint pk_comp_key_address primary key(address_id1, address_id2));
        create table comp_key_employee(employee_id1 int not null, employee_id2 int not null, employee_no int not null ,employee_name varchar(20),manager_id1 int,manager_id2 int,hiredate date,salary numeric(7,2),department_id1 int,department_id2 int,address_id1 int,address_id2 int,version int, constraint pk_comp_key_employee primary key(employee_id1, employee_id2), constraint fk_comp_key_department_id foreign key(department_id1, department_id2) references comp_key_department(department_id1, department_id2), constraint fk_comp_key_address_id foreign key(address_id1, address_id2) references comp_key_address(address_id1, address_id2));

        create table large_object(id numeric(8) not null primary key, name varchar(20), large_name text, bytes varbinary(2000), large_bytes varbinary(max), dto varbinary(2000), large_dto varbinary(max));
        create table tense (id int primary key,date_date date, date_time time, date_timestamp datetime2, cal_date date, cal_time time, cal_timestamp datetime2, sql_date date, sql_time time, sql_timestamp datetime2);
        create table job (id int not null primary key, job_type varchar(20));
        create table authority (id int not null primary key, authority_type int);
        create table no_id (value1 int, value2 int);
        create table owner_of_no_id (id int not null primary key, no_id_value1 int);
        create table constraint_checking (primary_key int primary key, unique_key int unique, foreign_key int, check_constraint int, not_null int not null, constraint ck_constraint_checking_1 check (check_constraint > 0), constraint fk_job_id foreign key (foreign_key) references job (id));
        create table pattern (value varchar(10));

        create table id_generator(pk varchar(20) not null primary key, value int not null);
        create table my_id_generator(my_pk varchar(20) not null primary key, my_value int not null);
        create table auto_strategy(id int identity primary key, value varchar(10));
        create table identity_strategy(id int identity primary key, value varchar(10));
        create table sequence_strategy(id int not null primary key, value varchar(10));
        create table sequence_strategy2(id int not null primary key, value varchar(10));
        create table table_strategy(id int not null primary key, value varchar(10));
        create table table_strategy2(id int not null primary key, value varchar(10));

        create table big_decimal_test(id int not null primary key, value decimal);
        create table big_integer_test(id int not null primary key, value decimal);
        create table blob_test(id int not null primary key, value varbinary(max));
        create table boolean_test(id int not null primary key, value bit);
        create table byte_test(id int not null primary key, value tinyint);
        create table byte_array_test(id int not null primary key, value varbinary(100));
        create table clob_test(id int not null primary key, value text);
        create table double_test(id int not null primary key, value real);
        create table enum_test(id int not null primary key, value varchar(20));
        create table float_test(id int not null primary key, value float);
        create table int_test(id int not null primary key, value int);
        create table local_date_time_test(id int not null primary key, value datetime2);
        create table local_date_test(id int not null primary key, value date);
        create table local_time_test(id int not null primary key, value time);
        create table long_test(id int not null primary key, value bigint);
        create table offset_date_time_test(id int not null primary key, value datetime2);
        create table short_test(id int not null primary key, value smallint);
        create table string_test(id int not null primary key, value varchar(20));

        insert into department values(1,10,'ACCOUNTING','NEW YORK',1);
        insert into department values(2,20,'RESEARCH','DALLAS',1);
        insert into department values(3,30,'SALES','CHICAGO',1);
        insert into department values(4,40,'OPERATIONS','BOSTON',1);
        insert into address values(1,'STREET 1',1);
        insert into address values(2,'STREET 2',1);
        insert into address values(3,'STREET 3',1);
        insert into address values(4,'STREET 4',1);
        insert into address values(5,'STREET 5',1);
        insert into address values(6,'STREET 6',1);
        insert into address values(7,'STREET 7',1);
        insert into address values(8,'STREET 8',1);
        insert into address values(9,'STREET 9',1);
        insert into address values(10,'STREET 10',1);
        insert into address values(11,'STREET 11',1);
        insert into address values(12,'STREET 12',1);
        insert into address values(13,'STREET 13',1);
        insert into address values(14,'STREET 14',1);
        insert into address values(15,'STREET 15',1);
        insert into employee values(1,7369,'SMITH',13,'1980-12-17',800,2,1,1);
        insert into employee values(2,7499,'ALLEN',6,'1981-02-20',1600,3,2,1);
        insert into employee values(3,7521,'WARD',6,'1981-02-22',1250,3,3,1);
        insert into employee values(4,7566,'JONES',9,'1981-04-02',2975,2,4,1);
        insert into employee values(5,7654,'MARTIN',6,'1981-09-28',1250,3,5,1);
        insert into employee values(6,7698,'BLAKE',9,'1981-05-01',2850,3,6,1);
        insert into employee values(7,7782,'CLARK',9,'1981-06-09',2450,1,7,1);
        insert into employee values(8,7788,'SCOTT',4,'1982-12-09',3000.0,2,8,1);
        insert into employee values(9,7839,'KING',NULL,'1981-11-17',5000,1,9,1);
        insert into employee values(10,7844,'TURNER',6,'1981-09-08',1500,3,10,1);
        insert into employee values(11,7876,'ADAMS',8,'1983-01-12',1100,2,11,1);
        insert into employee values(12,7900,'JAMES',6,'1981-12-03',950,3,12,1);
        insert into employee values(13,7902,'FORD',4,'1981-12-03',3000,2,13,1);
        insert into employee values(14,7934,'MILLER',7,'1982-01-23',1300,1,14,1);

        insert into comp_key_department VALUES(1,1,10,'ACCOUNTING','NEW YORK',1);
        insert into comp_key_department VALUES(2,2,20,'RESEARCH','DALLAS',1);
        insert into comp_key_department VALUES(3,3,30,'SALES','CHICAGO',1);
        insert into comp_key_department VALUES(4,4,40,'OPERATIONS','BOSTON',1);
        insert into comp_key_address values(1,1,'STREET 1',1);
        insert into comp_key_address values(2,2,'STREET 2',1);
        insert into comp_key_address values(3,3,'STREET 3',1);
        insert into comp_key_address values(4,4,'STREET 4',1);
        insert into comp_key_address values(5,5,'STREET 5',1);
        insert into comp_key_address values(6,6,'STREET 6',1);
        insert into comp_key_address values(7,7,'STREET 7',1);
        insert into comp_key_address values(8,8,'STREET 8',1);
        insert into comp_key_address values(9,9,'STREET 9',1);
        insert into comp_key_address values(10,10,'STREET 10',1);
        insert into comp_key_address values(11,11,'STREET 11',1);
        insert into comp_key_address values(12,12,'STREET 12',1);
        insert into comp_key_address values(13,13,'STREET 13',1);
        insert into comp_key_address values(14,14,'STREET 14',1);
        insert into comp_key_employee values(1,1,7369,'SMITH',13,13,'1980-12-17',800,2,2,1,1,1);
        insert into comp_key_employee values(2,2,7499,'ALLEN',6,6,'1981-02-20',1600,3,3,2,2,1);
        insert into comp_key_employee values(3,3,7521,'WARD',6,6,'1981-02-22',1250,3,3,3,3,1);
        insert into comp_key_employee values(4,4,7566,'JONES',9,9,'1981-04-02',2975,2,2,4,4,1);
        insert into comp_key_employee values(5,5,7654,'MARTIN',6,6,'1981-09-28',1250,3,3,5,5,1);
        insert into comp_key_employee values(6,6,7698,'BLAKE',9,9,'1981-05-01',2850,3,3,6,6,1);
        insert into comp_key_employee values(7,7,7782,'CLARK',9,9,'1981-06-09',2450,1,1,7,7,1);
        insert into comp_key_employee values(8,8,7788,'SCOTT',4,4,'1982-12-09',3000.0,2,2,8,8,1);
        insert into comp_key_employee values(9,9,7839,'KING',NULL,NULL,'1981-11-17',5000,1,1,9,9,1);
        insert into comp_key_employee values(10,10,7844,'TURNER',6,6,'1981-09-08',1500,3,3,10,10,1);
        insert into comp_key_employee values(11,11,7876,'ADAMS',8,8,'1983-01-12',1100,2,2,11,11,1);
        insert into comp_key_employee values(12,12,7900,'JAMES',6,6,'1981-12-03',950,3,3,12,12,1);
        insert into comp_key_employee values(13,13,7902,'FORD',4,4,'1981-12-03',3000,2,2,13,13,1);
        insert into comp_key_employee values(14,14,7934,'MILLER',7,7,'1982-01-23',1300,1,1,14,14,1);

        insert into tense values (1, '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10');
        insert into job values (1, 'SALESMAN');
        insert into job values (2, 'MANAGER');
        insert into job values (3, 'PRESIDENT');
        insert into authority values (1, 10);
        insert into authority values (2, 20);
        insert into authority values (3, 30);
        insert into no_id values (1, 1);
        insert into no_id values (1, 1);
        """.trimIndent()
    override val resetSql: String
        get() = """
        truncate table identity_strategy;
        dbcc checkident ('identity_strategy', reseed, 1);
        alter sequence sequence_strategy_id restart with 1;
        """.trimIndent()
}
