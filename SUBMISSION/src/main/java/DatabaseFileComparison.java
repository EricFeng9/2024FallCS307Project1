import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseFileComparison {
    private static String host = "localhost";
    private static String dbname = "project_db";
    private static String USER = "postgres";
    private static String PASSWORD = "Lekge66811616";
    private static String port = "5432";

    private static final String NDJSON_FILE = "pubmed24n.ndjson";
    private static final String DATA_FILE = "file_storage.txt";
    private static final int BATCH_SIZE = 10000;

    // PostgreSQL connection configuration
    private static final String DB_URL = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;

    public static void main(String[] args) {
        try {
            List<JSONObject> data = readNdjsonData();
            List<Article> Articles = readNdjsonDataArticle();
            System.out.println("File I/O Performance:");
            fileInsert(data);
            fileQuery();
            fileUpdate();
            fileDelete();

            System.out.println("\nDBMS Performance(postgreSQL):");
            dbInsert(Articles);
            dbQuery();
            dbUpdate();
            dbDelete();

            System.out.println("\nDBMS Performance(MySQL):");
            dbInsertMySQL(Articles);
            dbQueryMySQL();
            dbUpdateMySQL();
            dbDeleteMySQL();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 读取ndjson文件
    private static List<JSONObject> readNdjsonData() throws IOException {
        List<JSONObject> data = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(NDJSON_FILE))) {
            String line;
            int count = 0;
            while ((line = br.readLine())!= null && count < BATCH_SIZE) {
                // 使用FastJSON2解析JSON字符串为JSONObject
                data.add(JSON.parseObject(line));
                count++;
            }
        }
        return data;
    }
    private static List<Article> readNdjsonDataArticle() throws IOException {
        List<Article> articles = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(NDJSON_FILE))) {
            String line;
            int count = 0;
            while ((line = br.readLine())!= null && count < BATCH_SIZE) {
                // 使用FastJSON2解析JSON字符串为JSONObject
                articles.add(JSON.parseObject(line,Article.class));
                count++;
            }
        }
        return articles;
    }

    // 文件插入操作
    private static void fileInsert(List<JSONObject> data) throws IOException {
        long start = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (JSONObject entry : data) {
                // 将JSONObject转换为字符串写入文件
                writer.write(entry.toJSONString());
                writer.newLine();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("File Insert: " + (end - start) + " ms");
    }

    // 文件查询操作
    private static void fileQuery() throws IOException {
        long start = System.currentTimeMillis();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            int count = 0;
            while ((line = reader.readLine())!= null && count < BATCH_SIZE) {
                // 使用FastJSON2解析文件中的JSON字符串为JSONObject（这里可以根据实际需求处理解析后的对象）
                //用fileQuery找到前1w行为
                Article article = JSON.parseObject(line, Article.class);
                count++;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("File Query: " + (end - start) + " ms");
    }

    // 文件更新操作
    private static void fileUpdate() throws IOException {
        List<JSONObject> updatedData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = reader.readLine())!= null) {
                JSONObject obj = JSON.parseObject(line);
                // 更新JSONObject中的字段
                obj.put("updated", true);
                updatedData.add(obj);
            }
        }
        long start = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (JSONObject entry : updatedData) {
                writer.write(entry.toJSONString());
                writer.newLine();
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("File Update: " + (end - start) + " ms");
    }


    // 文件删除操作
    private static void fileDelete() {
        long start = System.currentTimeMillis();
        File file = new File(DATA_FILE);
        if (file.delete()) {
            long end = System.currentTimeMillis();
            System.out.println("File Delete: " + (end - start) + " ms");
        } else {
            System.out.println("Failed to delete file.");
        }
    }
    // 数据库插入操作
    private static void dbInsert(List<Article> articles) {
        long start = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "insert into journals (id, country, title, issn) VALUES (?,?,?,?) ON CONFLICT DO NOTHING";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (Article article : articles) {
                    String journal_id = article.getJournal().getId();
                    String country = article.getJournal().getCountry();
                    String title = article.getJournal().getTitle();
                    String issn = article.getJournal().getIssn();
                    stmt.setString(1, journal_id);
                    stmt.setString(2, country);
                    stmt.setString(3, title);
                    stmt.setString(4, issn);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("DB Insert: " + (end - start) + " ms");
    }

    // 数据库删除操作
    private static void dbDelete() {
        long start = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM journals WHERE id = '0000211'")) {
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("DB Delete: " + (end - start) + " ms");
    }

    // 数据库查询操作
    private static void dbQuery() {
        long start = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM journals")) {

            while (rs.next()) {
                rs.getString("id");
                rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("DB Query: " + (end - start) + " ms");
    }

    // 数据库更新操作
    private static void dbUpdate() {
        long start = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("UPDATE journals SET title = 'aaaaaa' WHERE id LIKE '0000211';")) {

            /*stmt.setString(1, "new_value");
            stmt.setString(2, "some_condition");
            stmt.executeUpdate();*/

        } catch (SQLException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("DB Update: " + (end - start) + " ms");
    }

    //以下为MYSQL测试
    // MySQL数据库插入操作
    private static void dbInsertMySQL(List<Article> articles) {
        long start = System.currentTimeMillis();
        try (Connection conn = SQL_dbConnectMySQL.getConnection()) {
            String sql = "INSERT INTO journals (id, country, title, issn) VALUES (?,?,?,?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                conn.setAutoCommit(false);
                for (Article article : articles) {
                    String journal_id = article.getJournal().getId();
                    String country = article.getJournal().getCountry();
                    String title = article.getJournal().getTitle();
                    String issn = article.getJournal().getIssn();
                    stmt.setString(1, journal_id);
                    stmt.setString(2, country);
                    stmt.setString(3, title);
                    stmt.setString(4, issn);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("DB Insert: " + (end - start) + " ms");
    }

    // 数据库删除操作
    private static void dbDeleteMySQL() {
        long start = System.currentTimeMillis();
        Connection conn = SQL_dbConnectMySQL.getConnection();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM journals WHERE id = '0000211'");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        System.out.println("DB Delete: " + (end - start) + " ms");
    }

    // 数据库查询操作
    private static void dbQueryMySQL() throws SQLException {
        long start = System.currentTimeMillis();
        Connection conn = SQL_dbConnectMySQL.getConnection();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM journals");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        while (rs.next()) {
                rs.getString("id");
                rs.getString("title");
        }

        long end = System.currentTimeMillis();
        System.out.println("DB Query: " + (end - start) + " ms");
    }

    // 数据库更新操作
    private static void dbUpdateMySQL() throws SQLException {
        long start = System.currentTimeMillis();
        Connection conn = SQL_dbConnectMySQL.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE journals SET title = 'aaaaaa' WHERE id = '0000211'");
        stmt.executeUpdate();
        long end = System.currentTimeMillis();
        System.out.println("DB Update: " + (end - start) + " ms");
    }

}