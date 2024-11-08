package integration.core

import org.komapper.core.Database

public interface PostgreSqlSetting<DATABASE : Database> : Setting<DATABASE> {
    override val dbms: Dbms get() = Dbms.POSTGRESQL
    override val createSql: String
        get() =
            """
            create type mood as enum ('SAD', 'OK', 'HAPPY');

            create sequence if not exists sequence_strategy_id increment by 100 start with 1;
            create sequence if not exists person_id increment by 100 start with 1;

            create table if not exists department(department_id integer not null primary key, department_no integer not null unique,department_name varchar(20),location varchar(20) default 'tokyo', version integer);
            create table if not exists address(address_id integer not null primary key, street varchar(20) unique, version integer);
            create table if not exists address_archive(address_id integer not null primary key, street varchar(20) unique, version integer);
            create table if not exists employee(employee_id integer not null primary key, employee_no integer not null ,employee_name varchar(20),manager_id integer,hiredate date,salary numeric(7,2),department_id integer,address_id integer, version integer, constraint fk_department_id foreign key(department_id) references department(department_id), constraint fk_address_id foreign key(address_id) references address(address_id));
            create table if not exists human(human_id integer not null primary key, name varchar(20), created_at timestamp with time zone, updated_at timestamp with time zone, version integer, created_by varchar(20), updated_by varchar(20));
            create table if not exists person(person_id integer not null primary key, name varchar(20), created_at timestamp, updated_at timestamp, version integer);
            create table if not exists "order"("order_id" integer not null primary key, "value" varchar(20));

            create table if not exists comp_key_department(department_id1 integer not null, department_id2 integer not null, department_no integer not null unique,department_name varchar(20),location varchar(20) default 'tokyo', version integer, constraint pk_comp_key_department primary key(department_id1, department_id2));
            create table if not exists comp_key_address(address_id1 integer not null, address_id2 integer not null, street varchar(20), version integer, constraint pk_comp_key_address primary key(address_id1, address_id2));
            create table if not exists comp_key_employee(employee_id1 integer not null, employee_id2 integer not null, employee_no integer not null ,employee_name varchar(20),manager_id1 integer,manager_id2 integer,hiredate date,salary numeric(7,2),department_id1 integer,department_id2 integer,address_id1 integer,address_id2 integer,version integer, constraint pk_comp_key_employee primary key(employee_id1, employee_id2), constraint fk_comp_key_department_id foreign key(department_id1, department_id2) references comp_key_department(department_id1, department_id2), constraint fk_comp_key_address_id foreign key(address_id1, address_id2) references comp_key_address(address_id1, address_id2));

            create table if not exists identity_strategy(id serial primary key, value varchar(10));
            create table if not exists sequence_strategy(id integer not null primary key, value varchar(10));

            create table if not exists array_data(id integer not null primary key, value text[]);
            create table if not exists big_decimal_data(id integer not null primary key, value numeric);
            create table if not exists big_integer_data(id integer not null primary key, value numeric);
            create table if not exists blob_data(id integer not null primary key, value bytea);
            create table if not exists boolean_data(id integer not null primary key, value boolean);
            create table if not exists byte_data(id integer not null primary key, value int2);
            create table if not exists byte_array_data(id integer not null primary key, value bytea);
            create table if not exists clob_data(id integer not null primary key, value text);
            create table if not exists double_data(id integer not null primary key, value float8);
            create table if not exists enum_data(id integer not null primary key, value varchar(20));
            create table if not exists enum_ordinal_data(id integer not null primary key, value integer);
            create table if not exists enum_property_data(id integer not null primary key, value integer);
            create table if not exists enum_udt_data(id integer not null primary key, value mood);
            create table if not exists float_data(id integer not null primary key, value float);
            create table if not exists instant_data(id integer not null primary key, value timestamp with time zone);
            create table if not exists int_data(id integer not null primary key, value integer);
            create table if not exists local_date_time_data(id integer not null primary key, value timestamp);
            create table if not exists local_date_data(id integer not null primary key, value date);
            create table if not exists local_time_data(id integer not null primary key, value time);
            create table if not exists long_data(id integer not null primary key, value bigint);
            create table if not exists offset_date_time_data(id integer not null primary key, value timestamp with time zone);
            create table if not exists short_data(id integer not null primary key, value smallint);
            create table if not exists sqlxml_data(id integer not null primary key, value xml);
            create table if not exists string_data(id integer not null primary key, value varchar(20));
            create table if not exists uuid_data(id integer not null primary key, value uuid);

            create table if not exists box_data(id integer not null primary key, value box);
            create table if not exists circle_data(id integer not null primary key, value circle);
            create table if not exists geometry_data(id integer not null primary key, value geometry);
            create table if not exists line_data(id integer not null primary key, value line);
            create table if not exists lseg_data(id integer not null primary key, value lseg);
            create table if not exists interval_data(id integer not null primary key, value interval);
            create table if not exists json_data(id integer not null primary key, value jsonb);
            create table if not exists path_data(id integer not null primary key, value path);
            create table if not exists point_data(id integer not null primary key, value point);
            create table if not exists polygon_data(id integer not null primary key, value polygon);

            create table if not exists friend(uuid1 uuid, uuid2 uuid, pending boolean, constraint pk_friend primary key(uuid1, uuid2));
            create unique index friend_unique_idx on friend (greatest(uuid1, uuid2), least(uuid1, uuid2));

            create table names(
                id bigint primary key not null,
                first_name varchar(255),
                last_name varchar(255),
                deleted_at timestamp with time zone null
            );
            create unique index idx_uq_last_name on names(last_name) where deleted_at is null;

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
            """.trimIndent()
    override val resetSql: String
        get() =
            """
            alter sequence identity_strategy_id_seq restart with 1;
            alter sequence sequence_strategy_id restart with 1
            """.trimIndent()
}
