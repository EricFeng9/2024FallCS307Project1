import java.sql.*;
import java.util.List;

public class SQL_dbConnectMySQL {
    private static Connection con = null;
    private static ResultSet resultSet;

    private static String host = "localhost";
    private static String dbname = "project_dbmysql";
    private static String user = "root"; // MySQL的默认用户通常是root
    private static String pwd = "Lekge66811616"; // 请确保这是你的MySQL密码
    private static String port = "3306"; // MySQL的默认端口是3306

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL的JDBC驱动类名

        } catch (ClassNotFoundException e) {
            System.err.println("Cannot find the MySQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbname + "?useSSL=false&serverTimezone=UTC";
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return con;
    }

    public void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void executeSQLBatch(List<String> sqlStatements) throws SQLException {
        con.setAutoCommit(false);
        Statement statement = con.createStatement();
        for (String sql : sqlStatements) {
            sql = sql.trim(); // 去除字符串开头和结尾的空白字符
            if (!sql.isEmpty()) { // Skip empty statements
                statement.addBatch(sql);
            }
        }
        statement.executeBatch();
        con.commit(); // 提交事务
        statement.clearBatch();
    }

    public int getAuthorId(String lastName) {
        String lastName1 = lastName.replace("'", "''");
        Statement stmt = null;
        int id = -1;
        String sql = "SELECT id FROM authors WHERE last_name ='" + lastName1 + "'";
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                id = rs.getInt("id");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;
    }
    //____________________________________________________

}

