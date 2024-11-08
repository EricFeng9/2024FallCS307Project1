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
    id integer primary key,  -- 唯一标识符，无前导零的 1 到 8 位数字
    title text not null,  -- 文章的完整标题，必填
    pub_model varchar(50) check (pub_model in ('Print', 'Print-Electronic', 'Electronic', 'Electronic-Print', 'Electronic-eCollection')),  -- 出版模型
    date_created date not null,  -- 文章创建日期，格式为年-月-日
    date_completed date  -- 文章完成日期，可能为空
);

-- 2. journals 表，存储期刊信息
create table journals (
    id varchar(50) primary key,  -- 期刊唯一标识符
    country varchar(50),  -- 期刊的出版国家
    title text not null,  -- 期刊的完整名称
    issn char(9)  -- 国际标准刊号（ISSN）
);

-- 3. journal_issues 表，存储期刊的卷和期信息
create table journal_issues (
    journal_id varchar(50) references journals(id),  -- 外键，指向 journals 表中的期刊 ID
    volume varchar(50),  -- 期刊的卷号，可为空
    issue varchar(50),  -- 期刊的期号，可为空
    primary key (journal_id, volume, issue)  -- 复合主键，唯一标识一个期刊的卷和期
);

-- 4. authors 表，存储作者信息，包括去重逻辑和 affiliation 字段
create table authors (
    id serial primary key,  -- 自动递增的唯一标识符
    last_name varchar(255) not null,  -- 作者的姓，必填
    fore_name varchar(255) default '',  -- 作者的名字，可为空，默认为空字符串
    initials varchar(50) default '',  -- 作者姓名缩写，可为空
    collective_name boolean default false,  -- 布尔值，指示 last_name 是否为集体名称
    unique (last_name, fore_name, initials, collective_name)  -- 去重约束，确保唯一性
);

-- 单独的 affiliations 表，用于存储单位信息
CREATE TABLE affiliations (
    id SERIAL PRIMARY KEY,  -- 自动递增的唯一标识符
    affiliation TEXT NOT NULL,  -- 单位信息
    UNIQUE (affiliation)  -- 确保单位信息唯一
);

-- 中间表 author_affiliations，用于建立 authors 与 affiliations 的多对多关系
CREATE TABLE author_affiliations (
    author_id INTEGER REFERENCES authors(id) ON DELETE CASCADE,  -- 引用 authors 表
    affiliation_id INTEGER REFERENCES affiliations(id) ON DELETE CASCADE,  -- 引用 affiliations 表
    PRIMARY KEY (author_id, affiliation_id)  -- 复合主键，确保唯一性
);


-- 5. keywords 表，与文章关联，存储关键词
create table keywords (
    article_id integer references articles(id),  -- 外键，指向 articles 表中的文章 ID
    keyword varchar(255) not null,  -- 单个关键词，必填
    primary key (article_id, keyword)  -- 复合主键，确保每篇文章的关键词唯一
);

-- 6. publication_types 表，与文章关联，存储出版类型
create table publication_types (
    id varchar(50) not null,  -- 出版类型的唯一标识符
    article_id integer references articles(id),  -- 外键，指向 articles 表中的文章 ID
    name text not null,  -- 出版类型名称，必填
    primary key (id, article_id)  -- 复合主键，确保每篇文章的出版类型唯一
);

-- 7. grants 表，与文章关联，存储资助信息
create table grants (
    grant_id varchar(50),  -- 资助的唯一标识符
    acronym varchar(50) default '',  -- 资助的缩写，可能为空，默认为空字符串
    country varchar(100) default '',  -- 资助国家，可能为空
    agency text not null,  -- 提供资助的机构，必填
    article_id integer references articles(id),  -- 外键，指向 articles 表中的文章 ID
    primary key (agency, article_id)  -- 复合主键，确保每个资助与每篇文章的唯一性
);

-- 8. article_ids 表，与文章关联，存储多种标识符
create table article_ids (
    article_id integer references articles(id),  -- 外键，指向 articles 表中的文章 ID
    type varchar(50) not null,  -- 标识符类型，例如 'pubmed' 或 'doi'
    id varchar(255),  -- 标识符的实际值
    primary key (article_id, type, id)  -- 复合主键，确保每篇文章的标识符类型唯一
);

-- 9. article_references 表，存储文章引用，避免使用 PostgreSQL 的保留字 "references"
create table article_references (
    article_id integer references articles(id),  -- 外键，指向 articles 表中的文章 ID
    reference_id integer,  -- 被引用文章的 ID
    primary key (article_id, reference_id)  -- 复合主键，确保引用的唯一性
);

SELECT * FROM authors WHERE collective_name = true;
