import json
import csv
import time
import psycopg2
from datetime import datetime

# 文件路径
NDJSON_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/pubmed24n.ndjson'
CSV_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/test.csv'

# 数据库连接
def connect_db():
    return psycopg2.connect(
        dbname='postgres',
        user='test',
        password='123456',
        host='localhost',
        port='5432'
    )

def load_ndjson_data(file_path):
    data = []
    with open(file_path, 'r', encoding='utf-8') as file:
        for i, line in enumerate(file):
            if i >= 100000:  # 只加载前10000条数据
                break
            data.append(json.loads(line))
    return data

# 插入数据库的时间
def insert_to_db(data):
    conn = connect_db()
    start_time = time.time()
    with conn, conn.cursor() as cursor:
        for entry in data:
            article_id = entry.get('id')
            title = entry.get('title')
            pub_model = entry.get('pub_model')

            # 提取 'date_created' 字段
            date_created_raw = entry.get('date_created')
            if isinstance(date_created_raw, dict):
                # 假设 'date_created' 字段中包含了一个 'date' 键，提取该值
                date_created = date_created_raw.get('date')
            else:
                date_created = date_created_raw

            # 提取 'date_completed' 字段
            date_completed_raw = entry.get('date_completed')
            if isinstance(date_completed_raw, dict):
                # 假设 'date_completed' 字段中包含了一个 'date' 键，提取该值
                date_completed = date_completed_raw.get('date')
            else:
                date_completed = date_completed_raw

            # 检查日期字段是否为空
            if date_created and isinstance(date_created, str):
                try:
                    date_created = datetime.strptime(date_created, '%Y-%m-%d')
                except ValueError:
                    date_created = None  # 如果无法解析日期，设为 None
            if date_completed and isinstance(date_completed, str):
                try:
                    date_completed = datetime.strptime(date_completed, '%Y-%m-%d')
                except ValueError:
                    date_completed = None  # 如果无法解析日期，设为 None

            # 如果 date_created 为空，可以设置为当前日期，或者一个默认日期
            if date_created is None:
                date_created = datetime.now()  # 或者你可以选择其他默认日期

            # 如果 date_completed 为空，可以设置为当前日期，或者一个默认日期
            if date_completed is None:
                date_completed = datetime.now()  # 或者你可以选择其他默认日期

            # 插入数据
            cursor.execute("""
                INSERT INTO articles (id, title, pub_model, date_created, date_completed)
                VALUES (%s, %s, %s, %s, %s)
                ON CONFLICT DO NOTHING
                """, (
                article_id,
                title,
                pub_model,
                date_created,
                date_completed
            ))
        conn.commit()
    return time.time() - start_time


# 将数据写入到CSV文件
def write_to_csv(data, filename):
    start_time = time.time()
    with open(filename, mode='w', newline='', encoding='utf-8') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(['id', 'title', 'pub_model', 'date_created', 'date_completed'])
        for entry in data:
            writer.writerow([
                entry.get('id'),
                entry.get('title'),
                entry.get('pub_model'),
                entry.get('date_created'),
                entry.get('date_completed')
            ])
    return time.time() - start_time

# 查询数据库的时间
def query_db():
    conn = connect_db()
    start_time = time.time()
    with conn.cursor() as cursor:
        cursor.execute("SELECT * FROM articles WHERE id = 999")
        result = cursor.fetchone()
    return time.time() - start_time

# 查询CSV的时间
def query_csv(filename):
    start_time = time.time()
    with open(filename, mode='r', encoding='utf-8') as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            if row['id'] == '999':
                break
    return time.time() - start_time

# 更新数据库的时间
def update_db():
    conn = connect_db()
    start_time = time.time()
    with conn, conn.cursor() as cursor:
        cursor.execute("UPDATE articles SET title = 'Updated Title' WHERE id = 1")
        conn.commit()
    return time.time() - start_time

# 更新CSV的时间
def update_csv(filename):
    start_time = time.time()
    rows = []
    with open(filename, mode='r', encoding='utf-8') as csv_file:
        reader = csv.DictReader(csv_file)
        rows = list(reader)
    for row in rows:
        if row['id'] == '1':
            row['title'] = 'Updated Title'
    with open(filename, mode='w', newline='', encoding='utf-8') as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=['id', 'title', 'pub_model', 'date_created', 'date_completed'])
        writer.writeheader()
        writer.writerows(rows)
    return time.time() - start_time

# 删除数据库中的数据
def delete_from_db():
    conn = connect_db()
    start_time = time.time()
    with conn, conn.cursor() as cursor:
        cursor.execute("DELETE FROM articles WHERE id = 1")
        conn.commit()
    return time.time() - start_time

# 删除CSV中的数据
def delete_from_csv(filename):
    start_time = time.time()
    rows = []
    with open(filename, mode='r', encoding='utf-8') as csv_file:
        reader = csv.DictReader(csv_file)
        rows = [row for row in reader if row['id'] != '1']
    with open(filename, mode='w', newline='', encoding='utf-8') as csv_file:
        writer = csv.DictWriter(csv_file, fieldnames=['id', 'title', 'pub_model', 'date_created', 'date_completed'])
        writer.writeheader()
        writer.writerows(rows)
    return time.time() - start_time

# 主流程
data = load_ndjson_data(NDJSON_FILE)

# 插入操作
db_insert_time = insert_to_db(data)
csv_insert_time = write_to_csv(data, CSV_FILE)
print(f"Insertion Time - DBMS: {db_insert_time:.4f}s, File I/O: {csv_insert_time:.4f}s")

# 查询操作
db_query_time = query_db()
csv_query_time = query_csv(CSV_FILE)
print(f"Query Time - DBMS: {db_query_time:.4f}s, File I/O: {csv_query_time:.4f}s")

# 更新操作
db_update_time = update_db()
csv_update_time = update_csv(CSV_FILE)
print(f"Update Time - DBMS: {db_update_time:.4f}s, File I/O: {csv_update_time:.4f}s")

# 删除操作
db_delete_time = delete_from_db()
csv_delete_time = delete_from_csv(CSV_FILE)
print(f"Deletion Time - DBMS: {db_delete_time:.4f}s, File I/O: {csv_delete_time:.4f}s")
