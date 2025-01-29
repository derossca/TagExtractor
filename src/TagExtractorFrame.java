import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;

public class TagExtractorFrame extends JFrame {
    JPanel mainPanel;
    JPanel buttonPanel;
    JPanel displayPanel;

    JButton quit;
    JButton write;
    JButton choose;

    JTextArea area;

    JScrollPane scroll;

    JLabel filename;

    private final Set<String> set = new HashSet<>();

    public TagExtractorFrame() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        createButtonPanel();
        createDisplayPanel();

        mainPanel.add(displayPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screen = kit.getScreenSize();
        int screenHeight = screen.height;
        int screenWidth = screen.width;

        setSize(screenWidth / 2, screenHeight / 2);
        setLocation(screenWidth / 4, screenHeight / 4);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void createButtonPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));

        choose = new JButton("Choose");
        write = new JButton("Write");
        quit = new JButton("Quit");

        choose.setFont(new Font("Times New Roman", Font.BOLD, 24));
        write.setFont(new Font("Times New Roman", Font.BOLD, 24));
        quit.setFont(new Font("Times New Roman", Font.BOLD, 24));

        choose.addActionListener((ActionEvent e) -> {pickFile();});
        write.addActionListener((ActionEvent e) -> {writeFile();});
        quit.addActionListener((ActionEvent e) -> {System.exit(0);});

        buttonPanel.add(choose);
        buttonPanel.add(write);
        buttonPanel.add(quit);
    }

    public void createDisplayPanel() {
        displayPanel = new JPanel();
        displayPanel.setBorder(new TitledBorder(new EtchedBorder(), "Tag Extractor"));
        displayPanel.setLayout(new BorderLayout());

        filename = new JLabel("Chosen File: ");
        filename.setHorizontalAlignment(JLabel.CENTER);
        filename.setFont(new Font("Times New Roman", Font.PLAIN, 18));

        area = new JTextArea();
        area.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        area.setEditable(false);

        scroll = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        displayPanel.setBackground(Color.white);
        displayPanel.add(filename, BorderLayout.NORTH);
        displayPanel.add(scroll, BorderLayout.CENTER);
    }

    public void pickFile() {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String record = "";
        ArrayList words = new ArrayList<>();
        try {
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                InputStream in = new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                int line = 0;
                filename.setText("Chosen File: " + selectedFile.getName());
                JOptionPane.showMessageDialog(displayPanel, "Choose a Stop Word Filter File", "Choose a File", JOptionPane.INFORMATION_MESSAGE);

                chooseStopWordFilterList();

                Scanner scanner = new Scanner(selectedFile);

                while(scanner.hasNext()) {
                    words.add(scanner.next().replace("\t", "").replace(" ", "").replace("-", "").replace("!", "").replace(".", "").replace(",", "").replace("\t\t", "").trim().toLowerCase());
                }

                for(int i = 0; i < words.size(); i++) {
                    if(set.contains((String) words.get(i))) {
                        words.remove(i);
                        i--;
                    } else if (((String) words.get(i)).length() < 3) {
                        words.remove(i);
                        i--;
                    }
                }
                Map<String, Integer> map = (Map<String, Integer>) words.parallelStream().collect(Collectors.groupingByConcurrent(w -> w, Collectors.counting()));
                for(Map.Entry<String, Integer> en: map.entrySet()) {
                    area.append("Word:\t" + en.getKey() + "\t\tFrequency:\t" + en.getValue() + "\n");
                }
                reader.close();
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!!!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void chooseStopWordFilterList() {
        JFileChooser chooser = new JFileChooser();
        File selectedFile;
        String record = "";

        try {
            File workingDirectory = new File(System.getProperty("user.dir"));
            chooser.setCurrentDirectory(workingDirectory);
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                Path file = selectedFile.toPath();
                InputStream in = new BufferedInputStream(Files.newInputStream(file, CREATE));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                Scanner scanner = new Scanner(selectedFile);

                while(scanner.hasNext()) {
                    record = scanner.next();
                    set.add(record.toLowerCase());
                }
                reader.close();
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!!!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        Path file = Paths.get(workingDirectory.getPath() + "\\src\\filtered.txt");

        try {
            if(!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        try {
            OutputStream out = new BufferedOutputStream(Files.newOutputStream(file, CREATE));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

            writer.write(area.getText());
            JOptionPane.showMessageDialog(displayPanel, "File has been written!", "File", JOptionPane.INFORMATION_MESSAGE);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
