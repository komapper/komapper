package integration.core

interface OracleSetting<CONFIG> : Setting<CONFIG> {

    override val dbms: Dbms get() = Dbms.ORACLE
    override val createSql: String
        get() = """
        create sequence sequence_strategy_id increment by 100 start with 1;
        create sequence person_id increment by 100 start with 1;

        create table department(department_id integer not null primary key, department_no integer not null unique,department_name varchar2(20),location varchar2(20) default 'tokyo', version integer);
        create table address(address_id integer not null primary key, street varchar2(20) unique, version integer);
        create table address_archive(address_id integer not null primary key, street varchar2(20) unique, version integer);
        create table employee(employee_id integer not null primary key, employee_no integer not null ,employee_name varchar2(20),manager_id integer,hiredate date,salary numeric(7,2),department_id integer,address_id integer, version integer, constraint fk_department_id foreign key(department_id) references department(department_id), constraint fk_address_id foreign key(address_id) references address(address_id));
        create table person(person_id integer not null primary key, name varchar2(20), created_at timestamp, updated_at timestamp, version integer);
        create table "order"("order_id" integer not null primary key, "value" varchar2(20));

        create table comp_key_department(department_id1 integer not null, department_id2 integer not null, department_no integer not null unique,department_name varchar2(20),location varchar2(20) default 'tokyo', version integer, constraint pk_comp_key_department primary key(department_id1, department_id2));
        create table comp_key_address(address_id1 integer not null, address_id2 integer not null, street varchar2(20), version integer, constraint pk_comp_key_address primary key(address_id1, address_id2));
        create table comp_key_employee(employee_id1 integer not null, employee_id2 integer not null, employee_no integer not null ,employee_name varchar2(20),manager_id1 integer,manager_id2 integer,hiredate date,salary numeric(7,2),department_id1 integer,department_id2 integer,address_id1 integer,address_id2 integer,version integer, constraint pk_comp_key_employee primary key(employee_id1, employee_id2), constraint fk_comp_key_department_id foreign key(department_id1, department_id2) references comp_key_department(department_id1, department_id2), constraint fk_comp_key_address_id foreign key(address_id1, address_id2) references comp_key_address(address_id1, address_id2));
                
        create table identity_strategy(id integer generated always as identity primary key, "value" varchar2(10));
        create table sequence_strategy(id integer not null primary key, "value" varchar2(10));

        create table big_decimal_test(id integer not null primary key, "value" decimal);
        create table big_integer_test(id integer not null primary key, "value" decimal);
        create table blob_test(id integer not null primary key, "value" blob);
        create table boolean_test(id integer not null primary key, "value" number(1, 0));
        create table byte_test(id integer not null primary key, "value" integer);
        create table byte_array_test(id integer not null primary key, "value" raw(1000));
        create table clob_test(id integer not null primary key, "value" clob);
        create table double_test(id integer not null primary key, "value" float);
        create table enum_test(id integer not null primary key, "value" varchar2(20));
        create table float_test(id integer not null primary key, "value" float);
        create table interval_day_test(id integer not null primary key, "value" interval day to second);
        create table interval_year_test(id integer not null primary key, "value" interval year to month);
        create table int_test(id integer not null primary key, "value" integer);
        create table local_date_time_test(id integer not null primary key, "value" date);
        create table local_date_test(id integer not null primary key, "value" date);
        create table local_time_test(id integer not null primary key, "value" timestamp);
        create table long_test(id integer not null primary key, "value" integer);
        create table offset_date_time_test(id integer not null primary key, "value" timestamp with time zone);
        create table short_test(id integer not null primary key, "value" integer);
        create table string_test(id integer not null primary key, "value" varchar2(20));
 
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
        insert into employee values(1,7369,'SMITH',13,TO_DATE('1980-12-17','YYYY-MM-DD'),800,2,1,1);
        insert into employee values(2,7499,'ALLEN',6,TO_DATE('1981-02-20','YYYY-MM-DD'),1600,3,2,1);
        insert into employee values(3,7521,'WARD',6,TO_DATE('1981-02-22','YYYY-MM-DD'),1250,3,3,1);
        insert into employee values(4,7566,'JONES',9,TO_DATE('1981-04-02','YYYY-MM-DD'),2975,2,4,1);
        insert into employee values(5,7654,'MARTIN',6,TO_DATE('1981-09-28','YYYY-MM-DD'),1250,3,5,1);
        insert into employee values(6,7698,'BLAKE',9,TO_DATE('1981-05-01','YYYY-MM-DD'),2850,3,6,1);
        insert into employee values(7,7782,'CLARK',9,TO_DATE('1981-06-09','YYYY-MM-DD'),2450,1,7,1);
        insert into employee values(8,7788,'SCOTT',4,TO_DATE('1982-12-09','YYYY-MM-DD'),3000.0,2,8,1);
        insert into employee values(9,7839,'KING',NULL,TO_DATE('1981-11-17','YYYY-MM-DD'),5000,1,9,1);
        insert into employee values(10,7844,'TURNER',6,TO_DATE('1981-09-08','YYYY-MM-DD'),1500,3,10,1);
        insert into employee values(11,7876,'ADAMS',8,TO_DATE('1983-01-12','YYYY-MM-DD'),1100,2,11,1);
        insert into employee values(12,7900,'JAMES',6,TO_DATE('1981-12-03','YYYY-MM-DD'),950,3,12,1);
        insert into employee values(13,7902,'FORD',4,TO_DATE('1981-12-03','YYYY-MM-DD'),3000,2,13,1);
        insert into employee values(14,7934,'MILLER',7,TO_DATE('1982-01-23','YYYY-MM-DD'),1300,1,14,1);

        insert into comp_key_department values(1,1,10,'ACCOUNTING','NEW YORK',1);
        insert into comp_key_department values(2,2,20,'RESEARCH','DALLAS',1);
        insert into comp_key_department values(3,3,30,'SALES','CHICAGO',1);
        insert into comp_key_department values(4,4,40,'OPERATIONS','BOSTON',1);
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
        insert into comp_key_employee values(1,1,7369,'SMITH',13,13,TO_DATE('1980-12-17','YYYY-MM-DD'),800,2,2,1,1,1);
        insert into comp_key_employee values(2,2,7499,'ALLEN',6,6,TO_DATE('1981-02-20','YYYY-MM-DD'),1600,3,3,2,2,1);
        insert into comp_key_employee values(3,3,7521,'WARD',6,6,TO_DATE('1981-02-22','YYYY-MM-DD'),1250,3,3,3,3,1);
        insert into comp_key_employee values(4,4,7566,'JONES',9,9,TO_DATE('1981-04-02','YYYY-MM-DD'),2975,2,2,4,4,1);
        insert into comp_key_employee values(5,5,7654,'MARTIN',6,6,TO_DATE('1981-09-28','YYYY-MM-DD'),1250,3,3,5,5,1);
        insert into comp_key_employee values(6,6,7698,'BLAKE',9,9,TO_DATE('1981-05-01','YYYY-MM-DD'),2850,3,3,6,6,1);
        insert into comp_key_employee values(7,7,7782,'CLARK',9,9,TO_DATE('1981-06-09','YYYY-MM-DD'),2450,1,1,7,7,1);
        insert into comp_key_employee values(8,8,7788,'SCOTT',4,4,TO_DATE('1982-12-09','YYYY-MM-DD'),3000.0,2,2,8,8,1);
        insert into comp_key_employee values(9,9,7839,'KING',NULL,NULL,TO_DATE('1981-11-17','YYYY-MM-DD'),5000,1,1,9,9,1);
        insert into comp_key_employee values(10,10,7844,'TURNER',6,6,TO_DATE('1981-09-08','YYYY-MM-DD'),1500,3,3,10,10,1);
        insert into comp_key_employee values(11,11,7876,'ADAMS',8,8,TO_DATE('1983-01-12','YYYY-MM-DD'),1100,2,2,11,11,1);
        insert into comp_key_employee values(12,12,7900,'JAMES',6,6,TO_DATE('1981-12-03','YYYY-MM-DD'),950,3,3,12,12,1);
        insert into comp_key_employee values(13,13,7902,'FORD',4,4,TO_DATE('1981-12-03','YYYY-MM-DD'),3000,2,2,13,13,1);
        insert into comp_key_employee values(14,14,7934,'MILLER',7,7,TO_DATE('1982-01-23','YYYY-MM-DD'),1300,1,1,14,14,1);
        """.trimIndent()
    override val resetSql: String get() = """
        alter table identity_strategy modify(id generated as identity (start with 1));
        alter sequence sequence_strategy_id restart start with 1;
    """.trimIndent()
}
