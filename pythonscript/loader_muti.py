import os
import time
import psycopg2

SQL_FOLDER = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/sql_files'
CONSTRAINTS_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/yueshu.sql'
DISABLE_TRIGGERS_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/disable_triggers.sql'
ENABLE_TRIGGERS_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/enable_triggers.sql'

#39608
# Database connection
def connect_db():
    connection_params = {
        'dbname': 'postgres3',
        'user': 'test',
        'password': '123456',
        'host': 'localhost',
        'port': '5432'
    }
    return psycopg2.connect(**connection_params)


# Execute enable/disable triggers and constraints scripts
def execute_script(conn, script_file):
    with conn.cursor() as cur:
        with open(script_file, 'r', encoding='utf-8') as file:
            sql_script = file.read()
            try:
                cur.execute(sql_script)
                conn.commit()
                print(f"Executed script {script_file} successfully.")
            except Exception as e:
                conn.rollback()
                print(f"Error executing script {script_file}: {e}")


# Directly execute SQL file contents with timing
def execute_sql_file(filepath, conn):
    with conn.cursor() as cur:
        start_time = time.time()  # Start timing
        try:
            with open(filepath, 'r', encoding='utf-8') as file:
                sql_script = file.read()  # Read the entire SQL file

                # Execute the entire SQL script in one go
                cur.execute(sql_script)
                conn.commit()

                # Calculate duration and efficiency
                duration = time.time() - start_time
                record_count = sql_script.count("INSERT INTO")  # Count number of insert statements
                efficiency = record_count / duration if duration > 0 else 0
                print(f"Executed {filepath} successfully in {duration:.2f} seconds.")
                print(f"Records inserted: {record_count}, Efficiency: {efficiency:.2f} records/second")
        except Exception as e:
            conn.rollback()
            print(f"Error executing {filepath}: {e}")


# Execute SQL files in the specified order
def execute_all_sql_files():
    ordered_sql_files = [
        'articles.sql', 'journals.sql', 'authors.sql', 'keywords.sql',
        'publication_types.sql', 'grants.sql', 'article_ids.sql',
        'journal_issues.sql', 'article_references.sql', 'affiliations.sql',
        'author_articles.sql', 'article_journals.sql'
    ]
    sql_files = [os.path.join(SQL_FOLDER, file) for file in ordered_sql_files]

    conn = connect_db()
    total_start_time = time.time()  # Start total timing
    try:
        # Disable triggers before inserting data
        execute_script(conn, DISABLE_TRIGGERS_FILE)

        # Execute each SQL file sequentially
        with conn:
            for filepath in sql_files:
                execute_sql_file(filepath, conn)

        # Enable triggers after data insertion
        execute_script(conn, ENABLE_TRIGGERS_FILE)
        print("All SQL files executed successfully.")
    finally:
        conn.close()
        total_duration = time.time() - total_start_time
        print(f"Total execution time: {total_duration:.2f} seconds.")


# Apply constraints
def apply_constraints():
    conn = connect_db()
    try:
        execute_script(conn, CONSTRAINTS_FILE)
    finally:
        conn.close()


if __name__ == "__main__":
    execute_all_sql_files()
    apply_constraints()
