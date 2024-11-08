--- 删除已有表格并使用 CASCADE 删除其依赖
drop table if exists article_ids cascade;
drop table if exists grants cascade;
drop table if exists publication_types cascade;
drop table if exists authors cascade;
drop table if exists journal_issues cascade;
drop table if exists journals cascade;
drop table if exists keywords cascade;
drop table if exists affiliations cascade;
drop table if exists author_affiliations cascade;
drop table if exists article_references cascade;
drop table if exists articles cascade;

-- 1. 主表 articles，存储文章的核心信息
create table articles (
    id integer,  -- 唯一标识符，无前导零的 1 到 8 位数字
    title text,  -- 文章的完整标题
    pub_model varchar(50),  -- 出版模型
    date_created date,  -- 文章创建日期，格式为年-月-日
    date_completed date  -- 文章完成日期，可能为空
);

-- 2. journals 表，存储期刊信息
create table journals (
    id varchar(50),  -- 期刊唯一标识符
    country varchar(50),  -- 期刊的出版国家
    title text,  -- 期刊的完整名称
    issn char(9)  -- 国际标准刊号（ISSN）
);

-- 3. journal_issues 表，存储期刊的卷和期信息
create table journal_issues (
    journal_id varchar(50),  -- 外键
    volume varchar(50),  -- 期刊的卷号
    issue varchar(50)  -- 期刊的期号
);

-- 4. authors 表，存储作者信息
create table authors (
    id serial,  -- 自动递增的唯一标识符
    last_name varchar(255),  -- 作者的姓
    fore_name varchar(255) default '',  -- 作者的名字
    initials varchar(50) default '',  -- 作者姓名缩写
    collective_name boolean default false -- 是否为集体名称
);

-- 单独的 affiliations 表，用于存储单位信息
CREATE TABLE affiliations (
    id SERIAL,  -- 自动递增的唯一标识符
    affiliation TEXT  -- 单位信息
);

-- 中间表 author_affiliations，用于建立 authors 与 affiliations 的多对多关系
CREATE TABLE author_affiliations (
    author_id INTEGER,  -- 引用 authors 表
    affiliation_id INTEGER  -- 引用 affiliations 表
);

-- 5. keywords 表，与文章关联，存储关键词
create table keywords (
    article_id integer,  -- 外键
    keyword varchar(255)  -- 单个关键词
);

-- 6. publication_types 表，与文章关联，存储出版类型
create table publication_types (
    id varchar(50),  -- 出版类型的唯一标识符
    article_id integer,  -- 外键
    name text  -- 出版类型名称
);

-- 7. grants 表，与文章关联，存储资助信息
create table grants (
    grant_id varchar(50),  -- 资助的唯一标识符
    acronym varchar(50) default '',  -- 资助的缩写
    country varchar(100) default '',  -- 资助国家
    agency text,  -- 提供资助的机构
    article_id integer  -- 外键
);

-- 8. article_ids 表，与文章关联，存储多种标识符
create table article_ids (
    article_id integer,  -- 外键
    type varchar(50),  -- 标识符类型
    id varchar(255)  -- 标识符的实际值
);

-- 9. article_references 表，存储文章引用
create table article_references (
    article_id integer,  -- 外键
    reference_id integer  -- 被引用文章的 ID
);
