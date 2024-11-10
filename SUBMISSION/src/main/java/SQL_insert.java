import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SQL_insert {
    public static void main(String[] args) throws SQLException, InterruptedException {
        long stime = System.currentTimeMillis();
        SQL_dbConnect dbConnect = new SQL_dbConnect();
        dbConnect.getConnection();

        // 需要执行的任务数量
        int numberOfTasks = 12;
        // 初始化 CountDownLatch
        CountDownLatch latch = new CountDownLatch(numberOfTasks);

        Read_SQL readAuthor = new Read_SQL(latch,"ReadAuthor","author",dbConnect);
        readAuthor.start();

        Read_SQL readGrants = new Read_SQL(latch,"ReadGrants","grants",dbConnect);
        readGrants.start();

        Read_SQL readJournalIssues = new Read_SQL(latch,"ReadJournalIssues","journal_issues",dbConnect);
        readJournalIssues.start();

        Read_SQL readKeywords = new Read_SQL(latch,"ReadKeywords","keywords",dbConnect);
        readKeywords.start();

        Read_SQL readPublicationTypes = new Read_SQL(latch,"ReadPublicationTypes","publication_types",dbConnect);
        readPublicationTypes.start();

        Read_SQL readArticleReferences = new Read_SQL(latch,"ReadArticleReferences","article_references",dbConnect);
        readArticleReferences.start();

        Read_SQL readArticleIds = new Read_SQL(latch,"ReadArticleIds","article_ids",dbConnect);
        readArticleIds.start();

        Read_SQL readArticleJournals = new Read_SQL(latch,"ReadArticleJournals","ArticleJournals",dbConnect);
        readArticleJournals.start();


        Read_SQL readArticles = new Read_SQL(latch,"ReadArticles","articles",dbConnect);
        readArticles.start();
        Read_SQL readJournals = new Read_SQL(latch,"ReadJournals","journals",dbConnect);
        readJournals.start();

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
        //dbConnect.executeSQLBatch(addKey);
        System.out.println("所有任务完成,用时(ms):"+(ftime-stime));

    }
}

class Read_SQL extends Thread{
    CountDownLatch latch;
    long time0 = System.currentTimeMillis();
    private String threadName;
    private SQL_dbConnect dbConnect;
    private String fileName;
    private Thread t ;
    private String path;
    Read_SQL(CountDownLatch latch,String threadName,String fileName,SQL_dbConnect dbConnect){
        this.latch = latch;
        this.threadName = threadName;
        this.fileName = fileName;
        this.dbConnect = dbConnect;
        path = "SQLforTest/"+fileName+".sql";
        //System.out.println("Creating "+ threadName);
    }

    public void start(){
        //System.out.println("Starting "+threadName);
        if (t==null){
            t = new Thread(this,threadName);
            t.start();
        }//在Thread类中， public void start() 使该线程开始执行；Java 虚拟机调用该线程的 run 方法。
    }
    public void run(){
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            long time0 =System.currentTimeMillis();
            int count = 0;
            String line;
            List<String> sqlStatements = new ArrayList<>();
            while ((line = br.readLine()) != null){
                String line1 = line.replace("None","''");
                sqlStatements.add(line1);
                if (sqlStatements.size()==1000){
                    dbConnect.executeSQLBatch(sqlStatements);
                    sqlStatements.clear();
                    //System.out.println("成功提交1000条数据");
                }
                count++;
                if (count%100000==0){
                    long time1 = System.currentTimeMillis();
                    System.out.println(threadName+"当前已读取(行):"+count+" 用时(ms/10w行):"+(time1-time0));
                    time0 = time1;
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        latch.countDown();
        if (threadName.equals("ReadAuthor")){

            Read_SQL readAuthorArticles = new Read_SQL(latch,"ReadAuthorArticles","author_Articles",dbConnect);
            readAuthorArticles.start();

            Read_SQL readAffiliations = new Read_SQL(latch,"ReadAffiliations","affiliations",dbConnect);
            readAffiliations.start();

        }
        long time1 = System.currentTimeMillis();
        System.out.println(threadName+"用时(ms):"+(time1-time0));
    }
}