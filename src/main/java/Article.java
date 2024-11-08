import com.alibaba.fastjson2.JSON;

import java.util.List;

public class Article {
    private int id;
    private String title;
    private Pub_model pub_model;
    private JDate date_created;
    private JDate date_completed;
    private Journal journal;
    private List<Author> author;
    private List<Publication_types> publication_types;
    private List<Grant> grant;
    private List<Ariticle_ids> article_ids;

    private String[] references;//存的时候再转成int
    private String[] keywords;

    // 标准 getters & setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Pub_model getPub_model() {
        return pub_model;
    }

    public void setPub_model(Pub_model pub_model) {
        this.pub_model = pub_model;
    }

    public JDate getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created_str) {
        this.date_created = JSON.parseObject(date_created_str,JDate.class);
    }

    public JDate getDate_completed() {
        return date_completed;
    }

    public void setDate_completed(String date_completed_str) {
        this.date_completed = JSON.parseObject(date_completed_str,JDate.class);
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(String journal_str) {
        this.journal = JSON.parseObject(journal_str,Journal.class);
    }

    public List<Author> getAuthor() {
        return author;
    }

    public void setAuthor(String author_str) {
        this.author = JSON.parseArray(author_str,Author.class);
    }

    public List<Publication_types> getPublication_types() {
        return publication_types;
    }

    public void setPublication_types(String publication_types_str) {
        this.publication_types = JSON.parseArray(publication_types_str,Publication_types.class);
    }

    public List<Grant> getGrant() {
        return grant;
    }

    public void setGrant(String grant_str) {
        this.grant =JSON.parseArray(grant_str,Grant.class);
    }

    public List<Ariticle_ids> getArticle_ids() {
        return article_ids;
    }

    public void setArticle_ids(String article_ids_str) {
        this.article_ids = JSON.parseArray(article_ids_str,Ariticle_ids.class);
    }

    public String[] getReferences() {
        return references;
    }

    public void setReferences(String references_str){
        String references_str1 = references_str.substring(1,references_str.length()-1);
        this.references = references_str1.split(",");
        //references[]这里面的元素是带双引号的  [""1139315"",""803854"",""806808""]
        //eg references[0] print 出来是"1139315"
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords_str0) {
        String keywords_str1 = keywords_str0.replace('"',' ');
        String keywords_str2 = keywords_str1.substring(1,keywords_str1.length()-1-1);
        this.keywords = keywords_str2.split(",");
    }
}

enum Pub_model {
    Print,Print_Electronic,Electronic,Electronic_Print,ElectroniceCollection
}

