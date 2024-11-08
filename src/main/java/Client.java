
public class Client {

    public static void main(String[] args) {
        try {
            DataManipulation dm = new DataFactory().createDataManipulation(args[0]);
            dm.addOneMovie("80001;抓娃娃;cn;2024;133");
            System.out.println(dm.allContinentNames());
            System.out.println(dm.continentsWithCountryCount());
            //complete the function:findMovieById
            //System.out.println(dm.findMovieById(10));
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}

