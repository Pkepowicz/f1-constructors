import javax.swing.*;
import org.apache.logging.log4j.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jsoup.Connection.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GUI {
    private static final Logger log = LogManager.getRootLogger();

    private final static int defaultWidth = 100;
    private final static int defaultHeight = 100;

    private final ArrayList<Constructor> constructorCache = new ArrayList<Constructor>();

    private static final String[] constructors = {"Red Bull", "Ferrari", "Mercedes", "Alpine", "Mclaren", "Alfa Romeo",
            "Aston Martin", "Haas", "Alphatauri", "Williams"};

    public static void createAndShowGUI() {
        log.info("CREATING GUI...");
        Thread.currentThread().setName("Thread: GUI");

        JFrame frame = new JFrame("F1 Constructors");
        final JPanel control = new JPanel(new GridLayout());
        final JComboBox<String> jlist1 = new JComboBox<>(constructors);
        final JLabel iconContainer1 = new JLabel();
        final JComboBox<String> jlist2 = new JComboBox<>(constructors);
        final ImageIcon logo2 = new ImageIcon("rb.jpg");
        final JLabel iconContainer2 = new JLabel();
        final JLabel textArea = new JLabel("Compare constructors", SwingConstants.CENTER);
        { // temp graph
            jlist2.setSelectedIndex(6);
            DefaultCategoryDataset data = SetNewData(
                    GetConstructorData(constructors[jlist1.getSelectedIndex()]),
                    GetConstructorData(constructors[jlist2.getSelectedIndex()])
            );
            final JFreeChart chart = ChartFactory.createLineChart(
                    "Amount of points earned in season", "Year", "Points",
                    data, PlotOrientation.VERTICAL, true, true, false
            );
            final ChartPanel chartPanel = new ChartPanel(chart);
            frame.add(chartPanel, BorderLayout.SOUTH);
        }
        iconContainer1.setPreferredSize(new Dimension(defaultWidth, defaultHeight));

        iconContainer1.setIcon(ScaleImage(logo2));
        control.add(iconContainer1);
        control.add(jlist1);
        control.add(jlist2);
        iconContainer2.setIcon(ScaleImage(logo2));
        control.add(iconContainer2);

        textArea.setFont(new Font("Courier", Font.BOLD, 70));

        frame.add(textArea, BorderLayout.NORTH);
        frame.add(control, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        log.info("CREATING SUCCESSFUL!");
    }

    private static ImageIcon ScaleImage(ImageIcon iconToScale) {
        float mod = Math.max(iconToScale.getIconHeight() / (float) defaultHeight, iconToScale.getIconWidth() / (float) defaultWidth);
        int newHeight = (int) (0.4999 + iconToScale.getIconHeight() / mod);
        int newWidth = (int) (0.4999 + iconToScale.getIconWidth() / mod);
        ImageIcon image = new ImageIcon(iconToScale.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH));
        return image;
    }

    private static Constructor GetConstructorData(String name) {
        log.debug("Start downloading for: " + name);
        String standingPrefix = "https://www.formula1.com/en/results.html/";
        String imgPrefix = "https://www.formula1.com/content/dam/fom-website/teams/2023/";
        String imgSuffix = "-logo.png.transform/2col/image.png";
        Constructor constructor = new Constructor();
        constructor.setName(name);
        ArrayList<Standing> tmpStand = new ArrayList<Standing>();

        for (int i = 2014; i <= 2022; i++) {    // Downloading standings for last x years
            Document r = null;
            try {
                log.info("Downloading standing for: " + name);
                r = Jsoup.connect(standingPrefix + Integer.toString(i) + "/team.html").get();
                Element tmp = r.select("table[class=resultsarchive-table]").first();
                for (Element e : tmp.select("tr")) {
                    if (!e.select("td:eq(2)").text().startsWith(name)) {
                        continue;
                    }
                    Standing standing = new Standing(
                            i,
                            Float.parseFloat(e.select("td:eq(3)").text()),
                            Integer.parseInt(e.select("td:eq(1)").text())
                    );
                    tmpStand.add(standing);
                    log.debug("Downloaded standing: " + standing);
                    // 1 - position
                    // 2 - full name
                    // 3 - points
                }
                log.info("Standing download successful!");
            } catch (Exception e) {
                log.error("Standing download error: " + e);
            }
        }
        constructor.setStandings(tmpStand);

        Document r = null;  // downlading team logo
        if (name.equalsIgnoreCase("red bull")) {
            name = "red-bull-racing";
        }
        try {
            log.info("Downloading image for team: " + name);
            ImageIcon logo = new ImageIcon(new URL(imgPrefix + name + imgSuffix));
            constructor.setLogo(logo);
            log.info("Image Download successful!");
        } catch (MalformedURLException e) {
            log.error("Image download error: " + e);
        }
        log.debug(constructor);
        return constructor;
    }

    private static DefaultCategoryDataset SetNewData(Constructor team1, Constructor team2) {
        log.info("Setting data");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        ArrayList<Standing> c1 = team1.getStandings();
        ArrayList<Standing> c2 = team2.getStandings();
        if(c2.size() > c1.size()) {
            Constructor tmp = team1;
            team1 = team2;
            team2 = tmp;
        }
        for(int i = 0; i < c1.size(); i++) {
            dataset.addValue(c1.get(i).score, team1.getName(), Integer.toString(c1.get(i).year));
            if(i + c2.size() >= c1.size()) {
                log.debug("actual data");
                dataset.addValue(c2.get(i - c1.size() + c2.size()).score, team2.getName(), Integer.toString(c2.get(i - c1.size() + c2.size()).year));
                continue;
            }
            log.debug("default data");
            dataset.addValue(0, team2.getName(), Integer.toString(c1.get(i).year));
        }
        log.info("Data set");
        return dataset;
    }
}