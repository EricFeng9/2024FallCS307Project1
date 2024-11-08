public class Author {

    private Boolean collective_name=false;
    //导入数据的时候把{"last_name":"Makar","fore_name":"A B","initials":"AB"}三个拼在一起
    private String last_name;
    private String fore_name;
    private String initials;
    private String[] affiliation;

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        if (!collective_name){
            //如果没有collective name
            this.last_name = last_name;
        }
    }

    public String getFore_name() {
        return fore_name;
    }

    public void setFore_name(String fore_name) {
        this.fore_name = fore_name;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public boolean getCollective_name() {
        return collective_name;
    }

    public void setCollective_name(String collectiveName_str) {
        if (!collectiveName_str.isEmpty()){
            collective_name = true;
            last_name = collectiveName_str;
        }
    }

    public String[] getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String  affiliation_str) {
        String affiliation_str1 = affiliation_str.substring(2,affiliation_str.length()-1-2);
        affiliation = affiliation_str1.split(",");
    }
}
