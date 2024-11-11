import json
import time
import psycopg2
from datetime import date
from psycopg2.extras import execute_values

# 数据库连接
def connect_db():
    connection_params = {
        'dbname': 'postgres',
        'user': 'test',
        'password': '123456',
        'host': 'localhost',
        'port': '5432'
    }
    connection_string = " ".join([f"{k}={v}" for k, v in connection_params.items()])
    return psycopg2.connect(connection_string)

# 将字典形式的日期转换为 DATE 格式
def parse_date(date_dict):
    if date_dict:
        return date(date_dict['year'], date_dict['month'], date_dict['day'])
    return None

# 插入 articles 表
def insert_article(cur, article):
    sql = """
    INSERT INTO articles (id, title, pub_model, date_created, date_completed)
    VALUES (%s, %s, %s, %s, %s)
    ON CONFLICT (id) DO NOTHING;
    """
    cur.execute(sql, (
        article['id'],
        article['title'],
        article['pub_model'],
        parse_date(article['date_created']),
        parse_date(article.get('date_completed'))
    ))

# 插入 journals 和 journal_issues 表
def insert_journal(cur, journal):
    if not journal:
        return
    sql = """
    INSERT INTO journals (id, country, title, issn)
    VALUES (%s, %s, %s, %s)
    ON CONFLICT (id) DO NOTHING;
    """
    cur.execute(sql, (
        journal['id'],
        journal.get('country'),
        journal['title'],
        journal.get('issn')
    ))

    journal_issue = journal.get('journal_issue')
    if journal_issue:
        sql_issue = """
        INSERT INTO journal_issues (journal_id, volume, issue)
        VALUES (%s, %s, %s)
        ON CONFLICT (journal_id, volume, issue) DO NOTHING;
        """
        cur.execute(sql_issue, (
            journal['id'],
            journal_issue.get('volume', ''),
            journal_issue.get('issue', '')
        ))

# 插入 authors 表，同时处理 affiliations 表和 author_affiliations 表
def insert_authors(cur, article_id, authors):
    # 插入 author 数据
    sql_authors = """
    INSERT INTO authors (last_name, fore_name, initials, collective_name)
    VALUES (%s, %s, %s, %s)
    ON CONFLICT (last_name, fore_name, initials, collective_name) DO NOTHING
    RETURNING id;
    """

    # 查询或插入 affiliation 并返回 id
    sql_affiliations = """
    INSERT INTO affiliations (affiliation)
    VALUES (%s)
    ON CONFLICT (affiliation) DO NOTHING
    RETURNING id;
    """

    # 插入 author 和 affiliation 的关联关系
    sql_author_affiliations = """
    INSERT INTO author_affiliations (author_id, affiliation_id)
    VALUES (%s, %s)
    ON CONFLICT DO NOTHING;
    """

    for author in authors:
        # 获取 author 的基本信息
        affiliations = author.get('affiliation', [])
        if 'collective_name' in author:
            last_name = author['collective_name']
            fore_name, initials, collective_name = '', '', True
        else:
            last_name = author.get('last_name', '')
            fore_name = author.get('fore_name', '')
            initials = author.get('initials', '')
            collective_name = False

        # 插入 authors 表并获取 author_id
        cur.execute(sql_authors, (last_name, fore_name, initials, collective_name))
        author_id = cur.fetchone()[0] if cur.rowcount else None

        # 跳过没有 author_id 的情况
        if not author_id:
            continue

        # 处理每个 affiliation 并建立关联
        for affiliation in affiliations:
            cur.execute(sql_affiliations, (affiliation,))
            affiliation_id = cur.fetchone()[0] if cur.rowcount else None

            if affiliation_id:
                cur.execute(sql_author_affiliations, (author_id, affiliation_id))



# 插入 keywords 表
def insert_keywords(cur, article_id, keywords):
    sql = """
    INSERT INTO keywords (article_id, keyword)
    VALUES %s ON CONFLICT DO NOTHING;
    """
    keyword_data = [(article_id, keyword) for keyword in keywords]
    execute_values(cur, sql, keyword_data)

# 插入 publication_types 表
def insert_publication_types(cur, article_id, publication_types):
    sql = """
    INSERT INTO publication_types (id, article_id, name)
    VALUES %s ON CONFLICT DO NOTHING;
    """
    pub_type_data = [(ptype['id'], article_id, ptype['name']) for ptype in publication_types]
    execute_values(cur, sql, pub_type_data)

# 插入 grants 表
def insert_grants(cur, article_id, grants):
    sql = """
    INSERT INTO grants (grant_id, acronym, country, agency, article_id)
    VALUES %s ON CONFLICT (agency, article_id) DO NOTHING;
    """
    grant_data = [
        (
            grant.get('grant_id'),
            grant.get('acronym', ''),
            grant.get('country', ''),
            grant['agency'],
            article_id
        )
        for grant in grants if 'agency' in grant
    ]
    execute_values(cur, sql, grant_data)


# 插入 article_ids 表
def insert_article_ids(cur, article_id, article_ids):
    sql = """
    INSERT INTO article_ids (article_id, type, id)
    VALUES %s ON CONFLICT DO NOTHING;
    """
    # 筛选出包含非空 'id' 的数据
    id_data = [(article_id, aid['ty'], aid['id']) for aid in article_ids if 'id' in aid and aid['id']]

    if id_data:  # 确保有数据再执行插入
        execute_values(cur, sql, id_data)


# 插入 article_references 表
def insert_references(cur, article_id, references):
    sql = """
    INSERT INTO article_references (article_id, reference_id)
    VALUES %s ON CONFLICT DO NOTHING;
    """
    ref_data = [(article_id, ref) for ref in references]
    execute_values(cur, sql, ref_data)

# 处理 .ndjson 文件并插入数据
def process_ndjson(file_path):
    conn = connect_db()
    cur = conn.cursor()
    start = time.time()
    record_count = 0
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            for line in f:
                article = json.loads(line.strip())
                insert_article(cur, article)
                if 'journal' in article:
                    insert_journal(cur, article['journal'])
                if 'author' in article:
                    insert_authors(cur, article['id'], article['author'])
                if 'keywords' in article:
                    insert_keywords(cur, article['id'], article['keywords'])
                if 'publication_types' in article:
                    insert_publication_types(cur, article['id'], article['publication_types'])
                if 'grant' in article:
                    insert_grants(cur, article['id'], article['grant'])
                if 'article_ids' in article:
                    insert_article_ids(cur, article['id'], article['article_ids'])
                if 'references' in article:
                    insert_references(cur, article['id'], article['references'])
                record_count += 1
                if record_count % 1000 == 0:
                    print(f"Processed {record_count} records")
        conn.commit()
    except Exception as e:
        print(f"Error processing data: {e}")
        conn.rollback()
    finally:
        cur.close()
        conn.close()
        print(f"Finished processing {record_count} records in {time.time() - start:.2f} seconds")

# 调用函数处理数据
if __name__ == "__main__":
    process_ndjson("C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/pubmed24n.ndjson")
