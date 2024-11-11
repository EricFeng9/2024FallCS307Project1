import os

# 设置输入和输出文件路径
INPUT_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/sql_files/journals.sql'
JOURNALS_OUTPUT_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/sql_files/journals1.sql'
JOURNAL_ISSUES_OUTPUT_FILE = 'C:/Users/Lenovo/PycharmProjects/pythonProject2/pubmed24n/sql_files/journal_issues.sql'


def split_journal_statements(input_file, journals_output, journal_issues_output):
    # 打开输出文件
    with open(journals_output, 'w', encoding='utf-8') as journals_file, \
            open(journal_issues_output, 'w', encoding='utf-8') as issues_file:

        # 逐行读取输入文件
        with open(input_file, 'r', encoding='utf-8') as infile:
            for line in infile:
                if line.strip().startswith("INSERT INTO journals"):
                    # 写入 `journals.sql`
                    journals_file.write(line)
                elif line.strip().startswith("INSERT INTO journal_issues"):
                    # 写入 `journal_issues.sql`
                    issues_file.write(line)

    print(f"分离完成！`journals.sql` 和 `journal_issues.sql` 已生成。")


# 运行分离脚本
if __name__ == "__main__":
    split_journal_statements(INPUT_FILE, JOURNALS_OUTPUT_FILE, JOURNAL_ISSUES_OUTPUT_FILE)