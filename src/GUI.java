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
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GUI {
    private static final Logger log = LogManager.getRootLogger();

    private final static int defaultWidth = 100;
    private final static int defaultHeight = 100;

    private static final String[] constructors = {"Red Bull", "Ferrari", "Mercedes", "Alpine", "Mclaren", "Alfa Romeo",
            "Aston Martin", "Haas", "Alphatauri", "Williams"};

    private final static Constructor[] constructorCache = new Constructor[constructors.length];

    public static void createAndShowGUI() {
        log.info("CREATING GUI...");
        Thread.currentThread().setName("Thread: GUI");

        JFrame frame = new JFrame("F1 Constructors");
        final JPanel titlePanel = new JPanel(new GridLayout());
        final JPanel menu = new JPanel(new GridLayout(0,2));
        final JTextField startYear = new JTextField();
        final JTextField endYear = new JTextField();
        final JComboBox<String> jlist1 = new JComboBox<>(constructors);
        final JLabel iconContainer1 = new JLabel();
        final JComboBox<String> jlist2 = new JComboBox<>(constructors);
        final ImageIcon logo2 = new ImageIcon("rb.jpg");
        final JLabel iconContainer2 = new JLabel();
        final JLabel textArea = new JLabel("Compare constructors", SwingConstants.CENTER);
        final ChartPanel chartPanel = new ChartPanel(ChartFactory.createLineChart(
                "",
                "Year",
                "",
                new DefaultCategoryDataset(),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        ));
        ItemListener listListner = new ItemListener() {
            public synchronized void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange()==ItemEvent.SELECTED) {
                    int index1 = jlist1.getSelectedIndex();
                    int index2 = jlist2.getSelectedIndex();
                    log.info("Team selection set to " + jlist1.getSelectedIndex() + " and " + jlist2.getSelectedIndex());
                    UpdateCache(index1, index2);
                    chartPanel.setChart(SetNewChart(index1, index2));
                    iconContainer1.setIcon(constructorCache[index1].getLogo());
                    iconContainer2.setIcon(constructorCache[index2].getLogo());
                }
            }
        };
        jlist1.addItemListener(listListner);
        jlist2.addItemListener(listListner);

        // icon containers
        iconContainer1.setPreferredSize(new Dimension(defaultWidth, defaultHeight));
        iconContainer2.setPreferredSize(new Dimension(defaultWidth, defaultHeight));
        iconContainer1.setVerticalAlignment(SwingConstants.CENTER);
        iconContainer1.setHorizontalAlignment(SwingConstants.CENTER);
        iconContainer2.setVerticalAlignment(SwingConstants.CENTER);
        iconContainer2.setHorizontalAlignment(SwingConstants.CENTER);
        iconContainer1.setFocusable(false);
        iconContainer2.setFocusable(false);

        startYear.setText("2012");
        endYear.setText("2022");
        startYear.setFont(new Font("Courier", Font.BOLD, 20));
        endYear.setFont(new Font("Courier", Font.BOLD, 20));
        startYear.setHorizontalAlignment(JTextField.CENTER);
        endYear.setHorizontalAlignment(JTextField.CENTER);

        FocusListener startYearListner = new FocusListener() {
            public void focusGained(FocusEvent fe)
            {
                log.info("Selected: " + fe.getComponent());
            }
            public synchronized void focusLost(FocusEvent ie) {
                int i;
                try {
                    i = Integer.parseInt(startYear.getText());
                }
                catch (NumberFormatException e) {
                    log.warn("Wrong format, returning default value");
                    startYear.setText("2006");
                    return;
                }
                log.info("Checking year format");
                if (i < 2006) startYear.setText("2006");
                if (i > Integer.parseInt(endYear.getText())) startYear.setText(endYear.getText());
            }
        };
        startYear.addFocusListener(startYearListner);
        FocusListener endYearListner = new FocusListener() {
            public void focusGained(FocusEvent fe)
            {
                log.info("Selected: " + fe.getComponent());
            }
            public synchronized void focusLost(FocusEvent ie) {
                int i;
                try {
                    i = Integer.parseInt(endYear.getText());
                }
                catch (NumberFormatException e) {
                    log.warn("Wrong format, returning default value");
                    endYear.setText("2006");
                    return;
                }
                log.info("Checking year format");
                if (i > 2022) endYear.setText("2006");
                if (i < Integer.parseInt(startYear.getText())) endYear.setText(endYear.getText());
            }
        };
        endYear.addFocusListener(endYearListner);

        menu.add(jlist1);
        menu.add(jlist2);
        menu.add(startYear);
        menu.add(endYear);


        titlePanel.add(iconContainer1);
        titlePanel.add(menu);
        titlePanel.add(iconContainer2);

        frame.add(chartPanel, BorderLayout.SOUTH);

        textArea.setFont(new Font("Courier", Font.BOLD, 70));

        frame.add(textArea, BorderLayout.NORTH);
        frame.add(titlePanel, BorderLayout.CENTER);

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
        } else {
            name = name.replaceAll(" ", "-").toLowerCase().strip();
            log.debug(name);
        }
        try {
            log.info("Downloading image for team: " + name);
            ImageIcon logo = new ImageIcon(new URL(imgPrefix + name + imgSuffix));
            constructor.setLogo(ScaleImage(logo));
            log.info("Image Download successful!");
        } catch (Exception e) {
            log.error("Image download error: " + e);
        }
        log.debug(constructor);
        return constructor;
    }

    private static DefaultCategoryDataset SetNewData(Constructor team1, Constructor team2) {
        log.info("Setting data");
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        if(team2.getStandings().size() > team1.getStandings().size()) {
            Constructor tmp = team1;
            team1 = team2;
            team2 = tmp;
        }
        ArrayList<Standing> c1 = team1.getStandings();
        ArrayList<Standing> c2 = team2.getStandings();
        for(int i = 0; i < c1.size(); i++) {
            dataset.addValue(c1.get(i).score, team1.getName(), Integer.toString(c1.get(i).year));
            if(i + c2.size() >= c1.size()) {
                dataset.addValue(c2.get(i - c1.size() + c2.size()).score, team2.getName(), Integer.toString(c2.get(i - c1.size() + c2.size()).year));
                continue;
            }
            dataset.addValue(0, team2.getName(), Integer.toString(c1.get(i).year));
        }
        log.info("Data set");
        return dataset;
    }

    private static JFreeChart SetNewChart(int index1, int index2) {
        log.info("Setting new chart");
        return  ChartFactory.createLineChart(
                "Amount of points earned in season",
                "Year",
                "Points",
                SetNewData(
                        constructorCache[index1],
                        constructorCache[index2]
                ),
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    private static void UpdateCache(int index1, int index2) {
        if (constructorCache[index1] == null) {
            log.info("Cache not found. Updating...");
            constructorCache[index1] = GetConstructorData(constructors[index1]);
        }
        if (constructorCache[index2] == null) {
            log.info("Cache not found. Updating...");
            constructorCache[index2] = GetConstructorData(constructors[index2]);
        }
    }
}