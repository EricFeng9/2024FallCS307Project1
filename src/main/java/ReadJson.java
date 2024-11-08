

import com.alibaba.fastjson2.JSON;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ReadJson {
    public static String line = null;
    public static void main(String[] args) {
        long stime= System.currentTimeMillis();
        String path = "pubmed24n.ndjson";
        DataManipulation dm = new DataFactory().createDataManipulation(args[0]);
        dm.getConnection();
        /*String temp = "{\n" +
                "  \"id\": 12265,\n" +
                "  \"title\": \"Size analysis of metered suspension pressurized aerosols with the Quantimet 720.\",\n" +
                "  \"pub_model\": \"Print\",\n" +
                "  \"date_created\": {\n" +
                "    \"year\": 2019,\n" +
                "    \"month\": 7,\n" +
                "    \"day\": 10\n" +
                "  },\n" +
                "  \"date_completed\": {\n" +
                "    \"year\": 1977,\n" +
                "    \"month\": 2,\n" +
                "    \"day\": 24\n" +
                "  },\n" +
                "  \"journal\": {\n" +
                "    \"id\": \"0376363\",\n" +
                "    \"country\": \"England\",\n" +
                "    \"issn\": \"0022-3573\",\n" +
                "    \"title\": \"The Journal of pharmacy and pharmacology\",\n" +
                "    \"journal_issue\": {\n" +
                "      \"volume\": \"28\",\n" +
                "      \"issue\": \"12\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"author\": [\n" +
                "    {\n" +
                "      \"last_name\": \"Hallworth\",\n" +
                "      \"fore_name\": \"G W\",\n" +
                "      \"initials\": \"GW\",\n" +
                "      \"affiliation\": [\n" +
                "        \"Pharmaceutical Research Department, Allen and Hanburys Research Ltd., Ware, Herts, U.K.\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"last_name\": \"Hamilton\",\n" +
                "      \"fore_name\": \"R R\",\n" +
                "      \"initials\": \"RR\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"publication_types\": [\n" +
                "    {\n" +
                "      \"id\": \"D016428\",\n" +
                "      \"name\": \"Journal Article\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"article_ids\": [\n" +
                "    {\n" +
                "      \"ty\": \"pubmed\",\n" +
                "      \"id\": \"12265\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"ty\": \"doi\",\n" +
                "      \"id\": \"10.1111/j.2042-7158.1976.tb04087.x\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        Article newArticle = JSON.parseObject(temp,Article.class);
        Thread_add_Atc_Jnl_JnlIs thread_add_Atc_Jnl_JnlIs =  new Thread_add_Atc_Jnl_JnlIs(dm,newArticle,"thread1",path);
        thread_add_Atc_Jnl_JnlIs.start();

        Thread_add_Article_ids thread_add_article_ids = new Thread_add_Article_ids(dm,newArticle,"thread2",path);
        thread_add_article_ids.start();

        Thread_add_Authors_and_Affiliation thread_add_authors_and_affiliation = new Thread_add_Authors_and_Affiliation(dm,newArticle,"thread3",path);
        thread_add_authors_and_affiliation.start();

        Thread_add_References thread_add_references = new Thread_add_References(dm,newArticle,"thread4",path);
        thread_add_references.start();

        Thread_add_Grants thread_add_grants = new Thread_add_Grants(dm,newArticle,"thread5",path);
        thread_add_grants.start();

        Thread_addKeywords thread_addKeywords = new Thread_addKeywords(dm,newArticle,"thread6",path);
        thread_addKeywords.start();

        Thread_addPublication_types thread_addPublication_types = new Thread_addPublication_types(dm,newArticle,"thread6",path);
        thread_addPublication_types.start();

        System.out.println(dm.getAffiliationId("Pharmaceutical Research Department"));
        System.out.println(dm.getAuthorId("Hallworth"));*/


        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            int record = 0;
            long time0 = System.currentTimeMillis();
            while ((line = br.readLine()) != null) {
                // 这里可以使用JSON库如fastjson或Jackson来解析jsonContent
                Article newArticle= JSON.parseObject(line,Article.class);
                List <Author> authors = newArticle.getAuthor();
                Thread_add_Atc_Jnl_JnlIs thread_add_Atc_Jnl_JnlIs =  new Thread_add_Atc_Jnl_JnlIs(dm,newArticle,"thread1",path);
                thread_add_Atc_Jnl_JnlIs.start();

                Thread_add_Article_ids thread_add_article_ids = new Thread_add_Article_ids(dm,newArticle,"thread2",path);
                thread_add_article_ids.start();

                Thread_add_Authors_and_Affiliation thread_add_authors_and_affiliation = new Thread_add_Authors_and_Affiliation(dm,newArticle,"thread3",path);
                thread_add_authors_and_affiliation.start();

                Thread_add_References thread_add_references = new Thread_add_References(dm,newArticle,"thread4",path);
                thread_add_references.start();

                Thread_add_Grants thread_add_grants = new Thread_add_Grants(dm,newArticle,"thread5",path);
                thread_add_grants.start();

                Thread_addKeywords thread_addKeywords = new Thread_addKeywords(dm,newArticle,"thread6",path);
                thread_addKeywords.start();

                Thread_addPublication_types thread_addPublication_types = new Thread_addPublication_types(dm,newArticle,"thread6",path);
                thread_addPublication_types.start();

                record++;

                if (record%1000==0){
                    long time1= System.currentTimeMillis();
                    System.out.println("current record is: "+record+"|"+"Time cost is(ms):"+(time1-time0));
                    time0 = time1;
                }

            }

            long ftime = System.currentTimeMillis();
            System.out.println("finished"+",record is:"+record+"\n"+"Time Cost:"+((ftime-stime)/1000));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //dm.closeConnection();
        //多线程导入
        //Thread_add_Article_ids thread_add_article_ids = new Thread_add_Article_ids("thread2",path);
        //thread_add_article_ids.start();
        //todo 按上面的格式补充完整即可
    }
}
class Thread_add_Atc_Jnl_JnlIs extends Thread{
    private String threadName;
    private DataManipulation dm;
    private Article article;
    private Thread t ;
    private String Path;
    Thread_add_Atc_Jnl_JnlIs(DataManipulation dm,Article article,String threadName,String Path){
        this.dm = dm;
        this.article = article;
        this.Path = Path;
        this.threadName = threadName;
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
        long time0 = System.currentTimeMillis();

        String line;
        add_Article_Journal_JournalIssue(article,dm);

        long time1 = System.currentTimeMillis();
        //System.out.println(threadName+"用时(ms):"+(time1-time0));
    }

    public void  add_Article_Journal_JournalIssue(Article article,DataManipulation dm){
        //Article
        int id = article.getId();
        String articleTitle = article.getTitle();
        String pubmodel = String.valueOf(article.getPub_model());

        JDate Jdate_created = article.getDate_created();
        String date_created_str = Jdate_created.getYear()+"-"+Jdate_created.getMonth()+"-"+Jdate_created.getDay();
        Date date_created_sql = transferToSqlDate(date_created_str);

        JDate Jdate_completed = article.getDate_completed();
        String date_completed_str = Jdate_completed.getYear()+"-"+Jdate_completed.getMonth()+"-"+Jdate_completed.getDay();
        Date date_completed_sql = transferToSqlDate(date_completed_str);
        String str_Article = id+";"+articleTitle+";"+pubmodel;
        //Journals
        Journal journal = article.getJournal();
        String journal_id = journal.getId();
        //insert into journals (id, country, title, issn)
        String country = journal.getCountry();
        String title = journal.getTitle();
        String issn = journal.getIssn();
        String journal_str = journal_id+";"+country+";"+title+";"+issn+" ";

        //JournalIssue
        //(journal_id, volume, issue)
        JournalIssue journalIssue = journal.getJournal_issue();
        String volume = journalIssue.getVolume();
        String issue = journalIssue.getIssue();
        String journalIssue_str = journal_id+";"+volume+";"+issue;
        dm.addAll(str_Article,date_created_sql,date_completed_sql,journal_str,journalIssue_str);

    }
    public Date transferToSqlDate(String date){
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
class  Thread_add_Article_ids extends Thread{
    private String threadName;
    private DataManipulation dm;
    private Article article;
    private Thread t ;
    private String Path;
    Thread_add_Article_ids(DataManipulation dm,Article article,String threadName,String Path){
        this.dm = dm;
        this.article = article;
        this.Path = Path;
        this.threadName = threadName;
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
        //dm.getConnection();
        String line;
        //System.out.println("Running "+ threadName);
        add_Article_ids(article,dm);
        //System.out.println("Thread " +  threadName + " exiting.");
        //dm.closeConnection();
    }
    public static void  add_Article_ids(Article article,DataManipulation dm){
        int idOfArticle = article.getId();
        List<Ariticle_ids> ariticle_ids = article.getArticle_ids();
        for (int i = 0; i < ariticle_ids.size(); i++) {
            Ariticle_ids temp_article_ids = ariticle_ids.get(i);
            String type = temp_article_ids.getTy();
            String id = temp_article_ids.getId();
            String str = idOfArticle +";"+type+";"+id;
            dm.addArticleIds(str);
        }
    }
}
class  Thread_add_Authors_and_Affiliation extends Thread{

    private String threadName;
    private DataManipulation dm;
    private Article article;
    private Thread t ;
    private String Path;
    Thread_add_Authors_and_Affiliation(DataManipulation dm,Article article,String threadName,String Path){
        this.dm = dm;
        this.article = article;
        this.Path = Path;
        this.threadName = threadName;
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
        //dm.getConnection();
        String line;
        //System.out.println("Running "+ threadName);
        add_Authors_and_Affiliation(article,dm);
        //System.out.println("Thread " +  threadName + " exiting.");
        //dm.closeConnection();
    }

    public static void add_Authors_and_Affiliation(Article article,DataManipulation dm){
        int idOfArticle = article.getId();
        List<Author> authorList = article.getAuthor();

        if (authorList!=null){
            for (int i = 0; i < authorList.size(); i++) {
                //add author
                Author author = authorList.get(i);
                String last_name = author.getLast_name();
                String fore_name = author.getFore_name();
                String initials = author.getInitials();
                Boolean collectiveName = author.getCollective_name();
                String str = last_name+";"+fore_name+";"+initials;
                dm.addAuthor(str,collectiveName);
                //add affiliation
                String[] affiliationList = author.getAffiliation();
                if (affiliationList!=null){
                    for (int j = 0; j < affiliationList.length; j++) {
                        String affiliation = affiliationList[j];
                        dm.addAffiliation(affiliation);
                        //add author_affiliations
                        dm.add_author_affiliations(last_name,affiliation);
                    }
                }
            }
        }
    }
}


class  Thread_add_References extends Thread{

    private String threadName;
    private DataManipulation dm;
    private Article article;
    private Thread t ;
    private String Path;
    //add_References(newArticle,dm)
    Thread_add_References(DataManipulation dm,Article article,String threadName,String Path){
        this.dm = dm;
        this.article = article;
        this.Path = Path;
        this.threadName = threadName;
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
        //dm.getConnection();
        String line;
        //System.out.println("Running "+ threadName);
        add_References(article,dm);
        //System.out.println("Thread " +  threadName + " exiting.");
        //dm.closeConnection();
    }
    public static int author_id = 1;//author 的自增id
    public static void add_References(Article article,DataManipulation dm){
        String[] references_str_list = article.getReferences();
        int idOfArticle = article.getId();
        if (references_str_list!=null){
            for ( int i = 0; i < references_str_list.length; i++) {
                //references_str_list[]中的每一个元素打印出来的是带双引号的字符串，eg "1139315"
                String references_str0 = references_str_list[i];
                String references_str1 = references_str0.substring(1,references_str0.length()-1);
                int reference_id = Integer.parseInt(references_str1);
                //system.out.println(references_str1);
                dm.addReferences(idOfArticle+";"+reference_id);
            }
        }
    }
}

//add_Grants(newArticle,dm);
class  Thread_add_Grants extends Thread{

    private String threadName;
    private DataManipulation dm;
    private Article article;
    private Thread t ;
    private String Path;
    Thread_add_Grants(DataManipulation dm,Article article,String threadName,String Path){
        this.dm = dm;
        this.article = article;
        this.Path = Path;
        this.threadName = threadName;
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
        //dm.getConnection();
        String line;
        //System.out.println("Running "+ threadName);
        add_Grants(article,dm);
        //System.out.println("Thread " +  threadName + " exiting.");
        //dm.closeConnection();
    }
    public static void  add_Grants(Article article,DataManipulation dm){
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
                String str = grant_id+";"+acronym+";"+country+";"+agency+";"+idOfArticle;
                dm.addGrants(str);
            }
        }
    }
}

//addKeywords(newArticle,dm);
class  Thread_addKeywords extends Thread{

    private String threadName;
    private DataManipulation dm;
    private Article article;
    private Thread t ;
    private String Path;
    Thread_addKeywords(DataManipulation dm,Article article,String threadName,String Path){
        this.dm = dm;
        this.article = article;
        this.Path = Path;
        this.threadName = threadName;
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
        //dm.getConnection();
        String line;
        //System.out.println("Running "+ threadName);
        addKeywords(article,dm);
        //System.out.println("Thread " +  threadName + " exiting.");
        //dm.closeConnection();
    }
    public static void addKeywords(Article article,DataManipulation dm){
        //insert into keywords (article_id, keyword)
        int idOfArticle = article.getId();
        String[] keywordList = article.getKeywords();
        if (keywordList!=null){
            for (int i = 0; i < keywordList.length; i++) {
                String keyword_str0 = keywordList[i];
                //String keyword_str1 = keyword_str0.substring(1,keyword_str0.length()-1-1);
                String str = idOfArticle+";"+keyword_str0;
                dm.addKeywords(str);
            }
        }

    }
}
//addPublication_types(newArticle,dm)
class  Thread_addPublication_types extends Thread{

    private String threadName;
    private DataManipulation dm;
    private Article article;
    private Thread t ;
    private String Path;
    Thread_addPublication_types(DataManipulation dm,Article article,String threadName,String Path){
        this.dm = dm;
        this.article = article;
        this.Path = Path;
        this.threadName = threadName;
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
        //dm.getConnection();
        String line;
        //System.out.println("Running "+ threadName);
        addPublication_types(article,dm);
        //System.out.println("Thread " +  threadName + " exiting.");
        //dm.closeConnection();
    }
    public static void addPublication_types(Article article,DataManipulation dm){
        //insert into publication_types (id, article_id, name)
        int idOfArticle = article.getId();
        List<Publication_types> publicationTypesList = article.getPublication_types();
        for (int i = 0; i < publicationTypesList.size(); i++) {
            Publication_types publicationTypes = publicationTypesList.get(i);
            String id = publicationTypes.getId();
            String name = publicationTypes.getName();
            String str = id+";"+idOfArticle+";"+name;
            dm.addPublication_Types(str);
        }
    }
}