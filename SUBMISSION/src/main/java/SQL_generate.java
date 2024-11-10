import com.alibaba.fastjson2.JSON;

import java.io.*;
import java.lang.ref.Reference;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SQL_generate {
    public static Article article;
    public static String line = null;
    public static boolean isAuthorLoad = false;
    public static void main(String[] args) {
        //!!!运行前手动将SQLgenerate中的sql文件全部删除
        String path = "pubmed24n.ndjson";
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
            SQL_dbConnect dbConnect = new SQL_dbConnect();
            dbConnect.getConnection();
            int count = 0;
            long time0 = System.currentTimeMillis();
            while ((line = br.readLine()) != null){
                // 这里可以使用JSON库如fastjson或Jackson来解析jsonContent
                article= JSON.parseObject(line,Article.class);
                //
                write_SQL("articles",genSQL_Articles());
                //

                if (article.getAuthor()!=null){
                    List<Author> authorList = article.getAuthor();
                    for (int i = 0; i < authorList.size(); i++) {
                        Author author = authorList.get(i);
                        //
                        write_SQL("author",genSQL_Authors(author));
                        //
                        write_SQL("author_Articles",genSQL_AuthorArticles(dbConnect,author));
                        if (author.getAffiliation()!=null){
                            String[] affiliations = author.getAffiliation();
                            for (int j = 0; j < affiliations.length; j++) {
                                int id = dbConnect.getAuthorId(author.getLast_name());
                                String affiliation = affiliations[j];
                                write_SQL("affiliations",genSQL_Affiliations(id,affiliation));
                            }
                        }
                    }
                }
                //
                if (article.getArticle_ids()!=null){
                    List<Ariticle_ids> ariticle_ids_List= article.getArticle_ids();
                    for (int i = 0; i < ariticle_ids_List.size(); i++) {
                        Ariticle_ids ariticle_id = ariticle_ids_List.get(i);
                        write_SQL("article_ids",genSQL_Article_ids(ariticle_id));
                    }
                }
                //
                write_SQL("journals",genSQL_Journals());
                //
                write_SQL("ArticleJournals",genSQL_ArticleJournals());
                //
                if (article.getReferences()!=null){
                    String[] referencesList = article.getReferences();
                    for (int i = 0; i < referencesList.length; i++) {
                        String reference = referencesList[i];
                        write_SQL("article_references",genSQL_Article_Ref(reference));
                    }
                }
                //
                if (article.getGrant()!=null){
                    List<Grant> grantList = article.getGrant();
                    for (int i = 0; i < grantList.size(); i++) {
                        Grant grant = grantList.get(i);
                        write_SQL("grants",genSQL_Grants(grant));
                    }
                }
                //
                write_SQL("journal_issues",genSQL_Journal_issues());
                //
                if (article.getKeywords()!=null){
                    String[] keywordList = article.getKeywords();
                    for (int i = 0; i < keywordList.length; i++) {
                        String keyword = keywordList[i];
                        write_SQL("keywords",genSQL_Keywords(keyword));
                    }
                }
                //
                if (article.getPublication_types()!=null){
                    List<Publication_types> publication_typesList = article.getPublication_types();
                    for (int i = 0; i < publication_typesList.size(); i++) {
                        Publication_types pt = publication_typesList.get(i);
                        write_SQL("publication_types",genSQL_publicationTypes(pt));
                    }
                }
                count++;
                if (count%10000==0){
                    long time1=System.currentTimeMillis();
                    System.out.println("已经生成了(行):"+count+"|耗时(ms)："+(time1-time0));
                    time0=time1;
                }
            }

        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void write_SQL(String sql_name,String sql) throws IOException {
        String path = "SQLgenerate/"+sql_name+".sql";
        File file = new File(path);
        //如果文件不存在，创建文件
        if (!file.exists()) {
            file.createNewFile();
        }
        //创建FileWriter对象
        FileWriter writer = new FileWriter(file,true);

        writer.write(sql+"\r\n");

        // 刷新和关闭writer
        writer.flush();
        writer.close();
    }
    public static String genSQL_ArticleJournals(){
        //insert into article_journals (article_id, journal_id) ;
        int article_id = article.getId();
        String journal_id = article.getJournal().getId();
        String sql = String.format("insert into article_journals (article_id, journal_id) values (%d,'%s');",article_id,journal_id);
        return sql;
    }
    public static String genSQL_AuthorArticles(SQL_dbConnect dbConnect,Author author){
        if (!isAuthorLoad){
            String path = "SQLgenerate/author.sql";
            try (BufferedReader br = new BufferedReader(new FileReader(path))){
                String line;
                List<String> sqlStatements = new ArrayList<>();
                while ((line = br.readLine()) != null){
                    sqlStatements.add(line);
                    if (sqlStatements.size()==1000){
                        dbConnect.executeSQLBatch(sqlStatements);
                        sqlStatements.clear();
                        //System.out.println("成功提交100条数据");
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            isAuthorLoad = true;
        }
        //insert into article_journals (article_id, journal_id) ;
        int author_id = dbConnect.getAuthorId(author.getLast_name());
        int article_id = article.getId();

        String sql = String.format("insert into author_articles (author_id, article_id) values (%d,%d);",author_id,article_id);
        dbConnect.closeConnection();
        return sql;
    }

    public static String genSQL_publicationTypes(Publication_types pt){
        //insert into publication_types (id, article_id, name)
        String id = pt.getId().replace("'","''");
        int article_id = article.getId();
        String name = pt.getName().replace("'","''");
        String sql = String.format("insert into publication_types (id, article_id, name) values ('%s',%d,'%s');",id,article_id,name);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Keywords(String keyword0){
        //insert into keywords (article_id, keyword)
        String keyword = keyword0.replace("'","''");
        int id = article.getId();
        String sql = String.format("insert into keywords (article_id, keyword) values (%d,'%s') ON CONFLICT DO NOTHING;",id,keyword);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Journal_issues(){
        JournalIssue journalIssue = article.getJournal().getJournal_issue();
        //insert into journal_issues (journal_id, volume, issue)
        String journal_id = article.getJournal().getId().replace("'","''");
        String volume="" ;
        if (journalIssue.getVolume()!=null){
            volume = journalIssue.getVolume().replace("'","''");
        }
        String issue="" ;
        if (journalIssue.getIssue()!=null){
            issue = journalIssue.getIssue().replace("'","''");
        }
        String sql = String.format("insert into journal_issues (journal_id, volume, issue) values ('%s','%s','%s') ON CONFLICT DO NOTHING;",journal_id,volume,issue);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Grants(Grant grant){
        //insert into grants (grant_id, acronym, country, agency, article_id)
        String id ;
        if (grant.getId()==null){
            id="";
        }else {
            id = grant.getId().replace("'","''");
        }
        String acronym="";
        if (grant.getAcronym()!=null){
            acronym = grant.getAcronym().replace("'","''");
        }
        String country = grant.getCountry().replace("'","''");
        String agency = grant.getAgency().replace("'","''");
        int idOfArticle = article.getId();
        String sql = String.format("insert into grants (grant_id, acronym, country, agency, article_id) values ('%s','%s','%s','%s',%d) ON CONFLICT DO NOTHING;",id,acronym,country,agency,idOfArticle);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Article_Ref(String reference0){
        //insert into article_references (article_id, reference_id)
        String reference = reference0.replace("'","''");
        int id = article.getId();
        int referenceInt = Integer.parseInt(reference.substring(1,reference.length()-1));
        String sql = String.format("insert into article_references (article_id, reference_id) values (%d,%d) ON CONFLICT DO NOTHING;",id,referenceInt);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Article_ids(Ariticle_ids ariticle_ids){
        //insert into article_ids (article_id, type, id)
        int article_id = article.getId();
        String ty = ariticle_ids.getTy().replace("'","''");
        String id = ariticle_ids.getId().replace("'","''");
        String sql = String.format("insert into article_ids (article_id, type, id) values (%d,'%s','%s') ON CONFLICT DO NOTHING;",article_id,ty,id);
        //System.out.println(sql);
        return sql;
    }

    public static String genSQL_Affiliations(int id,String affiliation0){

        String affiliation = affiliation0.replace("'","''");
        //insert into affiliations (affiliation)
        String sql = String.format("insert into affiliations (author_id,affiliation) values (%d,'%s') ON CONFLICT DO NOTHING;",id,affiliation);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Journals(){
        //insert into journals (id, country, title, issn)
        Journal journal =article.getJournal();
        String id = journal.getId().replace("'","''");
        String country = journal.getCountry().replace("'","''");
        String title = journal.getTitle().replace("'","''");
        String issn = journal.getIssn().replace("'","''");
        String sql = String.format("insert into journals (id, country, title, issn) values ('%s','%s','%s','%s') ON CONFLICT DO NOTHING;",id,country,title,issn);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Authors(Author author){
        //insert into authors (id, last_name, fore_name, initials, collective_name)
        String last_name = author.getLast_name().replace("'","''");
        String fore_name="" ;
        if (author.getFore_name()!=null){
            fore_name = author.getFore_name().replace("'","''");
        }
        String initials = "";
        if (author.getInitials()!=null){
            initials = author.getInitials().replace("'","''");
        }
        String collective_name = "false";
        if (author.getCollective_name()){
            collective_name = "true";
        }

        String sql = String.format("insert into authors (last_name, fore_name, initials, collective_name) values ('%s','%s','%s',%s) ON CONFLICT DO NOTHING;",last_name,fore_name,initials,collective_name);
        //System.out.println(sql);
        return sql;
    }
    public static String genSQL_Articles(){
        //Article
        int id = article.getId();
        String articleTitle0 = article.getTitle().replace("'","''");
        String articleTitle = articleTitle0.replace("'","''");//把原来文本里的‘替换成’‘，其中一个’为转义符，防止插入时报错
        String pubmodel = String.valueOf(article.getPub_model());

        JDate Jdate_created = article.getDate_created();
        String date_created_str = Jdate_created.getYear()+"-"+Jdate_created.getMonth()+"-"+Jdate_created.getDay();

        JDate Jdate_completed = article.getDate_completed();
        String date_completed_str = Jdate_completed.getYear()+"-"+Jdate_completed.getMonth()+"-"+Jdate_completed.getDay();

        String sql = String.format("insert into articles (id, title, pub_model, date_created, date_completed) values (%d,'%s','%s',Date'%s',Date'%s') ON CONFLICT DO NOTHING;",id,articleTitle,pubmodel,date_created_str,date_completed_str);
        return sql;
        /*String sql = String.format("insert into articles (id, title, pub_model, date_created, date_completed) " +
                "values (%d,'%s','%s',Date'%s',Date'%s')",id,articleTitle,pubmodel,date_created_str,date_completed_str);*/

    }
}
