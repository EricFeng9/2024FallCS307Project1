import json
import os
import time
import psycopg2
from datetime import date

SQL_FOLDER = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/sql_files'
INVALID_DATA_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/invalid_data.json'
BATCH_SIZE = 1000  # 定义每批写入的条数

# 确保 SQL 文件夹存在
if not os.path.exists(SQL_FOLDER):
    os.makedirs(SQL_FOLDER)

# 数据库连接
def connect_db():
    connection_params = {
        'dbname': 'postgres3',
        'user': 'test',
        'password': '123456',
        'host': 'localhost',
        'port': '5432'
    }
    return psycopg2.connect(**connection_params)

# 解析和转义单个值
def escape_sql_value(value):
    if isinstance(value, str):
        value = value.replace("'", "''").replace(",", "，").replace(";", "；")
        return f"'{value}'"
    elif isinstance(value, date):
        return f"'{value.strftime('%Y-%m-%d')}'"  # 格式化日期为 'YYYY-MM-DD'
    else:
        return str(value)

# 日期转换
def parse_date(date_dict):
    if date_dict:
        return date(date_dict['year'], date_dict['month'], date_dict['day'])
    return None

# 记录无效数据
def log_invalid_data(data):
    with open(INVALID_DATA_FILE, 'a', encoding='utf-8') as f:
        json.dump(data, f)
        f.write('\n')

# 插入到 articles 表
def insert_article(article):
    if 'id' not in article or 'title' not in article:
        log_invalid_data(article)
        return None
    title = escape_sql_value(article['title'])
    pub_model = escape_sql_value(article.get('pub_model', ''))
    date_created = escape_sql_value(parse_date(article['date_created']))
    date_completed = escape_sql_value(parse_date(article.get('date_completed')))
    return f"INSERT INTO articles (id, title, pub_model, date_created, date_completed) VALUES ({article['id']}, {title}, {pub_model}, {date_created}, {date_completed}) ON CONFLICT DO NOTHING;"


# 插入到 journals 表，并生成 article_journals 表的多对多关系
def insert_journal(journal, article_id, journals_sql, article_journals_sql):
    if 'id' not in journal:
        log_invalid_data(journal)
        return []

    journal_id = escape_sql_value(journal['id'])
    title = escape_sql_value(journal['title'])
    country = escape_sql_value(journal.get('country', ''))
    issn = escape_sql_value(journal.get('issn', ''))

    # 插入到 journals 表
    journals_sql.append(
        f"INSERT INTO journals (id, country, title, issn) VALUES ({journal_id}, {country}, {title}, {issn}) ON CONFLICT DO NOTHING;"
    )
    journal_issue = journal.get('journal_issue')
    if journal_issue:
        volume = escape_sql_value(journal_issue.get('volume', ''))
        issue = escape_sql_value(journal_issue.get('issue', ''))
        journals_sql.append(
            f"INSERT INTO journal_issues (journal_id, volume, issue) VALUES ({escape_sql_value(journal['id'])}, {volume}, {issue}) ON CONFLICT DO NOTHING;"
        )
    # 将多对多关系加入 article_journals 表
    article_journals_sql.append(
        f"INSERT INTO article_journals (article_id, journal_id) VALUES ({article_id}, {journal_id}) ON CONFLICT DO NOTHING;"
    )
# 插入到 authors 表并将 affiliation 直接关联 author_id
def insert_authors_with_affiliations(authors,article_id, authors_sql, affiliations_sql,author_articles_sql):
    for author in authors:
        if 'last_name' not in author and 'collective_name' not in author:
            log_invalid_data(author)
            continue
        last_name = escape_sql_value(author.get('last_name', author.get('collective_name', '')))
        fore_name = escape_sql_value(author.get('fore_name', ''))
        initials = escape_sql_value(author.get('initials', ''))
        collective_name = 'collective_name' in author

        # 插入到 authors 表
        author_sql = f"INSERT INTO authors (last_name, fore_name, initials, collective_name) VALUES ({last_name}, {fore_name}, {initials}, {collective_name})  ON CONFLICT DO NOTHING RETURNING id;"
        authors_sql.append(author_sql)

        # 为每个 affiliation 插入到 affiliations 表
        for affiliation in author.get('affiliation', []):
            affiliation = escape_sql_value(affiliation)
            affiliations_sql.append(
                f"INSERT INTO affiliations (author_id, affiliation) VALUES ((SELECT id FROM authors WHERE last_name = {last_name} AND fore_name = {fore_name} AND initials = {initials} LIMIT 1), {affiliation}) ON CONFLICT DO NOTHING;"
            )
            # 将多对多关系加入 author_articles 表
        author_articles_sql.append(
            f"INSERT INTO author_articles (author_id, article_id) "
            f"VALUES ((SELECT id FROM authors WHERE last_name = {last_name} AND fore_name = {fore_name} AND initials = {initials} LIMIT 1), {article_id}) "
            f"ON CONFLICT DO NOTHING;"
        )



# 插入到 keywords 表
def insert_keywords(article_id, keywords):
    return [f"INSERT INTO keywords (article_id, keyword) VALUES ({article_id}, {escape_sql_value(keyword)}) ON CONFLICT DO NOTHING;" for keyword in keywords]

def insert_publication_types(article_id, publication_types):
    sql_statements = []
    for pub_type in publication_types:
        if 'id' not in pub_type or 'name' not in pub_type:
            log_invalid_data(pub_type)
            continue
        name = escape_sql_value(pub_type['name'])
        sql_statements.append(
            f"INSERT INTO publication_types (id, article_id, name) VALUES ({escape_sql_value(pub_type['id'])}, {article_id}, {name}) ON CONFLICT DO NOTHING;"
        )
    return sql_statements

def insert_grants(article_id, grants):
    sql_statements = []
    for grant in grants:
        if 'agency' not in grant:
            log_invalid_data(grant)
            continue
        grant_id = escape_sql_value(grant.get('grant_id', ''))
        acronym = escape_sql_value(grant.get('acronym', ''))
        country = escape_sql_value(grant.get('country', ''))
        agency = escape_sql_value(grant['agency'])
        sql_statements.append(
            f"INSERT INTO grants (grant_id, acronym, country, agency, article_id) VALUES ({grant_id}, {acronym}, {country}, {agency}, {article_id}) ON CONFLICT DO NOTHING;"
        )
    return sql_statements

def insert_article_ids(article_id, article_ids):
    sql_statements = []
    for article_id_entry in article_ids:
        if 'id' not in article_id_entry:
            log_invalid_data(article_id_entry)
            continue
        article_type = escape_sql_value(article_id_entry['ty'])
        article_id_val = escape_sql_value(article_id_entry.get('id', ''))

        sql_statements.append(
            f"INSERT INTO article_ids (article_id, type, id) VALUES ({article_id}, {article_type}, {article_id_val}) ON CONFLICT DO NOTHING;"
        )
    return sql_statements

def insert_article_references(article_id, references):
    return [f"INSERT INTO article_references (article_id, reference_id) VALUES ({article_id}, {ref}) ON CONFLICT DO NOTHING;" for ref in references]

# 批量写入 SQL 文件
def write_sql_batch_to_file(filename, sql_statements):
    if not sql_statements:
        return
    filepath = os.path.join(SQL_FOLDER, filename)
    with open(filepath, 'a', encoding='utf-8') as f:
        f.write('\n'.join(sql_statements) + '\n')

# 处理 .ndjson 文件并生成 SQL 文件
def process_ndjson(file_path):
    articles_sql = []
    journals_sql = []
    authors_sql = []
    affiliations_sql = []
    keywords_sql = []
    publication_types_sql = []
    grants_sql = []
    article_ids_sql = []
    article_references_sql = []
    author_articles_sql = []
    article_journals_sql = []
    start = time.time()
    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            article = json.loads(line.strip())
            article_sql = insert_article(article)
            if article_sql:
                articles_sql.append(article_sql)
            if 'author' in article:
                insert_authors_with_affiliations(article['author'], article['id'], authors_sql, affiliations_sql,author_articles_sql)

            if 'journal' in article:
                insert_journal(article['journal'], article['id'], journals_sql, article_journals_sql)

            if 'keywords' in article:
                keywords_sql.extend(insert_keywords(article['id'], article['keywords']))

            if 'publication_types' in article:
                publication_types_sql.extend(insert_publication_types(article['id'], article['publication_types']))

            if 'grant' in article:
                grants_sql.extend(insert_grants(article['id'], article['grant']))

            if 'article_ids' in article:
                article_ids_sql.extend(insert_article_ids(article['id'], article['article_ids']))

            if 'references' in article:
                article_references_sql.extend(insert_article_references(article['id'], article['references']))

            # 批量写入每个表的 SQL 语句到文件
            if len(articles_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('articles.sql', articles_sql)
                articles_sql.clear()
            if len(journals_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('journals.sql', journals_sql)
                journals_sql.clear()
            if len(authors_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('authors.sql', authors_sql)
                authors_sql.clear()
            if len(affiliations_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('affiliations.sql', affiliations_sql)
                affiliations_sql.clear()
            if len(keywords_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('keywords.sql', keywords_sql)
                keywords_sql.clear()
            if len(author_articles_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('author_articles.sql', author_articles_sql)
                author_articles_sql.clear()

            if len(article_journals_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('article_journals.sql', article_journals_sql)
                article_journals_sql.clear()

            if len(publication_types_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('publication_types.sql', publication_types_sql)
                publication_types_sql.clear()
            if len(grants_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('grants.sql', grants_sql)
                grants_sql.clear()
            if len(article_ids_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('article_ids.sql', article_ids_sql)
                article_ids_sql.clear()
            if len(article_references_sql) >= BATCH_SIZE:
                write_sql_batch_to_file('article_references.sql', article_references_sql)
                article_references_sql.clear()

    # 写入剩余的 SQL 语句
    write_sql_batch_to_file('articles.sql', articles_sql)
    write_sql_batch_to_file('journals.sql', journals_sql)
    write_sql_batch_to_file('authors.sql', authors_sql)
    write_sql_batch_to_file('affiliations.sql', affiliations_sql)
    write_sql_batch_to_file('keywords.sql', keywords_sql)
    write_sql_batch_to_file('publication_types.sql', publication_types_sql)
    write_sql_batch_to_file('grants.sql', grants_sql)
    write_sql_batch_to_file('article_ids.sql', article_ids_sql)
    write_sql_batch_to_file('article_references.sql', article_references_sql)
    end = time.time() - start
    print("Total execution time:", end, "seconds")
    print("Finished processing and generating SQL files.")

# 运行数据处理
if __name__ == "__main__":
    process_ndjson("C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/pubmed24n.ndjson")
