
import java.sql.*;

public class DatabaseManipulation implements DataManipulation {
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

    @Override
    public void addAll(String addOneArticle_str, Date date_created, Date date_completed, String addJournals_str, String JournalIssue_str) {
        //getConnection();
        int addOneArticle_result = addOneArticle(addOneArticle_str,date_created,date_completed);
        int addJournals_result = addJournals(addJournals_str);
        int addJournalIssue_result = addJournalIssue(addJournals_str);
        //closeConnection();
    }
    public int addJournalIssue(String str){
        int result = 0;
        String sql = "insert into journal_issues (journal_id, volume, issue) " +
                "values (?,?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, Info[0]);
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            //System.out.println(preparedStatement.toString());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            //e.printStackTrace();
            //重复插入会有报错，直接注释掉了
        }
        return result;
    }
    public int addJournals(String str){

        int result = 0;
        String sql = "insert into journals (id, country, title, issn) " +
                "values (?,?,?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, Info[0]);
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            preparedStatement.setString(4, Info[3]);
            //System.out.println(preparedStatement.toString());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            //e.printStackTrace();
            //重复插入会有报错，直接注释掉了
        }
        return result;
    }
    public int addOneArticle(String str, Date date_created, Date date_completed) {
        int result = 0;
        String sql = "insert into articles (id, title, pub_model, date_created, date_completed) " +
                "values (?,?,?,?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, Integer.parseInt(Info[0]));
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            preparedStatement.setDate(4, date_created);
            preparedStatement.setDate(5, date_completed);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
            //todo 检查报错
        }
        return result;
    }
    @Override
    public int addOneMovie(String str) {
        //getConnection();
        int result = 0;
        String sql = "insert into movies (movieid, title, country,year_released,runtime) " +
                "values (?,?,?,?,?)";
        String movieInfo[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, Integer.parseInt(movieInfo[0]));
            preparedStatement.setString(2, movieInfo[1]);
            preparedStatement.setString(3, movieInfo[2]);
            preparedStatement.setInt(4, Integer.parseInt(movieInfo[3]));
            preparedStatement.setInt(5, Integer.parseInt(movieInfo[4]));
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }
        return result;
    }
    @Override


    public int addArticleIds(String str) {
        //getConnection();
        int result = 0;
        String sql = "insert into article_ids (article_id, type, id) " +
                "values (?,?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, Integer.parseInt(Info[0]));
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            //e.printStackTrace(); //todo 重复性约束？
        }
        return result;
    }

    @Override
    public int addReferences(String str) {
        //getConnection();
        int result = 0;
        String sql = "insert into article_references (article_id, reference_id) " +
                "values (?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, Integer.parseInt(Info[0]));
            preparedStatement.setInt(2, Integer.parseInt(Info[1]));
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }
        return result;
    }

    @Override
    public int addAuthor(String str,Boolean isCollectiveName) {
        //getConnection();
        int result = 0;
        String sql = "insert into authors (last_name, fore_name, initials, collective_name) " +
                "values (?,?,?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, Info[0]);
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            preparedStatement.setBoolean(4, isCollectiveName);
            //System.out.println(preparedStatement.toString());

            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            //e.printStackTrace();
            //重复插入会有报错，直接注释掉了
        } finally {
            //closeConnection();
        }
        return result;
    }

    @Override
    public int addGrants(String str) {
        //getConnection();
        int result = 0;
        String sql = "insert into grants (grant_id, acronym, country, agency, article_id) " +
                "values (?,?,?,?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, Info[0]);
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            preparedStatement.setString(4, Info[3]);
            preparedStatement.setInt(5, Integer.parseInt(Info[4]));
            //System.out.println(preparedStatement.toString());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            //e.printStackTrace();
            //重复插入会有报错，直接注释掉了
        } finally {
            //closeConnection();
        }
        return result;
    }


    public int addKeywords(String str){
        //getConnection();
        int result = 0;
        String sql = "insert into keywords (article_id, keyword) " +
                "values (?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, Integer.parseInt(Info[0]));
            preparedStatement.setString(2, Info[1]);
            //System.out.println(preparedStatement.toString());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            //e.printStackTrace();
        } finally {
            //closeConnection();
        }
        return result;
    }
    public int addPublication_Types(String str){
        //getConnection();
        int result = 0;
        String sql = "insert into publication_types (id, article_id, name) " +
                "values (?,?,?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, Info[0]);
            preparedStatement.setInt(2, Integer.parseInt(Info[1]));
            preparedStatement.setString(3, Info[2]);
            //System.out.println(preparedStatement.toString());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }
        return result;
    }

    @Override
    public int addAffiliation(String str) {
        //getConnection();
        int result = 0;
        String sql = "insert into affiliations (affiliation) " +
                "values (?)";
        String Info[] = str.split(";");
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, Info[0]);
            //System.out.println(preparedStatement.toString());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    //insert into author_affiliations (author_id, affiliation_id)
    public int add_author_affiliations(String lastName,String affiliation) {
        //getConnection();
        int result = 0;
        String sql = "insert into author_affiliations (author_id, affiliation_id) " +
                "values (?,?)";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, getAuthorId(lastName));
            preparedStatement.setInt(2, getAffiliationId(affiliation));
            //System.out.println(preparedStatement.toString());
            result = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    public int getAuthorId(String lastName){
        Statement stmt = null;
        int id=-1;
        String sql = "select id from authors where last_name ='"+lastName+"'";
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
    public int getAffiliationId(String affiliation){
        Statement stmt = null;
        int id=-1;
        String sql = "select id from affiliations where affiliation = '"+affiliation+"'";
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
    @Override
    public String allContinentNames() {
        //getConnection();
        StringBuilder sb = new StringBuilder();
        String sql = "select continent from countries group by continent";
        try {
            Statement statement = con.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                sb.append(resultSet.getString("continent") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }

        return sb.toString();
    }

    @Override
    public String continentsWithCountryCount() {
        //getConnection();
        StringBuilder sb = new StringBuilder();
        String sql = "select continent, count(*) countryNumber from countries group by continent;";
        try {
            Statement statement = con.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                sb.append(resultSet.getString("continent") + "\t");
                sb.append(resultSet.getString("countryNumber"));
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }

        return sb.toString();
    }

    @Override
    public String FullInformationOfMoviesRuntime(int min, int max) {
        //getConnection();
        StringBuilder sb = new StringBuilder();
        String sql = "select m.title,c.country_name country,c.continent ,m.runtime " +
                "from movies m " +
                "join countries c on m.country=c.country_code " +
                "where m.runtime between ? and ? order by runtime;";
        try {
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setInt(1, min);
            preparedStatement.setInt(2, max);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                sb.append(resultSet.getString("runtime") + "\t");
                sb.append(String.format("%-18s", resultSet.getString("country")));
                sb.append(resultSet.getString("continent") + "\t");
                sb.append(resultSet.getString("title") + "\t");
                sb.append(System.lineSeparator());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }
        return sb.toString();
    }

    @Override
    public String findMovieById(int id) {

        return null;
    }



}
