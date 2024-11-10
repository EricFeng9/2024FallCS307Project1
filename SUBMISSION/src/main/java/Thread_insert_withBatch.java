import com.alibaba.fastjson2.JSON;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Thread_insert_withBatch {
    private static Connection con = null;
    private static String host = "localhost";
    private static String dbname = "postgres";
    private static String user = "postgres";
    private static String pwd = "Lekge66811616";
    private static String port = "5432";

    public static void main(String[] args) throws InterruptedException, SQLException {
        long stime = System.currentTimeMillis();
        // 需要执行的任务数量
        int numberOfTasks = 12;
        // 初始化 CountDownLatch
        CountDownLatch latch = new CountDownLatch(numberOfTasks);

        getConnection();

        Insert insertArticles = new Insert(latch,con,"Insert_Articles","insert into articles (id, title, pub_model, date_created, date_completed) values (?,?,?,?,?) ON CONFLICT DO NOTHING")   ;
        insertArticles.setPriority(1);
        insertArticles.start();
        //跑完跑keyword

        Insert insertJournals = new Insert(latch,con,"Insert_Journals","insert into journals (id, country, title, issn) values (?,?,?,?) ON CONFLICT DO NOTHING")   ;
        insertJournals.setPriority(2);
        insertJournals.start();

        Insert insertAuthors = new Insert(latch,con,"Insert_Authors","insert into authors (last_name, fore_name, initials, collective_name) values (?,?,?,?) ON CONFLICT DO NOTHING");
        insertAuthors.setPriority(2);
        insertAuthors.start();
        //跑完跑affiliation,AuthorArticles

        Insert insertArticleids = new Insert(latch,con,"Insert_Articleids","insert into article_ids (article_id, type, id) values (?,?,?) ON CONFLICT DO NOTHING");
        insertArticleids.start();

        Insert insertReferences = new Insert(latch,con,"Insert_References","insert into article_references (article_id, reference_id) values (?,?) ON CONFLICT DO NOTHING");
        insertReferences.start();

        Insert insertGrants = new Insert(latch,con,"Insert_Grants","insert into grants (grant_id, acronym, country, agency, article_id) values (?,?,?,?,?) ON CONFLICT DO NOTHING");
        insertGrants.start();

        Insert insertPublicationTy = new Insert(latch,con,"Insert_PublicationTy","insert into publication_types (id, article_id, name) values (?,?,?) ON CONFLICT DO NOTHING");
        insertPublicationTy.start();

        Insert insertArticleJournal = new Insert(latch,con,"Insert_ArticleJournal","insert into article_journals (article_id, journal_id) values (?,?) ON CONFLICT DO NOTHING");
        insertArticleJournal.start();

        Insert insertJournalIssues = new Insert(latch,con,"Insert_JournalIssues","insert into journal_issues (journal_id, volume, issue) values (?,?,?) ON CONFLICT DO NOTHING");
        insertJournalIssues.start();

        latch.await();
        /*-- 外键约束
        ALTER TABLE journal_issues ADD FOREIGN KEY (journal_id) REFERENCES journals(id);
        ALTER TABLE affiliations ADD FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE;
        ALTER TABLE keywords ADD FOREIGN KEY (article_id) REFERENCES articles(id);
        ALTER TABLE publication_types ADD FOREIGN KEY (article_id) REFERENCES articles(id);
        ALTER TABLE grants ADD FOREIGN KEY (article_id) REFERENCES articles(id);
        ALTER TABLE article_ids ADD FOREIGN KEY (article_id) REFERENCES articles(id);
        ALTER TABLE article_references ADD FOREIGN KEY (article_id) REFERENCES articles(id);
        ALTER TABLE article_journals ADD FOREIGN KEY (article_id) REFERENCES articles(id);
        ALTER TABLE article_journals ADD FOREIGN KEY (journal_id) REFERENCES journals(id);
        */
        List<String> addKey = new ArrayList<>();
        addKey.add("ALTER TABLE journal_issues ADD FOREIGN KEY (journal_id) REFERENCES journals(id);");
        addKey.add("ALTER TABLE affiliations ADD FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE;");
        addKey.add("ALTER TABLE keywords ADD FOREIGN KEY (article_id) REFERENCES articles(id);");
        addKey.add("ALTER TABLE publication_types ADD FOREIGN KEY (article_id) REFERENCES articles(id);");
        addKey.add("ALTER TABLE grants ADD FOREIGN KEY (article_id) REFERENCES articles(id);");
        addKey.add("ALTER TABLE article_ids ADD FOREIGN KEY (article_id) REFERENCES articles(id);");
        addKey.add("ALTER TABLE article_references ADD FOREIGN KEY (article_id) REFERENCES articles(id);");
        addKey.add("ALTER TABLE article_journals ADD FOREIGN KEY (article_id) REFERENCES articles(id);");
        addKey.add("ALTER TABLE article_journals ADD FOREIGN KEY (journal_id) REFERENCES journals(id);");//todo
        long ftime = System.currentTimeMillis();
        //executeSQLBatch(addKey);

        System.out.println("任务结束，耗时(ms):"+(ftime-stime));


    }//标记1

    public static void executeSQLBatch(List<String> sqlStatements) throws SQLException {
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
    public static void getConnection() {
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

    public static void closeConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
class Insert extends Thread {
    CountDownLatch latch = null;
    private Connection con = null;

    public static String line = null;

    private static int batchSize = 800;
    private static int batchCount = 0;

    public static Article article;
    private String threadName ;
    private String sql ;
    private Thread t;

    Insert(CountDownLatch latch,Connection con,String threadName,String sql) {
        this.latch = latch;
        this.con = con;
        this.threadName=threadName;
        this.sql = sql;
    }

    public void start() {
        //System.out.println("Starting "+threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }//在Thread类中， public void start() 使该线程开始执行；Java 虚拟机调用该线程的 run 方法。
    }

    public void run() {
        String path = "pubmed24n.ndjson";
        long stime = System.currentTimeMillis();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            int count = 0;
            long time0 = System.currentTimeMillis();
            //关闭自动提交模式
            con.setAutoCommit(false);
            PreparedStatement preparedStatement;
            try {
                preparedStatement = con.prepareStatement(sql);//这个必须放在循环外面
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            while ((line = br.readLine()) != null) {
                // 这里可以使用JSON库如fastjson或Jackson来解析jsonContent
                article = JSON.parseObject(line, Article.class);



                //选择生成对应sql的statement的方法
                if (threadName.equals("Insert_Articles")){
                    genStatement_Articles(preparedStatement);
                }else if (threadName.equals("Insert_Journals")){
                    genStatement_Journals(preparedStatement);
                }else if (threadName.equals("Insert_Authors")){
                    genStatement_Authors(preparedStatement);
                }else if (threadName.equals("Insert_Affiliations")){
                    genStatement_Affiliations(preparedStatement);
                }else if (threadName.equals("Insert_Articleids")){
                    genStatement_Articleids(preparedStatement);
                }else if (threadName.equals("Insert_References")){
                    genStatement_References(preparedStatement);
                }else if (threadName.equals("Insert_Grants")){
                    genStatement_Grants(preparedStatement);
                }else if (threadName.equals("Insert_JournalIssues")){
                    genStatement_JournalIssues(preparedStatement);
                }else if (threadName.equals("Insert_Keywords")){
                    genStatement_Keywords(preparedStatement);
                }else if (threadName.equals("Insert_PublicationTy")){
                    genStatement_PublicationTy(preparedStatement);
                }else if (threadName.equals("Insert_ArticleJournal")){
                    genStatement_ArticleJournal(preparedStatement);
                }else if (threadName.equals("Insert_AuthorArticles")){
                    genStatement_AuthorArticles(preparedStatement);
                }
                //标记2

                if (batchCount >= batchSize) {
                    try {
                        preparedStatement.executeBatch(); // 执行批处理
                        con.commit(); // 提交事务
                        batchCount = batchCount - batchSize;
                    } catch (SQLException e) {
                        try {
                            con.rollback(); // 发生异常时回滚
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                        e.printStackTrace();
                        //todo 检查报错
                    }
                }


                count++;
                if (count % 100000 == 0) {
                    long time1 = System.currentTimeMillis();
                    System.out.println(threadName+"已插入" + count + "行，耗时(ms/10w行):" + (time1 - time0));
                    time0 = time1;
                }
            }
            //读取结束最后提交一次，防止有剩余
            preparedStatement.executeBatch(); // 执行批处理
            con.commit(); // 提交事务
            if (threadName.equals("Insert_Authors")){
                Insert insertAffiliation = new Insert(latch,con,"Insert_Affiliations","insert into affiliations (author_id, affiliation) values (?,?) ON CONFLICT DO NOTHING");
                insertAffiliation.start();

                Insert insertAuthorArticles = new Insert(latch,con,"Insert_AuthorArticles","insert into author_articles (author_id, article_id) values (?,?) ON CONFLICT DO NOTHING");
                insertAuthorArticles.start();

            }else if (threadName.equals("Insert_Articles")){
                Insert insertKeywords = new Insert(latch,con,"Insert_Keywords","insert into keywords (article_id, keyword) values (?,?) ON CONFLICT DO NOTHING" );
                insertKeywords.start();
            }


        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        long ftime = System.currentTimeMillis();
        System.out.println(threadName + " finished,耗时(ms):" + (ftime - stime));
        latch.countDown();
    }

    //标记3
    public void genStatement_AuthorArticles(PreparedStatement preparedStatement){
        //insert into author_articles (author_id, article_id)
        List<Author> authorList = article.getAuthor();
        if (authorList!=null){
            for (int i = 0; i < authorList.size(); i++) {
                Author author = authorList.get(i);
                String str = getAuthorId(author.getLast_name())+"%%%"+article.getId();
                String Info[] = str.split("%%%");
                try {
                    preparedStatement.setInt(1, Integer.parseInt(Info[0]));
                    preparedStatement.setInt(2, Integer.parseInt(Info[1]));
                    //System.out.println(preparedStatement.toString());
                    preparedStatement.addBatch();
                    batchCount++;

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }



    }

    public void genStatement_ArticleJournal(PreparedStatement preparedStatement){
        //insert into article_journals (article_id, journal_id)
        String journal_id = article.getJournal().getId();
        int id = article.getId();
        String str = id+"%%%"+journal_id;
        String Info[] = str.split("%%%");
        try {
            preparedStatement.setInt(1, Integer.parseInt(Info[0]));
            preparedStatement.setString(2, Info[1]);
            //System.out.println(preparedStatement.toString());
            preparedStatement.addBatch();
            batchCount++;

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void genStatement_PublicationTy(PreparedStatement preparedStatement){
        int idOfArticle = article.getId();
        List<Publication_types> publicationTypesList = article.getPublication_types();
        for (int i = 0; i < publicationTypesList.size(); i++) {
            Publication_types publicationTypes = publicationTypesList.get(i);
            String id = publicationTypes.getId();
            String name = publicationTypes.getName();
            String str = id+"%%%"+idOfArticle+"%%%"+name;
            String Info[] = str.split("%%%");
            try {
                preparedStatement.setString(1, Info[0]);
                preparedStatement.setInt(2, Integer.parseInt(Info[1]));
                preparedStatement.setString(3, Info[2]);
                //System.out.println(preparedStatement.toString());
                preparedStatement.addBatch();
                batchCount++;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public void genStatement_Keywords(PreparedStatement preparedStatement){
        int idOfArticle = article.getId();
        String[] keywordList = article.getKeywords();
        if (keywordList!=null){
            for (int i = 0; i < keywordList.length; i++) {
                String keyword_str0 = keywordList[i];
                //String keyword_str1 = keyword_str0.substring(1,keyword_str0.length()-1-1);
                String str = idOfArticle+"%%%"+keyword_str0;
                String Info[] = str.split("%%%");
                try {
                    preparedStatement.setInt(1, Integer.parseInt(Info[0]));

                    preparedStatement.setString(2, Info[1]);

                    preparedStatement.addBatch();
                    batchCount++;
                    //System.out.println(preparedStatement.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void genStatement_JournalIssues(PreparedStatement preparedStatement){
        Journal journal = article.getJournal();
        String journal_id = journal.getId();
        JournalIssue journalIssue = journal.getJournal_issue();
        String volume = journalIssue.getVolume();
        String issue = journalIssue.getIssue();
        String str = journal_id+"%%%"+volume+"%%%"+issue;
        String Info[] = str.split("%%%");
        try {
            preparedStatement.setString(1, Info[0]);
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            //System.out.println(preparedStatement.toString());
            preparedStatement.addBatch();
            batchCount++;
        } catch (SQLException e) {
            e.printStackTrace();
            //重复插入会有报错，直接注释掉了
        }
    }
    public void genStatement_Grants(PreparedStatement preparedStatement){
        int idOfArticle = article.getId();
        List<Grant> grantList = article.getGrant();
        if (grantList!= null){
            for (int i = 0; i < grantList.size(); i++) {
                //(grant_id, acronym, country, agency, article_id)
                Grant grant = grantList.get(i);
                String grant_id = grant.getId();
                String acronym = grant.getAcronym();
                String country = grant.getCountry();
                String agency = grant.getAgency();
                String str = grant_id+"%%%"+acronym+"%%%"+country+"%%%"+agency+"%%%"+idOfArticle;
                String Info[] = str.split("%%%");
                try {
                    preparedStatement.setString(1, Info[0]);
                    preparedStatement.setString(2, Info[1]);
                    preparedStatement.setString(3, Info[2]);
                    preparedStatement.setString(4, Info[3]);
                    preparedStatement.setInt(5, Integer.parseInt(Info[4]));
                    //System.out.println(preparedStatement.toString());
                    preparedStatement.addBatch();
                    batchCount++;

                } catch (SQLException e) {
                    //e.printStackTrace();
                    //重复插入会有报错，直接注释掉了
                }
            }
        }

    }
    public void genStatement_References(PreparedStatement preparedStatement){
        //"insert into article_references (article_id, reference_id) values (?,?)";
        String[] references_str_list = article.getReferences();
        int idOfArticle = article.getId();
        if (references_str_list!=null){
            for ( int i = 0; i < references_str_list.length; i++) {
                //references_str_list[]中的每一个元素打印出来的是带双引号的字符串，eg "1139315"
                String references_str0 = references_str_list[i];
                String references_str1 = references_str0.substring(1,references_str0.length()-1);
                int reference_id = Integer.parseInt(references_str1);
                //system.out.println(references_str1);
                String str = idOfArticle+"%%%"+reference_id;
                String Info[] = str.split("%%%");
                try {
                    if (Info.length==2){
                        preparedStatement.setInt(1, Integer.parseInt(Info[0]));
                        preparedStatement.setInt(2, Integer.parseInt(Info[1]));
                        //System.out.println(preparedStatement.toString());
                        preparedStatement.addBatch();
                        batchCount++;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    public void genStatement_Articleids(PreparedStatement preparedStatement){
        //"insert into article_ids (article_id, type, id) values (?,?,?)"
        int idOfArticle = article.getId();
        List<Ariticle_ids> ariticle_ids = article.getArticle_ids();
        if (ariticle_ids!=null){
            for (int i = 0; i < ariticle_ids.size(); i++) {
                Ariticle_ids temp_article_ids = ariticle_ids.get(i);
                String type = temp_article_ids.getTy();
                String id = temp_article_ids.getId();
                String str = idOfArticle +"%%%"+type+"%%%"+id;
                String Info[] = str.split("%%%");
                try {
                    preparedStatement.setInt(1, Integer.parseInt(Info[0]));
                    preparedStatement.setString(2, Info[1]);
                    preparedStatement.setString(3, Info[2]);
                    //System.out.println(preparedStatement.toString());
                    preparedStatement.addBatch();
                    batchCount++;
                } catch (SQLException e) {
                    //e.printStackTrace(); //todo 重复性约束？
                }
            }
        }


    }
    public void genStatement_Affiliations(PreparedStatement preparedStatement){
        if (article.getAuthor()!=null){
            List<Author> authorList= article.getAuthor();
            for (int i = 0; i < authorList.size(); i++){
                Author author = authorList.get(i);
                String[] affiliations =  author.getAffiliation();
                if (affiliations!=null){
                    for (int j = 0; j < affiliations.length; j++) {
                        String affiliation = affiliations[j];
                        //insert into affiliations (author_id, affiliation) values (?,?)
                        //System.out.println(author.getLast_name() +" | id:"+getAuthorId(author.getLast_name()));
                        String str = getAuthorId(author.getLast_name())+"%%%"+affiliation+" ";
                        String Info[] = str.split("%%%");
                        try {
                            preparedStatement.setInt(1, Integer.parseInt(Info[0]));
                            preparedStatement.setString(2, Info[1]);
                            //System.out.println(preparedStatement.toString());
                            preparedStatement.addBatch();
                            batchCount++;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

        }
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


    public void genStatement_Authors(PreparedStatement preparedStatement){
        //add author
        if (article.getAuthor()!=null){
            List<Author> authorList= article.getAuthor();
            if (authorList!=null){
                for (int i = 0; i < authorList.size(); i++) {
                    Author author = authorList.get(i);
                    String last_name = author.getLast_name();
                    String fore_name = author.getFore_name();
                    String initials = author.getInitials();
                    Boolean collectiveName = author.getCollective_name();
                    String str = last_name+"%%%"+fore_name+"%%%"+initials;
                    String Info[] = str.split("%%%");
                    try {
                        preparedStatement.setString(1, Info[0]);
                        preparedStatement.setString(2, Info[1]);
                        preparedStatement.setString(3, Info[2]);
                        preparedStatement.setBoolean(4, collectiveName);
                        //System.out.println(preparedStatement.toString());
                        preparedStatement.addBatch();
                        batchCount++;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        }

    }
    public void genStatement_Journals(PreparedStatement preparedStatement){
        //Journals
        Journal journal = article.getJournal();
        String journal_id = journal.getId();
        //insert into journals (id, country, title, issn)
        String country = journal.getCountry();
        String title = journal.getTitle();
        String issn = journal.getIssn();
        String journal_str = journal_id+"%%%"+country+"%%%"+title+"%%%"+issn+" ";
        /*String sql = "insert into journals (id, country, title, issn) " +
                "values (?,?,?,?)";*/
        String Info[] = journal_str.split("%%%");
        try {
            preparedStatement.setString(1, Info[0]);
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            preparedStatement.setString(4, Info[3]);
            //System.out.println(preparedStatement.toString());
            preparedStatement.addBatch();
            batchCount++;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void genStatement_Articles(PreparedStatement preparedStatement) {
        //Article
        int id = article.getId();
        String articleTitle0 = article.getTitle();
        String articleTitle = articleTitle0.replace("'","''");//把原来文本里的‘替换成’‘，其中一个’为转义符，防止插入时报错
        String pubmodel = String.valueOf(article.getPub_model());

        JDate Jdate_created = article.getDate_created();
        String date_created_str = Jdate_created.getYear() + "-" + Jdate_created.getMonth() + "-" + Jdate_created.getDay();
        Date date_created_sql = transferToSqlDate(date_created_str);

        JDate Jdate_completed = article.getDate_completed();
        String date_completed_str = Jdate_completed.getYear() + "-" + Jdate_completed.getMonth() + "-" + Jdate_completed.getDay();
        Date date_completed_sql = transferToSqlDate(date_completed_str);

        /*String sql = String.format("insert into articles (id, title, pub_model, date_created, date_completed) " +
                "values (%d,'%s','%s',Date'%s',Date'%s')",id,articleTitle,pubmodel,date_created_str,date_completed_str);*/

        String str = id + "%%%" + articleTitle + "%%%" + pubmodel;
        //System.out.println(sql);
        String Info[] = str.split("%%%");
        try {
            preparedStatement.setInt(1, Integer.parseInt(Info[0]));
            preparedStatement.setString(2, Info[1]);
            preparedStatement.setString(3, Info[2]);
            preparedStatement.setDate(4, date_created_sql);
            preparedStatement.setDate(5, date_completed_sql);
            preparedStatement.addBatch();
            batchCount++;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Date transferToSqlDate(String date) {
        // 使用 SimpleDateFormat 将字符串解析为 java.util.Date 对象
        try {
            // 使用 SimpleDateFormat 将字符串解析为 java.util.Date 对象
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = sdf.parse(date);
            // 将 java.util.Date 转换为 java.sql.Date
            Date sqlDate = new Date(utilDate.getTime());
            return sqlDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}