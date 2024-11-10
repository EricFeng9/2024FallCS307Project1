import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class SQL_dbConnect {
    private Connection con = null;
    private ResultSet resultSet;

    private String host = "localhost";
    private String dbname = "project_db";
    private String user = "postgres";
    private String pwd = "Lekge66811616";
    private String port = "5432";


    public void getConnection() {
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


    public void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void executeSQLBatch(List<String> sqlStatements) throws SQLException {
        con.setAutoCommit(false);
        Statement statement = con.createStatement();
        for (String sql : sqlStatements) {
            sql = sql.trim();//去除字符串开头和结尾的空白字符
            if (!sql.isEmpty()) { // Skip empty statements
                statement.addBatch(sql);
            }
        }
        statement.executeBatch();
        con.commit(); // 提交事务
        statement.clearBatch();
    }

    public int getAuthorId(String lastName){
        String lastName1 = lastName.replace("'","''");
        Statement stmt = null;
        int id=-1;
        String sql = "select id from authors where last_name ='"+lastName1+"'";
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
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
