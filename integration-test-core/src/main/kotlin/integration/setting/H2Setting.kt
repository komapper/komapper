package integration.setting

interface H2Setting<CONFIG> : Setting<CONFIG> {

    override val dbms: Dbms get() = Dbms.H2
    override val createSql: String
        get() = """
        create sequence if not exists sequence_strategy_id start with 1 increment by 100;
        create sequence if not exists my_sequence_strategy_id start with 1 increment by 100;
        create sequence if not exists person_id_sequence start with 1 increment by 100;

        create table if not exists department(department_id integer not null primary key, department_no integer not null unique,department_name varchar(20),location varchar(20) default 'tokyo', version integer);
        create table if not exists address(address_id integer not null primary key, street varchar(20) unique, version integer);
        create table if not exists address_archive(address_id integer not null primary key, street varchar(20) unique, version integer);
        create table if not exists employee(employee_id integer not null primary key, employee_no integer not null ,employee_name varchar(20),manager_id integer,hiredate date,salary numeric(7,2),department_id integer,address_id integer,version integer, constraint fk_department_id foreign key(department_id) references department(department_id), constraint fk_address_id foreign key(address_id) references address(address_id));
        create table if not exists person(person_id integer not null primary key, name varchar(20), created_at timestamp, updated_at timestamp, version integer);
        create table if not exists "order"("order_id" integer not null primary key, "value" varchar(20));

        create table if not exists comp_key_department(department_id1 integer not null, department_id2 integer not null, department_no integer not null unique,department_name varchar(20),location varchar(20) default 'tokyo', version integer, constraint pk_comp_key_department primary key(department_id1, department_id2));
        create table if not exists comp_key_address(address_id1 integer not null, address_id2 integer not null, street varchar(20), version integer, constraint pk_comp_key_address primary key(address_id1, address_id2));
        create table if not exists comp_key_employee(employee_id1 integer not null, employee_id2 integer not null, employee_no integer not null ,employee_name varchar(20),manager_id1 integer,manager_id2 integer,hiredate date,salary numeric(7,2),department_id1 integer,department_id2 integer,address_id1 integer,address_id2 integer,version integer, constraint pk_comp_key_employee primary key(employee_id1, employee_id2), constraint fk_comp_key_department_id foreign key(department_id1, department_id2) references comp_key_department(department_id1, department_id2), constraint fk_comp_key_address_id foreign key(address_id1, address_id2) references comp_key_address(address_id1, address_id2));

        create table if not exists large_object(id numeric(8) not null primary key, name varchar(20), large_name clob, bytes binary, large_bytes blob, dto binary, large_dto blob);
        create table if not exists tense (id integer primary key,date_date date, date_time time, date_timestamp timestamp, cal_date date, cal_time time, cal_timestamp timestamp, sql_date date, sql_time time, sql_timestamp timestamp);
        create table if not exists job (id integer not null primary key, job_type varchar(20));
        create table if not exists authority (id integer not null primary key, authority_type integer);
        create table if not exists no_id (value1 integer, value2 integer);
        create table if not exists owner_of_no_id (id integer not null primary key, no_id_value1 integer);
        create table if not exists constraint_checking (primary_key integer primary key, unique_key integer unique, foreign_key integer, check_constraint integer, not_null integer not null, constraint ck_constraint_checking_1 check (check_constraint > 0), constraint fk_job_id foreign key (foreign_key) references job (id));
        create table if not exists pattern ("value" varchar(10));

        create table if not exists id_generator(pk varchar(20) not null primary key, "value" integer not null);
        create table if not exists my_id_generator(my_pk varchar(20) not null primary key, my_value integer not null);
        create table if not exists auto_strategy(id integer not null generated always as identity primary key, "value" varchar(10));
        create table if not exists identity_strategy(id integer not null generated always as identity primary key, "value" varchar(10));
        create table if not exists sequence_strategy(id integer not null primary key, "value" varchar(10));
        create table if not exists sequence_strategy2(id integer not null primary key, "value" varchar(10));
        create table if not exists table_strategy(id integer not null primary key, "value" varchar(10));
        create table if not exists table_strategy2(id integer not null primary key, "value" varchar(10));

        create table if not exists any_test(id integer not null primary key, "value" other);
        create table if not exists array_test(id integer not null primary key, "value" array);
        create table if not exists big_decimal_test(id integer not null primary key, "value" bigint);
        create table if not exists big_integer_test(id integer not null primary key, "value" bigint);
        create table if not exists boolean_test(id integer not null primary key, "value" bool);
        create table if not exists byte_test(id integer not null primary key, "value" tinyint);
        create table if not exists byte_array_test(id integer not null primary key, "value" binary(3));
        create table if not exists double_test(id integer not null primary key, "value" double);
        create table if not exists enum_test(id integer not null primary key, "value" varchar(20));
        create table if not exists float_test(id integer not null primary key, "value" float);
        create table if not exists int_test(id integer not null primary key, "value" integer);
        create table if not exists local_date_time_test(id integer not null primary key, "value" timestamp);
        create table if not exists local_date_test(id integer not null primary key, "value" date);
        create table if not exists local_time_test(id integer not null primary key, "value" time);
        create table if not exists long_test(id integer not null primary key, "value" bigint);
        create table if not exists offset_date_time_test(id integer not null primary key, "value" timestamp with time zone);
        create table if not exists short_test(id integer not null primary key, "value" smallint);
        create table if not exists sqlxml_test(id integer not null primary key, "value" clob);
        create table if not exists string_test(id integer not null primary key, "value" varchar(20));
        create table if not exists uuid_test(id integer not null primary key, "value" uuid);

        INSERT INTO DEPARTMENT VALUES(1,10,'ACCOUNTING','NEW YORK',1);
        INSERT INTO DEPARTMENT VALUES(2,20,'RESEARCH','DALLAS',1);
        INSERT INTO DEPARTMENT VALUES(3,30,'SALES','CHICAGO',1);
        INSERT INTO DEPARTMENT VALUES(4,40,'OPERATIONS','BOSTON',1);
        INSERT INTO ADDRESS VALUES(1,'STREET 1',1);
        INSERT INTO ADDRESS VALUES(2,'STREET 2',1);
        INSERT INTO ADDRESS VALUES(3,'STREET 3',1);
        INSERT INTO ADDRESS VALUES(4,'STREET 4',1);
        INSERT INTO ADDRESS VALUES(5,'STREET 5',1);
        INSERT INTO ADDRESS VALUES(6,'STREET 6',1);
        INSERT INTO ADDRESS VALUES(7,'STREET 7',1);
        INSERT INTO ADDRESS VALUES(8,'STREET 8',1);
        INSERT INTO ADDRESS VALUES(9,'STREET 9',1);
        INSERT INTO ADDRESS VALUES(10,'STREET 10',1);
        INSERT INTO ADDRESS VALUES(11,'STREET 11',1);
        INSERT INTO ADDRESS VALUES(12,'STREET 12',1);
        INSERT INTO ADDRESS VALUES(13,'STREET 13',1);
        INSERT INTO ADDRESS VALUES(14,'STREET 14',1);
        INSERT INTO ADDRESS VALUES(15,'STREET 15',1);
        INSERT INTO EMPLOYEE VALUES(1,7369,'SMITH',13,'1980-12-17',800,2,1,1);
        INSERT INTO EMPLOYEE VALUES(2,7499,'ALLEN',6,'1981-02-20',1600,3,2,1);
        INSERT INTO EMPLOYEE VALUES(3,7521,'WARD',6,'1981-02-22',1250,3,3,1);
        INSERT INTO EMPLOYEE VALUES(4,7566,'JONES',9,'1981-04-02',2975,2,4,1);
        INSERT INTO EMPLOYEE VALUES(5,7654,'MARTIN',6,'1981-09-28',1250,3,5,1);
        INSERT INTO EMPLOYEE VALUES(6,7698,'BLAKE',9,'1981-05-01',2850,3,6,1);
        INSERT INTO EMPLOYEE VALUES(7,7782,'CLARK',9,'1981-06-09',2450,1,7,1);
        INSERT INTO EMPLOYEE VALUES(8,7788,'SCOTT',4,'1982-12-09',3000.0,2,8,1);
        INSERT INTO EMPLOYEE VALUES(9,7839,'KING',NULL,'1981-11-17',5000,1,9,1);
        INSERT INTO EMPLOYEE VALUES(10,7844,'TURNER',6,'1981-09-08',1500,3,10,1);
        INSERT INTO EMPLOYEE VALUES(11,7876,'ADAMS',8,'1983-01-12',1100,2,11,1);
        INSERT INTO EMPLOYEE VALUES(12,7900,'JAMES',6,'1981-12-03',950,3,12,1);
        INSERT INTO EMPLOYEE VALUES(13,7902,'FORD',4,'1981-12-03',3000,2,13,1);
        INSERT INTO EMPLOYEE VALUES(14,7934,'MILLER',7,'1982-01-23',1300,1,14,1);

        INSERT INTO COMP_KEY_DEPARTMENT VALUES(1,1,10,'ACCOUNTING','NEW YORK',1);
        INSERT INTO COMP_KEY_DEPARTMENT VALUES(2,2,20,'RESEARCH','DALLAS',1);
        INSERT INTO COMP_KEY_DEPARTMENT VALUES(3,3,30,'SALES','CHICAGO',1);
        INSERT INTO COMP_KEY_DEPARTMENT VALUES(4,4,40,'OPERATIONS','BOSTON',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(1,1,'STREET 1',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(2,2,'STREET 2',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(3,3,'STREET 3',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(4,4,'STREET 4',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(5,5,'STREET 5',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(6,6,'STREET 6',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(7,7,'STREET 7',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(8,8,'STREET 8',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(9,9,'STREET 9',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(10,10,'STREET 10',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(11,11,'STREET 11',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(12,12,'STREET 12',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(13,13,'STREET 13',1);
        INSERT INTO COMP_KEY_ADDRESS VALUES(14,14,'STREET 14',1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(1,1,7369,'SMITH',13,13,'1980-12-17',800,2,2,1,1,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(2,2,7499,'ALLEN',6,6,'1981-02-20',1600,3,3,2,2,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(3,3,7521,'WARD',6,6,'1981-02-22',1250,3,3,3,3,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(4,4,7566,'JONES',9,9,'1981-04-02',2975,2,2,4,4,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(5,5,7654,'MARTIN',6,6,'1981-09-28',1250,3,3,5,5,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(6,6,7698,'BLAKE',9,9,'1981-05-01',2850,3,3,6,6,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(7,7,7782,'CLARK',9,9,'1981-06-09',2450,1,1,7,7,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(8,8,7788,'SCOTT',4,4,'1982-12-09',3000.0,2,2,8,8,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(9,9,7839,'KING',NULL,NULL,'1981-11-17',5000,1,1,9,9,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(10,10,7844,'TURNER',6,6,'1981-09-08',1500,3,3,10,10,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(11,11,7876,'ADAMS',8,8,'1983-01-12',1100,2,2,11,11,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(12,12,7900,'JAMES',6,6,'1981-12-03',950,3,3,12,12,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(13,13,7902,'FORD',4,4,'1981-12-03',3000,2,2,13,13,1);
        INSERT INTO COMP_KEY_EMPLOYEE VALUES(14,14,7934,'MILLER',7,7,'1982-01-23',1300,1,1,14,14,1);

        INSERT INTO TENSE VALUES (1, '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10', '2005-02-14', '12:11:10', '2005-02-14 12:11:10');
        INSERT INTO JOB VALUES (1, 'SALESMAN');
        INSERT INTO JOB VALUES (2, 'MANAGER');
        INSERT INTO JOB VALUES (3, 'PRESIDENT');
        INSERT INTO AUTHORITY VALUES (1, 10);
        INSERT INTO AUTHORITY VALUES (2, 20);
        INSERT INTO AUTHORITY VALUES (3, 30);
        INSERT INTO NO_ID VALUES (1, 1);
        INSERT INTO NO_ID VALUES (1, 1);

        INSERT INTO ID_GENERATOR VALUES('TABLE_STRATEGY_ID', 1);
        """.trimIndent()
    override val resetSql: String
        get() = """
        ALTER TABLE IDENTITY_STRATEGY ALTER COLUMN ID RESTART WITH 1;
        ALTER SEQUENCE SEQUENCE_STRATEGY_ID RESTART WITH 1
        """.trimIndent()
}
