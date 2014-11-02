# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "DEBIANBINARYPACKAGES" ("repo" VARCHAR NOT NULL,"distribution" VARCHAR NOT NULL,"component" VARCHAR NOT NULL,"name" VARCHAR NOT NULL,"version" VARCHAR NOT NULL,"arch" VARCHAR NOT NULL,"fileid" VARCHAR NOT NULL);
create table "FILES" ("id" VARCHAR NOT NULL PRIMARY KEY,"filename" VARCHAR NOT NULL);
create table "MAVENARTIFACTS" ("repoid" INTEGER NOT NULL,"group" VARCHAR NOT NULL,"artifact" VARCHAR NOT NULL,"version" VARCHAR NOT NULL,"fileid" VARCHAR NOT NULL,"filename" VARCHAR NOT NULL);
create table "MAVENREPOSITORIES" ("id" INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,"name" VARCHAR NOT NULL);
create table "TOKENS" ("token" VARCHAR NOT NULL PRIMARY KEY,"username" VARCHAR NOT NULL,"addedon" BIGINT NOT NULL);
create table "USERS" ("username" VARCHAR NOT NULL PRIMARY KEY,"password" VARCHAR NOT NULL,"isadmin" BOOLEAN NOT NULL);

# --- !Downs

drop table "USERS";
drop table "TOKENS";
drop table "MAVENREPOSITORIES";
drop table "MAVENARTIFACTS";
drop table "FILES";
drop table "DEBIANBINARYPACKAGES";

