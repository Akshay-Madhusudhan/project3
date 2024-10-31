package com.example.project3;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import project3.*;
import util.*;
import javafx.fxml.FXML;
import javafx.beans.property.SimpleStringProperty;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.io.File;
import java.util.Scanner;


public class ClinicManagerController {
    List<Appointment> appointments = new List<>();
    List<Provider> providers = new List<>();
    Circular<Provider> technicians = new Circular<>();
    List<Timeslot> timeslots = Timeslot.generateTimeslots();
    List<Patient> record = new List<>();
    DecimalFormat df = new DecimalFormat("#,###.00");
    private Scanner scanner;

    @FXML
    private Label welcomeText;
    @FXML
    private Label providersLoadedLabel;
    @FXML
    private TextArea providersListTextArea;
    @FXML
    private Button loadProvidersButton;
    @FXML
    private TableView<String[]> providersTable;
    @FXML private TableColumn<String[], String> providerNameColumn;
    @FXML private TableColumn<String[], String> providerDOBColumn;
    @FXML private TableColumn<String[], String> providerPracticeColumn;
    @FXML private TableColumn<String[], String> providerLocationColumn;
    @FXML private TableColumn<String[], String> providerRateColumn;
    @FXML private TableColumn<String[], String> providerNPIColumn;
    private ObservableList<String[]> providersList = FXCollections.observableArrayList();;


    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onLoadProvidersButtonClick() {
        scanner = new Scanner(System.in);
        File fp = new File("providers.txt");

        providerNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
        providerDOBColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
        providerPracticeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));
        providerLocationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));
        providerRateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[4]));
        providerNPIColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[5]));

        providersTable.setItems(providersList);

        processProviders(fp); //need to find out how to find out ratePerVisit for technicians
        providersLoadedLabel.setText("Providers loaded");
        providersLoadedLabel.setTextFill(Color.LIME);
        providersLoadedLabel.setVisible(true);
        displayProviders();
        reverseTechnicians();
        printTechnicians();
    }

    private void processProviders(File fp){
        try{
            Scanner scanner = new Scanner(fp);
            while(scanner.hasNextLine()){
                String input = scanner.nextLine().trim();
                providersHelper(input);
            }
        } catch(FileNotFoundException e){
            providersLoadedLabel.setText("providers.txt file not found.");
            providersLoadedLabel.setTextFill(Color.RED);
            providersLoadedLabel.setVisible(true);
        }
    }

    private void providersHelper(String input){

        String[] separated_data = input.split("\\s+");
        String command = separated_data[0];
        String[] dateStrings;
        int month, day, year;
        Date dob;
        Profile newProfile;
        Location loc;
        Specialty specialty;
        String npi;
        Provider prov;
        switch(command){
            case "D":
                dateStrings = separated_data[3].split("/");
                month = Integer.parseInt(dateStrings[0]);
                day = Integer.parseInt(dateStrings[1]);
                year = Integer.parseInt(dateStrings[2]);
                dob = new Date(month, day, year);
                newProfile = new Profile(separated_data[1], separated_data[2], dob);
                loc = Location.valueOf(separated_data[4]);
                specialty = Specialty.valueOf(separated_data[5]);
                npi = separated_data[6];
                prov = new Doctor(newProfile, loc, specialty, npi);
                providers.add(prov);
                break;
            case "T":
                dateStrings = separated_data[3].split("/");
                month = Integer.parseInt(dateStrings[0]);
                day = Integer.parseInt(dateStrings[1]);
                year = Integer.parseInt(dateStrings[2]);
                dob = new Date(month, day, year);
                newProfile = new Profile(separated_data[1], separated_data[2], dob);
                loc = Location.valueOf(separated_data[4]);
                int ratePerVisit = Integer.parseInt(separated_data[5]);
                prov = new Technician(newProfile, loc, ratePerVisit);
                technicians.add(prov);
                providers.add(prov);
                break;
            default:
                providersListTextArea.appendText("Not valid command.\n");
                break;
        }
    }

    private void displayProviders(){
        Sort.provider(providers);
        for(Provider prov : providers){
            providersListTextArea.appendText(prov.toString() + "\n");
            if(prov.isDoc()) {
                Doctor doc = (Doctor) prov;
                addProvider((doc.getProfile().getFname().toUpperCase() + " " + doc.getProfile().getLname().toUpperCase()),
                        doc.getProfile().getDob().toString(), doc.getSpecialty().toString(), doc.getLocation().toString(), Integer.toString(doc.getSpecialty().getCharge()), doc.getNpi());
            } else {
                Technician tech = (Technician) prov;
                addProvider((tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase()),
                        tech.getProfile().getDob().toString(), "TECHNICIAN", tech.getLocation().toString(), "N/A", "N/A");
            }

        }
    }

    private void reverseTechnicians(){
        Circular<Provider> temp = new Circular<>();
        Technician last = (Technician) technicians.get(technicians.size()-1);
        Technician tempTech = last;
        int idx = technicians.size()-1;
        do{
            temp.add(tempTech);
            idx--;
            tempTech = (Technician)technicians.get(idx);
        } while(tempTech!=null && tempTech!=last);
        technicians = temp;
    }

    private void printTechnicians(){
        int idx = 0;
        providersListTextArea.appendText("\nRotation list for the technicians.\n");
        for(Provider tech: technicians){
            if(idx%technicians.size()==technicians.size()-1){
                providersListTextArea.appendText(tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase() + " (" + tech.getLocation().toString() + ")");
                break;
            }
            idx++;
            providersListTextArea.appendText(tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase() + " (" + tech.getLocation().toString() + ") --> ");
        }
    }

    public void addProvider(String name, String dob, String practice, String location, String rate, String npi) {
        String[] newProvider = { name, dob, practice, location, rate, npi };
        providersList.add(newProvider); // Add the new provider array to the ObservableList
    }
}