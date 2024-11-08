import java.sql.Date;

public interface DataManipulation {

    public int addOneMovie(String str);

    public String allContinentNames();
    public String continentsWithCountryCount();
    public String FullInformationOfMoviesRuntime(int min, int max);
    public String findMovieById(int id);

    public int addOneArticle(String str,Date date_created,Date date_completed);
    public int addArticleIds(String str);
    public int addReferences(String str);
    public int addAuthor(String str,Boolean isCollectiveName);
    public int addGrants(String str);
    public int addJournalIssue(String str);
    public int addJournals(String str);
    public int addKeywords(String str);
    public int addPublication_Types(String str);
    public int addAffiliation(String str);
}
