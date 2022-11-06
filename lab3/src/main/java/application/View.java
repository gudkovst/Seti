package application;

import config.Config;
import records.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class View extends JFrame implements Runnable{
    private JPanel entryField;
    private List<GeocodeRecord> locations;
    private final Requester requester;
    private JTextField textNamePlace;
    private JLabel messFind;
    private boolean flagFind = false;
    private JList<String> placesList;
    private final DefaultListModel<String> listModel;

    public View(Requester req){
        requester = req;
        listModel = new DefaultListModel<>();
    }

    @Override
    public void run(){
        init();
        initStartFrame();
    }

    private void init(){
        setSize(Config.WIDTH, Config.HEIGHT);
        getContentPane().setBackground(Color.white); // Set background color
        setDefaultCloseOperation(EXIT_ON_CLOSE); // When "(X)" clicked, process is being killed
        setTitle("Places"); // Set title
        setResizable(true);
        setVisible(true); // Show everything
    }

    private void initStartFrame(){
        textNamePlace = new JTextField();
        textNamePlace.setToolTipText("Name place");
        textNamePlace.setHorizontalAlignment(JTextField.CENTER);
        JButton buttonFind = new JButton("Find");
        JLabel messHead = new JLabel("Enter the name of the place you are interested in:");
        messHead.setHorizontalAlignment(JLabel.CENTER);

        entryField = new JPanel();
        entryField.setLayout(new GridLayout(4, 1));
        entryField.add(messHead);
        entryField.add(textNamePlace);
        entryField.add(buttonFind);
        setContentPane(entryField);
        buttonFind.setMnemonic(KeyEvent.VK_ENTER);
        buttonFind.addActionListener(e -> {
            try {
                handleFindGeocode();
            } catch (IOException | ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void handleFindGeocode() throws IOException, ExecutionException, InterruptedException {
        if (flagFind){
            return;
        }
        flagFind = true;
        String namePlace = textNamePlace.getText().replace(" ", "+");
        CompletableFuture<List<GeocodeRecord>> request = requester.requestGeocode(namePlace);
        request.thenAccept(this::showListLocations);
        javax.swing.SwingUtilities.invokeLater(() -> {
            messFind = new JLabel("Wait, please. There is a search.");
            messFind.setHorizontalAlignment(JLabel.CENTER);
            entryField.add(messFind);
            setContentPane(entryField);
        });
    }

    private void showListLocations(List<GeocodeRecord> listLocations){
        locations = listLocations;
        if (locations == null || locations.size() == 0){
            javax.swing.SwingUtilities.invokeLater(() -> {
                JLabel label = new JLabel("Nothing found");
                label.setHorizontalAlignment(JLabel.CENTER);
                entryField.removeAll();
                entryField.add(label);
                setContentPane(entryField);
            });
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            entryField.setLayout(new GridLayout(locations.size(), 1));
            Button[] locationsButtons = new Button[locations.size()];
            entryField.removeAll();
            listModel.removeAllElements();
            for (int i = 0; i < locations.size(); i++){
                GeocodeRecord location = locations.get(i);
                locationsButtons[i] = new Button(location.getName());
                entryField.add(locationsButtons[i]);
                locationsButtons[i].addActionListener(e -> handleLocation(location));
            }
            setContentPane(entryField);
        });
    }

    private void handleLocation(GeocodeRecord location){
        CompletableFuture<WeatherRecord> weather = requester.requestWeather(location.getLat(), location.getLng());
        CompletableFuture<List<PlacesRecord>> places = requester.requestPlaces(location.getLat(), location.getLng());
        javax.swing.SwingUtilities.invokeLater(() -> {
            JLabel label = new JLabel("Interesting places in " + location.getName());
            label.setHorizontalAlignment(JLabel.CENTER);
            Button returnButton = new Button("return to list locations");
            entryField.removeAll();
            entryField.add(label);
            entryField.add(messFind);
            entryField.add(returnButton);
            setContentPane(entryField);
            returnButton.addActionListener(e -> showListLocations(locations));
        });
        weather.thenAccept(this::showWeather);
        places.thenAccept(this::findDescrPlaces);
    }

    private void showWeather(WeatherRecord weather){
        javax.swing.SwingUtilities.invokeLater(() -> {
            String ws = "    ";
            String info = "Weather:";
            for (Weather w : weather.getWeather()){
                info += ws + w.getWeather();
            }
            info += ws + "temperature = " + weather.getTemperature();
            info += ws + "wind speed = " + weather.getWindSpeed();
            JLabel weatherLabel = new JLabel(info);
            weatherLabel.setHorizontalAlignment(JLabel.CENTER);
            entryField.add(weatherLabel);
            entryField.remove(messFind);
            setContentPane(entryField);
        });
    }

    private void findDescrPlaces(List<PlacesRecord> places){
        entryField.setLayout(new GridLayout(4, 1));
        if (places.size() == 0){
            javax.swing.SwingUtilities.invokeLater(() -> {
                JLabel label = new JLabel("There are no interesting places in the radius " + Config.RADIUS + " meters");
                label.setHorizontalAlignment(JLabel.CENTER);
                entryField.add(label);
            });
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            placesList = new JList<>(listModel);
            entryField.add(new JScrollPane(placesList));
        });
        for (PlacesRecord place : places) {
            requester.requestDescription(place.getId()).thenAccept(this::showDescription);
        }
    }

    private void showDescription(DescriptionPlaceRecord description){
        if ("".equals(description.getName())){
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            listModel.addElement(description.getName() + " " + description.getDescription());
            int index = listModel.size() - 1;
            placesList.ensureIndexIsVisible(index);
            setContentPane(entryField);
        });
    }
}
