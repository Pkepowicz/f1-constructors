import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;

public class Constructor {
    private String name;
    private ArrayList<Standing> standings;
    private ImageIcon logo;

    public String getName() {
        return name;
    }

    public ArrayList<Standing> getStandings() {
        return standings;
    }

    public ImageIcon getLogo() {
        return logo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStandings(ArrayList<Standing> standings) {
        this.standings = standings;
        Collections.sort(standings);
    }

    public void addStandings(Standing standing) {
        getStandings().add(standing);
        Collections.sort(standings);
    }

    public void addStandings(ArrayList<Standing> standing) {
        getStandings().addAll(standing);
        Collections.sort(standings);
    }

    public void setLogo(ImageIcon logo) {
        this.logo = logo;
    }
}
