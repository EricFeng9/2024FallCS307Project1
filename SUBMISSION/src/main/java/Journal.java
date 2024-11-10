import com.alibaba.fastjson2.JSON;

public class Journal {
    private String id;
    private String country;
    private String issn;
    private String title;
    private JournalIssue  journal_issue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JournalIssue getJournal_issue() {
        return journal_issue;
    }

    public void setJournal_issue(String journal_issue_str) {
        this.journal_issue =  JSON.parseObject(journal_issue_str,JournalIssue.class);
    }
}
