import os
import time
import psycopg2
from concurrent.futures import ThreadPoolExecutor, as_completed

SQL_FOLDER = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/sql_files'
CONSTRAINTS_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/yueshu.sql'
DISABLE_TRIGGERS_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/disable_triggers.sql'
ENABLE_TRIGGERS_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/enable_triggers.sql'
BATCH_SIZE = 1000
MAX_WORKERS = 8


def create_db_connection():
    connection_params = {
        'dbname': 'postgres3',
        'user': 'test',
        'password': '123456',
        'host': 'localhost',
        'port': '5432',
        'client_encoding': 'UTF8'
    }
    return psycopg2.connect(**connection_params)


def execute_script(script_file, description="script"):
    conn = create_db_connection()
    start_time = time.time()
    try:
        with conn.cursor() as cur, open(script_file, 'r', encoding='utf-8') as file:
            sql_script = file.read()
            cur.execute(sql_script)
            conn.commit()
            duration = time.time() - start_time
            print(f"Executed {description} from {script_file} in {duration:.2f} seconds.")
    except Exception as e:
        conn.rollback()
        print(f"Error executing {description} from {script_file}: {e}")
    finally:
        conn.close()


def execute_sql_file_in_batches(filepath, batch_size=BATCH_SIZE):
    conn = create_db_connection()
    start_time = time.time()
    try:
        with conn, conn.cursor() as cur:
            with open(filepath, 'r', encoding='utf-8') as file:
                sql_statements = file.read().strip().split(';')
                cur.execute("BEGIN;")
                batch = []
                for statement in sql_statements:
                    if statement.strip():
                        batch.append(statement + ';')
                    if len(batch) >= batch_size:
                        cur.execute(" ".join(batch))
                        batch = []
                if batch:
                    cur.execute(" ".join(batch))
                cur.execute("COMMIT;")
                duration = time.time() - start_time
                print(f"Executed {filepath} in batches successfully in {duration:.2f} seconds.")
    except Exception as e:
        conn.rollback()
        print(f"Error executing {filepath}: {e}")
    finally:
        conn.close()


def execute_all_sql_files():
    primary_sql_files = [os.path.join(SQL_FOLDER, file) for file in [
        'articles.sql', 'journals.sql', 'authors.sql', 'keywords.sql',
        'publication_types.sql', 'grants.sql', 'article_ids.sql',
        'journal_issues.sql', 'article_references.sql', 'affiliations.sql'
    ]]

    # Secondary files (dependent on primary files)
    secondary_sql_files = [os.path.join(SQL_FOLDER, file) for file in [
        'author_articles.sql', 'article_journals.sql'
    ]]

    total_start_time = time.time()
    execute_script(DISABLE_TRIGGERS_FILE, "disable triggers")

    # Execute primary files concurrently
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        primary_futures = {executor.submit(execute_sql_file_in_batches, file): file for file in primary_sql_files}
        for future in as_completed(primary_futures):
            try:
                future.result()
            except Exception as exc:
                print(f"Error executing primary file {primary_futures[future]}: {exc}")

    # Execute secondary files sequentially after primary files
    for file in secondary_sql_files:
        execute_sql_file_in_batches(file)

    execute_script(ENABLE_TRIGGERS_FILE, "enable triggers")
    total_duration = time.time() - total_start_time
    print(f"Total execution time for all SQL files: {total_duration:.2f} seconds.")


if __name__ == "__main__":
    execute_all_sql_files()
    execute_script(CONSTRAINTS_FILE, "apply constraints")
