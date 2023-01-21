import javax.swing.*;
import org.apache.logging.log4j.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GUI {
    private static final Logger log = LogManager.getRootLogger();
    private final static int defaultWidth = 100;
    private final static int defaultHeight = 100;
    private static int buttonPress = 0;
    private static final String[] constructors = {"-", "Red Bull", "Ferrari", "Mercedes", "Alpine", "Mclaren", "Alfa Romeo",
            "Aston Martin", "Haas", "Alphatauri", "Williams"};
    private static final DefaultCategoryDataset data = new DefaultCategoryDataset();
    private static final ChartPanel chartPanel = new ChartPanel(ChartFactory.createLineChart(
            "Points earned in season",
            "Year",
            "points",
            data,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
    ));
    private static final JLabel[] iconContainer = { new JLabel(), new JLabel() };
    private static final JComboBox<String> jlist1 = new JComboBox<>(constructors);
    private static final JComboBox<String> jlist2 = new JComboBox<>(constructors);
    private static final JTextField startYear = new JTextField();
    private static final JTextField endYear = new JTextField();
    private final static Set<ParserTask> set = Collections.synchronizedSet(new HashSet<ParserTask>());
    private final static ExecutorService pool = Executors.newFixedThreadPool(5);
    private final static Constructor[] constructorCache = new Constructor[constructors.length];

    public static void createAndShowGUI() {
        log.info("CREATING GUI...");
        Thread.currentThread().setName("Thread: GUI");

        JFrame frame = new JFrame("F1 Constructors");
        final JPanel titlePanel = new JPanel(new GridLayout());
        final JPanel menu = new JPanel(new GridLayout(0,2));
        final JButton generateButton = new JButton();
        final JLabel textArea = new JLabel("Compare constructors", SwingConstants.CENTER);


        ItemListener listListner = new ItemListener() {
            public synchronized void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange()==ItemEvent.SELECTED) {
                    log.info("Team selection set to " + jlist1.getSelectedIndex() + " and " + jlist2.getSelectedIndex());
                }
            }
        };
        jlist1.addItemListener(listListner);
        jlist2.addItemListener(listListner);

        // icon containers
        for(int i = 0; i < iconContainer.length; i++) {
            iconContainer[i].setPreferredSize(new Dimension(defaultWidth, defaultHeight));
            iconContainer[i].setVerticalAlignment(SwingConstants.CENTER);
            iconContainer[i].setHorizontalAlignment(SwingConstants.CENTER);
            iconContainer[i].setFocusable(false);
        }

        startYear.setText("2012");
        endYear.setText("2022");
        startYear.setFont(new Font("Courier", Font.BOLD, 20));
        endYear.setFont(new Font("Courier", Font.BOLD, 20));
        startYear.setHorizontalAlignment(JTextField.CENTER);
        endYear.setHorizontalAlignment(JTextField.CENTER);

        FocusListener startYearListner = new FocusListener() {
            public synchronized void focusGained(FocusEvent fe)
            {
                log.info("Selected year field" + fe.getComponent());
            }
            public synchronized void focusLost(FocusEvent ie) {
                int i;
                try {
                    i = Integer.parseInt(startYear.getText());
                }
                catch (NumberFormatException e) {
                    log.warn("Wrong format, returning default value");
                    startYear.setText("2012");
                    return;
                }
                log.info("Checking year format");
                if (i < 2012) startYear.setText("2012");
                if (i >= Integer.parseInt(endYear.getText())) startYear.setText(Integer.toString(Integer.parseInt(endYear.getText()) - 1));
            }
        };
        startYear.addFocusListener(startYearListner);
        FocusListener endYearListner = new FocusListener() {
            public synchronized void focusGained(FocusEvent fe)
            {
                log.info("Selected year field");
            }
            public synchronized void focusLost(FocusEvent ie) {
                int i;
                try {
                    i = Integer.parseInt(endYear.getText());
                }
                catch (NumberFormatException e) {
                    log.warn("Wrong format, returning default value");
                    endYear.setText("2022");
                    return;
                }
                log.info("Checking year format");
                if (i > 2022) endYear.setText("2022");
                if (i < Integer.parseInt(startYear.getText())) endYear.setText(endYear.getText());
            }
        };
        endYear.addFocusListener(endYearListner);

        generateButton.setText("Generate graph!");
        ActionListener buttonListener = new ActionListener() {
            @Override
            public synchronized void actionPerformed(ActionEvent e) {
                buttonPress += 1;
                data.clear();
                int index1 = jlist1.getSelectedIndex();
                int index2 = jlist2.getSelectedIndex();
                log.info("Team selection set to " + jlist1.getSelectedIndex() + " and " + jlist2.getSelectedIndex());
                if(index1 != 0) UpdateCache(index1, 0, buttonPress, Integer.parseInt(startYear.getText()), Integer.parseInt(endYear.getText()));
                if(index2 != 0) UpdateCache(index2, 1, buttonPress, Integer.parseInt(startYear.getText()), Integer.parseInt(endYear.getText()));
            }
        };
        generateButton.addActionListener(buttonListener);

        menu.add(jlist1);
        menu.add(jlist2);
        menu.add(startYear);
        menu.add(endYear);
        menu.add(new JLabel("Select years to plot"));
        menu.add(generateButton);


        titlePanel.add(iconContainer[0]);
        titlePanel.add(menu);
        titlePanel.add(iconContainer[1]);

        frame.add(chartPanel, BorderLayout.SOUTH);

        textArea.setFont(new Font("Courier", Font.BOLD, 70));

        frame.add(textArea, BorderLayout.NORTH);
        frame.add(titlePanel, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
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
    private static void SetData(Constructor team, int startYear, int endYear) {
        log.info("Setting data");
        ArrayList<Standing> c = team.getStandings();
        for(int i = 2022 - endYear; i <= 2022 - startYear; i++) {
            if(i >= c.size()) continue;
            data.addValue(c.get(i).score, team.getName(), Integer.toString(c.get(i).year));
        }
        log.info("Data set");
    }
    private static synchronized void UpdateCache(int index, int imgNum, int thisPress, int startYear, int endYear) {
        if (constructorCache[index] != null) {
            log.info("Cache found. Setting data for graph...");
            SetData(constructorCache[index], startYear, endYear);
            iconContainer[imgNum].setIcon(constructorCache[index].getLogo());
            return;
        }
        add(new ParserTask(index, imgNum, thisPress, startYear, endYear));
    }
    private static void add(ParserTask parserTask) {
        if (set.add(parserTask)) {
            // New TASK (new element in the set)
            parserTask.addFuture(pool.submit(parserTask));
            log.trace("[add] "+"New "+parserTask);
            return;
        }

        ParserTask tmp = null;
        try {
            tmp = set.stream().filter(parserTask::equals).findAny().orElse(null);
            log.trace("[add] "+"Found "+tmp+(tmp.isDone() ? " DONE" : " WAITING"));
        } catch(Exception e) {
            log.error("[add] Problem with "+tmp+", "+e.getMessage());
            return;
        }
    }
    private static  class ParserTask implements Runnable {
        int nameNum;
        int imgNum;
        int thisPress;
        int startYear;
        int endYear;
        String name;
        String standingPrefix = "https://www.formula1.com/en/results.html/";
        String imgPrefix = "https://www.formula1.com/content/dam/fom-website/teams/2023/";
        String imgSuffix = "-logo.png.transform/2col/image.png";
        Constructor constructor = new Constructor();
        boolean standingSuccess = false;
        boolean logoSuccess = false;
        private Future<?> future;
        private static final int MAX_ATTEMPTS = 5;
        public ParserTask(int constructor, int imgNum, int counter, int startYear, int endYear) {
            this.nameNum = constructor;
            this.imgNum = imgNum;
            this.thisPress = counter;
            this.startYear = startYear;
            this.endYear = endYear;
        }
        public void addFuture(Future<?> submit) {
            this.future=submit;
        }
        public boolean isDone() {
            return this.future.isDone();
        }
        public void run() {
            Thread.currentThread().setName("Thread: "+constructors[this.nameNum]);
            //try { Thread.sleep((long) (Math.random() * 50000)); } catch (InterruptedException e) {}

            log.info("New thread to download data for: " + constructors[nameNum]);

            int attempt=1;
            do {
                name = constructors[nameNum];
                constructor.setName(name);
                // downloading standings
                if(!standingSuccess) {
                    ArrayList<Standing> tmpStand = new ArrayList<Standing>();
                    for (int i = 2012; i <= 2022; i++) {    // Downloading standings for last x years
                        Document r1 = null;
                        try {
                            r1 = Jsoup.connect(standingPrefix + i + "/team.html").get();
                            Element tmp = r1.select("table[class=resultsarchive-table]").first();
                            for (Element e : tmp.select("tr")) {
                                if (!e.select("td:eq(2)").text().toLowerCase().startsWith(name.toLowerCase())) {
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
                            standingSuccess = true;
                        } catch (Exception e) {
                            log.error("Standing download error: " + e);
                        }
                    }
                    constructor.setStandings(tmpStand);
                }

                // downlading team logo
                if(!logoSuccess) {
                    if (name.equalsIgnoreCase("red bull")) {
                        name = "red-bull-racing";
                    } else if (name.equalsIgnoreCase("haas")) {
                        name = "haas-f1-team";
                    } else {
                        name = name.replaceAll(" ", "-").toLowerCase().strip();
                        log.debug(name);
                    }
                    try {
                        log.info("Downloading image for team: " + name);
                        ImageIcon logo = new ImageIcon(new URL(imgPrefix + name + imgSuffix));
                        constructor.setLogo(ScaleImage(logo));
                        log.info("Image Download successful!");
                        logoSuccess = true;
                    } catch (Exception e) {
                        log.error("Image download error: " + e);
                    }
                }

                if(this.thisPress == buttonPress) {
                    if (standingSuccess) {
                        constructorCache[nameNum] = constructor;
                        SetData(constructorCache[nameNum], startYear, endYear);
                        log.debug(constructor);
                    }
                    if (logoSuccess) {
                        iconContainer[imgNum].setIcon(constructorCache[nameNum].getLogo());
                    }
                }

                if(standingSuccess && logoSuccess) return;
                log.info("Trying to connect again in 3 seconds...");
                try { Thread.sleep(3000); } catch (InterruptedException e1) {}
            } while (attempt++<=MAX_ATTEMPTS);
        }

    }

}

