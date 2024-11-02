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

/**
 * @author Akshay Madhusudhan
 * @author Aidan Pembleton
 */
public class ClinicManagerController {
    List<Appointment> appointments = new List<>();
    List<Provider> providers = new List<>();
    Circular<Provider> technicians = new Circular<>();
    List<Timeslot> timeslots = Timeslot.generateTimeslots();
    List<Patient> record = new List<>();
    DecimalFormat df = new DecimalFormat("#,###.00");
    private Scanner scanner;

    @FXML
    private Label providersLoadedLabel;
    @FXML
    private TextArea out;
    @FXML
    private Button loadProvidersButton;
    @FXML
    private Button clearOut;
    @FXML
    private RadioButton officeButton;
    @FXML
    private RadioButton imagingButton;
    @FXML
    private ToggleGroup toggleGroup = new ToggleGroup();
    @FXML
    private DatePicker appDatePicker;
    @FXML
    private ComboBox<String> doctorPicker;
    @FXML
    private ComboBox<String> roomPicker;
    @FXML
    private ComboBox<String> timeslotPicker;
    @FXML
    private TableView<String[]> providersTable;
    @FXML
    private TextField patientFname;
    @FXML
    private TextField patientLname;
    @FXML
    private DatePicker patientDOB;
    @FXML
    private Button scheduleButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button rescheduleButton;
    @FXML
    private ComboBox<String> initialSlotPicker;
    @FXML
    private ComboBox<String> newSlotPicker;
    @FXML
    private TextField rPatientFname;
    @FXML
    private TextField rPatientLname;
    @FXML
    private DatePicker rPatientDOB;
    @FXML
    private DatePicker initialAppDate;
    @FXML
    private Button PAButton;
    @FXML
    private Button PPButton;
    @FXML
    private Button PLButton;
    @FXML
    private Button POButton;
    @FXML
    private Button PIButton;
    @FXML
    private Button PCButton;
    @FXML
    private Button PSButton;
    @FXML private TableColumn<String[], String> providerNameColumn;
    @FXML private TableColumn<String[], String> providerDOBColumn;
    @FXML private TableColumn<String[], String> providerPracticeColumn;
    @FXML private TableColumn<String[], String> providerLocationColumn;
    @FXML private TableColumn<String[], String> providerRateColumn;
    @FXML private TableColumn<String[], String> providerNPIColumn;
    private ObservableList<String[]> providersList = FXCollections.observableArrayList();;

    /**
     * Method to call methods to fill the drop-down menus
     */
    @FXML
    protected void initialize(){
        initializeTimeslotButton();
        initializeRoomButton();
        initializeRSlotPicker();
        initializeRNewSlotPicker();
    }

    /**
     * Method to fill the timeslot drop-down
     */
    @FXML
    protected void initializeTimeslotButton(){
        int idx = 1;
        for(Timeslot slot : timeslots){
            timeslotPicker.getItems().add("(" + idx + ") " + slot.toString());
            idx++;
        }
    }

    /**
     * Method to fill the reschedule initial timeslot drop-down
     */
    @FXML
    protected void initializeRSlotPicker(){
        int idx = 1;
        for(Timeslot slot: timeslots){
            initialSlotPicker.getItems().add("(" + idx + ") " + slot.toString());
            idx++;
        }
    }

    /**
     * Method to fill the reschedule new timeslot drop-down
     */
    @FXML
    protected void initializeRNewSlotPicker(){
        int idx = 1;
        for(Timeslot slot: timeslots){
            newSlotPicker.getItems().add("(" + idx + ") " + slot.toString());
            idx++;
        }
    }

    /**
     * Method to fill the room drop-down for imaging appointments
     */
    @FXML
    protected void initializeRoomButton(){
        roomPicker.getItems().add(Radiology.XRAY.toString());
        roomPicker.getItems().add(Radiology.CATSCAN.toString());
        roomPicker.getItems().add(Radiology.ULTRASOUND.toString());
    }

    /**
     * Method to show/hide appropriate info boxes for scheduling an office appointment
     */
    @FXML
    protected void onOfficeButtonClick(){
        imagingButton.setToggleGroup(toggleGroup);
        roomPicker.setDisable(true);
        doctorPicker.setDisable(false);
    }

    /**
     * Method to show/hide appropriate info boxes for scheduling an imaging appointment
     */
    @FXML
    protected void onImagingButtonClick(){
        officeButton.setToggleGroup(toggleGroup);
        roomPicker.setDisable(false);
        doctorPicker.setDisable(true);
    }

    /**
     * Method to clear the output text area
     */
    @FXML
    protected void onClearOutButtonClick(){
        out.clear();
        out.appendText("Cleared.\n");
    }

    /**
     * Method to load in providers from a providers.txt file in ../project3/
     */
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
        for(Provider prov : providers){
            if(prov.getClass() == Doctor.class){
                doctorPicker.getItems().add(prov.getProfile().getFname() + " " + prov.getProfile().getLname() + " (" + ((Doctor) prov).getNpi() + ")");
            }
        }
        displayProviders();
        reverseTechnicians();
        printTechnicians();
    }

    /**
     * @param fp provider file taken in
     * Method to process providers.txt file
     */
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

    /**
     * @param input a line of the providers.txt file taken one by one
     * helper method for processProviders() to populate providers and technicians
     */
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
                out.appendText("Not valid command.\n");
                break;
        }
    }

    /**
     * Method to add providers to Providers tab TableView
     */
    private void displayProviders(){
        Sort.provider(providers);
        for(Provider prov : providers){
            out.appendText(prov.toString() + "\n");
            if(prov.isDoc()) {
                Doctor doc = (Doctor) prov;
                addProvider((doc.getProfile().getFname().toUpperCase() + " " + doc.getProfile().getLname().toUpperCase()),
                        doc.getProfile().getDob().toString(), doc.getSpecialty().toString(), doc.getLocation().toString(), df.format(doc.getSpecialty().getCharge()), doc.getNpi());
            } else {
                Technician tech = (Technician) prov;
                addProvider((tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase()),
                        tech.getProfile().getDob().toString(), "TECHNICIAN", tech.getLocation().toString(), df.format(tech.rate()), "N/A");
            }

        }
    }

    /**
     * Reverses technicians in the circular linked list to make iterating easier
     */
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

    /**
     * prints technicians to output TextArea traversing as though it is a circular linked list
     */
    private void printTechnicians(){
        int idx = 0;
        out.appendText("\nRotation list for the technicians.\n");
        for(Provider tech: technicians){
            if(idx%technicians.size()==technicians.size()-1){
                out.appendText(tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase() + " (" + tech.getLocation().toString() + ")");
                break;
            }
            idx++;
            out.appendText(tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase() + " (" + tech.getLocation().toString() + ") --> ");
        }
        out.appendText("\n");
    }

    /**
     * @param name Provider's first and last name
     * @param dob Provider's date of birth
     * @param practice Provider's practice
     * @param location Provider's location
     * @param rate Provider's rate
     * @param npi Provider's npi
     * Adds an individual Provider's information to the TableView in the Providers tab
     */
    public void addProvider(String name, String dob, String practice, String location, String rate, String npi) {
        String[] newProvider = { name, dob, practice, location, rate, npi };
        providersList.add(newProvider); // Add the new provider array to the ObservableList
    }

    /**
     * Method to handle the schedule button click event
     */
    @FXML
    protected void onScheduleButtonClick(){
        if(officeButton.isSelected()) {
            try {
                String[] separated_data = {appDatePicker.getValue().toString(), timeslotPicker.getValue(), patientFname.getText().trim(), patientLname.getText().trim(), patientDOB.getValue().toString(), doctorPicker.getValue()};
                scheduleAppointment(separated_data);
            } catch(Exception e){
                out.appendText("Please fill out all the required fields. (Load Providers if there are no providers listed in dropdown)\n");
            }
        } else if(imagingButton.isSelected()){
            try {
                String[] separated_data = {appDatePicker.getValue().toString(), timeslotPicker.getValue(), patientFname.getText().trim(), patientLname.getText().trim(), patientDOB.getValue().toString(), roomPicker.getValue()};
                scheduleImagingAppointment(separated_data);
            } catch(Exception e){
                out.appendText("Please fill out all the required fields. (Load Providers if there are no providers listed in dropdown)\n");
            }
        } else if(!officeButton.isSelected() && !imagingButton.isSelected()){
            out.appendText("Please select an office or imaging appointment to schedule.\n");
        }
    }

    /**
     * Method to handle the cancel button click event
     */
    @FXML
    protected void onCancelButtonClick(){
        if(officeButton.isSelected() || imagingButton.isSelected()){
            try{
                String[] separated_data = {appDatePicker.getValue().toString(), timeslotPicker.getValue(), patientFname.getText().trim(), patientLname.getText().trim(), patientDOB.getValue().toString()};
                cancelAppointment(separated_data);
            } catch(Exception e){
                out.appendText("Please fill out all the required fields.\n");
            }
        }
    }

    /**
     * Method to handle the reschedule button click event
     */
    @FXML
    protected void onRescheduleButtonClick(){
        try{
            String[] separated_data = {initialAppDate.getValue().toString(), initialSlotPicker.getValue(), rPatientFname.getText(), rPatientLname.getText(), rPatientDOB.getValue().toString(), newSlotPicker.getValue()};
            rescheduleAppointment(separated_data);
        } catch(Exception e){
            out.appendText("Please fill out all the required fields.\n");
        }
    }

    /**
     * Method to print appointments by date when date button clicked
     */
    @FXML
    protected void onPAButtonClick(){
        if(appointments.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }
        out.appendText("\n**List of appointments, ordered by date/time/provider.\n");
        printAppointments(Sort.appointment(appointments, 'A'));
        out.appendText("** end of list **\n");
    }

    /**
     * Method to print appointments by patient when patient button clicked
     */
    @FXML
    protected void onPPButtonClick(){
        if(appointments.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }
        out.appendText("\n**List of appointments, ordered by patient/date/time.\n");
        printAppointments(Sort.appointment(appointments, 'P'));
        out.appendText("** end of list **\n");
    }

    /**
     * Method to print appointments by location when location button clicked
     */
    @FXML
    protected void onPLButtonClick(){
        if(appointments.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }
        out.appendText("\n**List of appointments, ordered by county/date/time.\n");
        printAppointments(Sort.appointment(appointments, 'L'));
        out.appendText("** end of list **\n");
    }

    /**
     * Method to print office appointments
     */
    @FXML
    protected void onPOButtonClick(){
        if(appointments.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }
        out.appendText("\n**List of office appointments ordered by county/date/time.\n");
        printAppointments(Sort.appointment(appointments, 'O'));
        out.appendText("** end of list **\n");
    }

    /**
     * Method to print imaging appointments
     */
    @FXML
    protected void onPIButtonClick(){
        if(appointments.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }
        out.appendText("\n**List of imaging appointments ordered by county/date/time.\n");
        printAppointments(Sort.appointment(appointments, 'I'));
        out.appendText("** end of list **\n");
    }

    /**
     * Method to print credits when Print Credits button clicked
     */
    @FXML
    protected void onPCButtonClick(){
        Sort.provider(providers);
        printCredits(providers);
    }

    /**
     * Method to print billing statement when Print Billing Statement button clicked
     */
    @FXML
    protected void onPSButtonClick(){
        printStatements();
        appointments = new List<>();
    }

    /**
     * @param separated_data an array of Strings containing data pulled from various buttons, text fields, and drop-downs
     * Helper method to add an appointment to the appointments List, if valid
     */
    private void scheduleAppointment(String[] separated_data) {
        try {
            if(separated_data.length!=6){
                out.appendText("Missing data tokens.\n");
                return;
            }
            String[] dateStrings = separated_data[0].split("-");
            int year = Integer.parseInt(dateStrings[0]);
            int month = Integer.parseInt(dateStrings[1]);
            int day = Integer.parseInt(dateStrings[2]);
            Date appointmentDate = new Date(month, day, year);

            if (appointmentDate.isValidMaster() != null) {
                out.appendText(appointmentDate.isValidMaster() + "\n");
                return;
            }

            String timeslotString = separated_data[1].split("[()]+")[1].trim();

            Timeslot timeslot = timeslots.get(Integer.parseInt(timeslotString) - 1);


            String fname = separated_data[2];
            String lname = separated_data[3];
            if(fname.isEmpty() || lname.isEmpty()){
                out.appendText("Please fill out all the required fields. (Load Providers if there are no providers listed in dropdown)\n");
                return;
            }
            String[] dobStrings = separated_data[4].split("-");
            int dobYear = Integer.parseInt(dobStrings[0]);
            int dobMonth = Integer.parseInt(dobStrings[1]);
            int dobDay = Integer.parseInt(dobStrings[2]);
            Date dobDate = new Date(dobMonth, dobDay, dobYear);
            Profile patient = new Profile(fname, lname, dobDate);
            if(dobYear < 1900){
                out.appendText("Patient dob: " + dobDate.toString() + " is not a valid calendar date.\n");
                return;
            }
            if (!dobDate.isValidBirth()) {
                out.appendText("Patient dob: " + dobDate.toString() + " is today or a date after today.\n");
                return;
            }

            String npi = separated_data[5].split("[()]+")[1].trim();
            if (!checkProviderExists(npi)) {
                out.appendText(npi +
                        " - provider doesn't exist.\n");
                return;
            }

            Provider provider = null;
            for (Provider prov : providers) {
                if (prov.getClass() == Doctor.class && ((Doctor) prov).getNpi().equals(npi)) {
                    provider = prov;
                    break;
                }
            }

            Appointment appointment = new Appointment(appointmentDate, timeslot, patient, provider);

            if (appointment.appointmentValid(appointment, appointments) != null) {
                out.appendText(appointment.appointmentValid(appointment, appointments) + "\n");
                return;
            }

            Doctor doc = (Doctor) provider;

            if (providerBooked(timeslot, appointment)) {
                out.appendText("[" + doc.getProfile().getFname().toUpperCase() + " " + doc.getProfile().getLname().toUpperCase() +
                        " " + doc.getProfile().getDob().toString() + ", " +
                        doc.getLocation().toString() + ", " + doc.getLocation().countyString() + " " + doc.getLocation().getZip() + "][" +
                        doc.getSpecialty().toString() + ", #" + doc.getNpi() + "] is not available at slot " + timeslotString + ".\n");
                return;
            }

            appointments.add(appointment);
            addToMedicalRecord(patient, appointment);
            out.appendText(appointment.getDate().toString() + " " + timeslot.toString() + " " + patient.getFname() + " " + patient.getLname() + " " + patient.getDob().toString() + " " +
                    "[" + doc.getProfile().getFname().toUpperCase() + " " + doc.getProfile().getLname().toUpperCase() +
                    " " + doc.getProfile().getDob().toString() + ", " +
                    doc.getLocation().toString() + ", " + doc.getLocation().countyString() + " " + doc.getLocation().getZip() + "][" +
                    doc.getSpecialty().toString() + ", #" + doc.getNpi() + "] booked.\n");
        } catch (Exception e){
            out.appendText("Missing data tokens.\n");
        }
    }

    /**
     * @param patient The Profile to be added to the record, if they don't exist on it already
     * @param appointment The Appointment to be added alongside the Profile to the record
     * Method to add a Profile to the medical record if they don't exist on it, and add an Appointment linked to the given Profile
     */
    private void addToMedicalRecord(Profile patient, Appointment appointment){
        Patient patientObj = new Patient(patient);
        int patientIndex = record.indexOf(patientObj);
        if(patientIndex != -1){
            record.get(patientIndex).add(appointment);
        } else {
            record.add(patientObj);
            int newPatientIndex = record.indexOf(patientObj);
            record.get(newPatientIndex).add(appointment);
        }
    }

    /**
     * @param npi a Provider NPI number
     * @return true if given NPI is associated with any Doctor
     * Method to determine if a Doctor exists, given their NPI
     */
    private boolean checkProviderExists(String npi) {
        for (Provider p : providers) {
            if (p.getClass()==Doctor.class && ((Doctor) p).getNpi().equals(npi)) {
                return true;
            }
        }
        return false;
    }

    // Takes a given timeslot and appointment, determines if provider of that appointment is busy at that timeslot on that day
    /**
     * @param timeslot A Timeslot to check if the provider is busy at it
     * @param appointment An Appointment with an associated Provider and date
     * @return True if provider is busy at given timeslot, false otherwise
     * Method to determine if a provider is booked at a given timeslot on the same day as a given Appointment
     */
    private boolean providerBooked(Timeslot timeslot, Appointment appointment) {
        for(int i = 0; i < appointments.size(); i++){
            Appointment pointer = appointments.get(i);
            if(pointer.getDate().equals(appointment.getDate()) &&
                    pointer.getTimeslot().equals(appointment.getTimeslot()) &&
                    pointer.getProvider().equals(appointment.getProvider())){
                return true;
            }
        }
        return false;
    }

    /**
     * @param separated_data an array of Strings containing data pulled from various buttons, text fields, and drop-downs
     * Helper method to add specifically an imaging appointment to the appointments List, if valid
     */
    private void scheduleImagingAppointment(String[] separated_data){
        // This try/catch is to catch errors where user doesn't include entire command
        try{
            separated_data[0] = separated_data[0];
            separated_data[1] = separated_data[1];
            separated_data[2] = separated_data[2];
            separated_data[3] = separated_data[3];
            separated_data[4] = separated_data[4];
            separated_data[5] = separated_data[5];
        } catch(IndexOutOfBoundsException e){
            out.appendText("Missing data tokens.\n");
            return;
        }

        String[] dateStrings = separated_data[0].split("-");
        int year = Integer.parseInt(dateStrings[0]);
        int month = Integer.parseInt(dateStrings[1]);
        int day = Integer.parseInt(dateStrings[2]);
        Date appointmentDate = new Date(month, day, year);

        if(appointmentDate.isValidMaster()!=null){
            out.appendText(appointmentDate.isValidMaster() + "\n");
            return;
        }

        String timeslotString = separated_data[1].split("[()]+")[1].trim();
        Timeslot timeslot = timeslots.get(Integer.parseInt(timeslotString) - 1);

        String fname = separated_data[2];
        String lname = separated_data[3];
        if(fname.isEmpty() || lname.isEmpty()){
            out.appendText("Please fill out all the required fields. (Load Providers if there are no providers listed in dropdown)\n");
            return;
        }
        String[] dobStrings = separated_data[4].split("-");
        int dobYear = Integer.parseInt(dobStrings[0]);
        int dobMonth = Integer.parseInt(dobStrings[1]);
        int dobDay = Integer.parseInt(dobStrings[2]);
        Date dobDate = new Date(dobMonth, dobDay, dobYear);
        Profile patient = new Profile(fname, lname, dobDate);

        if(dobYear < 1900){
            out.appendText("Patient dob: " + dobDate.toString() + " is not a valid calendar date.\n");
            return;
        }
        if(!dobDate.isValidBirth()){
            out.appendText("Patient dob: " + dobDate.toString() + " is today or a date after today.\n");
            return;
        }

        String imagingType = separated_data[5];
        if(!(imagingType.toUpperCase().equals(Radiology.CATSCAN.toString()) ||
                imagingType.toUpperCase().equals(Radiology.ULTRASOUND.toString()) ||
                imagingType.toUpperCase().equals(Radiology.XRAY.toString()))){
            out.appendText(imagingType + " - imaging service not provided.\n");
            return;
        }

        Radiology room = Radiology.valueOf(imagingType.toUpperCase());

        Provider provider = null;
        if(appointments.isEmpty()){
            provider = technicians.get(0);
        } else {
            for(Provider tech : technicians) {
                boolean isBooked = false;
                for (Appointment app : appointments) {
                    if ((app.getClass() == Imaging.class && app.getProvider().equals(tech) && app.getTimeslot().equals(timeslot) && app.getDate().equals(appointmentDate))) {
                        isBooked = true;
                        break;
                    } else if (app.getClass() == Imaging.class && ((Imaging) app).getRoom().equals(room) && app.getProvider().getLocation().equals(tech.getLocation()) && app.getTimeslot().equals(timeslot) && app.getDate().equals(appointmentDate)) {
                        isBooked = true;
                        break;
                    }
                }
                if (!isBooked) {
                    provider = tech;
                    break;
                }
            }
        }

        if(provider==null){
            out.appendText("Cannot find an available technician at all locations for " + imagingType.toUpperCase() + " at slot" + timeslotString + ".\n");
            return;
        }

        Imaging appointment = new Imaging(appointmentDate, timeslot, patient, provider, room);

        if(appointment.appointmentValid(appointment, appointments) != null){
            System.out.println(appointment.appointmentValid(appointment, appointments));
            return;
        }

        Technician tech = (Technician) provider;

        if(providerBooked(timeslot, appointment)){
            out.appendText("[" + tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase() +
                    " " + tech.getProfile().getDob().toString() + ", " +
                    tech.getLocation().toString() + ", " + tech.getLocation().countyString() + " " + tech.getLocation().getZip() + "][" +
                    "rate: $" + df.format(tech.rate()) + "] is not available at slot " + timeslotString + ".\n");
            return;
        }

        appointments.add(appointment);
        addToMedicalRecord(patient, appointment);
        out.appendText(appointment.getDate().toString() + " " + timeslot.toString() + " " + patient.getFname() + " " + patient.getLname() + " " + patient.getDob().toString() + " " +
                "[" + tech.getProfile().getFname().toUpperCase() + " " + tech.getProfile().getLname().toUpperCase() +
                " " + tech.getProfile().getDob().toString() + ", " +
                tech.getLocation().toString() + ", " + tech.getLocation().countyString() + " " + tech.getLocation().getZip() + "][" +
                "rate: $" + df.format(tech.rate()) + "][" + appointment.getRoom() + "] booked.\n");
        Provider toStart = technicians.get((technicians.indexOf(provider)+1)%technicians.size());
        technicians.setStartIdx(toStart);
    }

    /**
     * @param separated_data an array of Strings containing data pulled from various buttons, text fields, and drop-downs
     * Helper method to remove an appointment from the appointments List, if valid
     */
    private void cancelAppointment(String[] separated_data) {
        try {
            String[] dateStrings = separated_data[0].split("-");
            int year = Integer.parseInt(dateStrings[0]);
            int month = Integer.parseInt(dateStrings[1]);
            int day = Integer.parseInt(dateStrings[2]);
            Date appointmentDate = new Date(month, day, year);

            String timeslotString = separated_data[1].split("[()]+")[1].trim();
            Timeslot timeslot = timeslots.get(Integer.parseInt(timeslotString) - 1);


            String fname = separated_data[2];
            String lname = separated_data[3];
            String[] dobStrings = separated_data[4].split("-");
            int dobYear = Integer.parseInt(dobStrings[0]);
            int dobMonth = Integer.parseInt(dobStrings[1]);
            int dobDay = Integer.parseInt(dobStrings[2]);
            Date dobDate = new Date(dobMonth, dobDay, dobYear);
            Profile patient = new Profile(fname, lname, dobDate);

            Appointment appointment = null;

            for(Appointment app : appointments){
                if(app.getDate().equals(appointmentDate) && app.getTimeslot().equals(timeslot) && app.getProfile().equals(patient)){
                    appointment = app;
                }
            }

            if(appointment==null){
                out.appendText(appointmentDate.toString() + " " + timeslot.toString() + " " +
                        fname + " " + lname + " " + dobDate.toString() + " - appointment does not exist.\n");
                return;
            }

            if (appointments.contains(appointment)) {
                appointments.remove(appointment);
                removePatientVisit(patient, appointment);
                out.appendText(appointment.getDate().toString() + " " + appointment.getTimeslot().toString() + " " +
                        fname + " " + lname + " " + dobDate.toString() + " - appointment has been canceled.\n");
            } else
                out.appendText(appointment.getDate().toString() + " " + appointment.getTimeslot().toString() + " " +
                        fname + " " + lname + " " + dobDate.toString() + " - appointment does not exist.\n");
        } catch (Exception e){
            out.appendText("Missing data tokens.\n");
        }
    }

    /**
     * @param patient The associated Profile of the appointment being removed
     * @param appointment The appointment to be removed from the record
     * Helper method to remove a Visit from the record when cancelling an appointment
     */
    private void removePatientVisit(Profile patient, Appointment appointment){
        Patient patientObj = new Patient(patient);
        int patientIndex = record.indexOf(patientObj);
        record.get(patientIndex).remove(appointment);
    }

    /**
     * @param separated_data an array of Strings containing data pulled from various buttons, text fields, and drop-downs
     * Helper method to reschedule an appointment on the same day
     */
    private void rescheduleAppointment(String[] separated_data) {

        try {
            String[] dateStrings = separated_data[0].split("-");
            int year = Integer.parseInt(dateStrings[0]);
            int month = Integer.parseInt(dateStrings[1]);
            int day = Integer.parseInt(dateStrings[2]);
            Date appointmentDate = new Date(month, day, year);

            String timeslotString = separated_data[1].split("[()]+")[1].trim();

            Timeslot timeslot = timeslots.get(Integer.parseInt(timeslotString) - 1);


            String fname = separated_data[2];
            String lname = separated_data[3];
            String[] dobStrings = separated_data[4].split("-");
            int dobYear = Integer.parseInt(dobStrings[0]);
            int dobMonth = Integer.parseInt(dobStrings[1]);
            int dobDay = Integer.parseInt(dobStrings[2]);
            Date dobDate = new Date(dobMonth, dobDay, dobYear);
            Profile patient = new Profile(fname, lname, dobDate);

            Appointment appointment = null;

            for (Appointment app : appointments) {
                if (app.getDate().equals(appointmentDate) && app.getTimeslot().equals(timeslot) && app.getProfile().equals(patient)) {
                    appointment = app;
                }
            }

            if(appointment==null){
                out.appendText(appointmentDate.toString() + " " + timeslot.toString() + " " +
                        fname + " " + lname + " " + dobDate.toString() + " does not exist.\n");
                return;
            }

            if (!appointments.contains(appointment)) {
                out.appendText(appointment.getDate().toString() + " " + appointment.getTimeslot().toString() + " " +
                        fname + " " + lname + " " + dobDate.toString() + " does not exist.\n");
                return;
            }

            Appointment oldAppointment = appointments.get(appointments.indexOf(appointment));
            if (oldAppointment == null) {
                out.appendText(appointmentDate + " " + timeslot.toString() + " " + fname + " " + lname + " " + dobDate.toString() + " does not exist.\n");
                return;
            }

            String newTimeslotString = separated_data[5].split("[()]+")[1].trim();

            Timeslot newTimeslot = timeslots.get(Integer.parseInt(newTimeslotString) - 1);

            Appointment newAppointment = null;
            if(oldAppointment.getClass()==Imaging.class){
                newAppointment = new Imaging(appointmentDate, newTimeslot, patient, oldAppointment.getProvider(), ((Imaging) oldAppointment).getRoom());
            } else {
                newAppointment = new Appointment(appointmentDate, newTimeslot, patient, oldAppointment.getProvider());
            }

            for (Appointment app : appointments) {
                if (newAppointment.getProvider().equals(app.getProvider()) && newAppointment.getTimeslot().equals(app.getTimeslot()) && newAppointment.getDate().equals(app.getDate())) {
                    out.appendText(app.getProfile().getFname() + " " + app.getProfile().getLname() + " " + app.getProfile().getDob().toString() +
                            " has an existing appointment at " + app.getDate().toString() + " " + app.getTimeslot().toString() + "\n");
                    return;
                }
            }

            Provider provider = oldAppointment.getProvider();

            if (appointments.contains(oldAppointment)) {
                appointments.remove(oldAppointment);
                appointments.add(newAppointment);
                out.appendText("Rescheduled to " + newAppointment.getDate().toString() + " " + newAppointment.getTimeslot().toString() + " " +
                        fname + " " + lname + " " + dobDate.toString() + " " + newAppointment.getProvider().toString() + "\n");
            } else {
                out.appendText(oldAppointment.getDate().toString() + " " + oldAppointment.getTimeslot().toString() + " " +
                        fname + " " + lname + " " + dobDate.toString() + " does not exist.\n");
            }
        } catch (Exception e){
            out.appendText("Missing data tokens.\n");
        }
    }

    /**
     * @param appointments a List of appointments to print
     * Helper method print the Appointments to the output TextArea at the bottom
     */
    private void printAppointments(List<Appointment> appointments){
        if(appointments.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }
        for(int i = 0; i< appointments.size(); i++){
            String date = appointments.get(i).getDate().toString();
            String time = appointments.get(i).getTimeslot().toString();
            String fname = appointments.get(i).getProfile().getFname();
            String lname = appointments.get(i).getProfile().getLname();
            String dob = appointments.get(i).getProfile().getDob().toString();
            if(appointments.get(i).getProvider().getClass()==Doctor.class){
                out.appendText(date + " " + time + " " + fname + " " + lname + " " + dob +
                        " " + ((Doctor)(appointments.get(i).getProvider())).toString() + "\n");
            } else {
                out.appendText(date + " " + time + " " + fname + " " + lname + " " + dob +
                        " " + ((Technician)(appointments.get(i).getProvider())).toString() + "\n");
            }
        }
    }

    /**
     * @param providers A List of Providers to print the credits of
     * Helper method to print the Provider credit amounts
     */
    private void printCredits(List<Provider> providers) {
        if(appointments.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }
        out.appendText("\n** Credit amount ordered by provider. **\n");

        int[] providerCredits = new int[providers.size()];
        for(int i = 0; i < providerCredits.length; i++){
            providerCredits[i] = 0;
        }

        for(int i = 0; i < appointments.size(); i++){
            if(appointments.get(i).getClass()== Imaging.class) {
                Provider provider = appointments.get(i).getProvider();
                int providerIndex = providers.indexOf(provider);

                if (providerIndex != -1) {
                    providerCredits[providerIndex] += provider.rate();
                }
            } else {
                Provider provider = appointments.get(i).getProvider();
                int providerIndex = providers.indexOf(provider);
                if(providerIndex!=-1){
                    providerCredits[providerIndex] += ((Doctor)provider).getSpecialty().getCharge();
                }
            }
        }

        for(int i = 0; i < providers.size(); i++){
            out.appendText("(" + (i+1) + ") " + providers.get(i).getProfile().getFname().toUpperCase() + " " +
                    providers.get(i).getProfile().getLname() + " " + providers.get(i).getProfile().getDob().toString() +
                    " [credit amount: $" + df.format(providerCredits[i]) + "]\n");
        }

        out.appendText("** end of list **\n");
    }

    /**
     * Helper method to print the billing statement
     */
    private void printStatements(){
        if(record.isEmpty()){
            out.appendText("Schedule calendar is empty.\n");
            return;
        }

        Sort.patient(record);

        out.appendText("\n** Billing statement ordered by patient **\n");
        int count = 0;
        for(Patient p : record){
            int charge = p.charge();
            String fname = p.getProfile().getFname();
            String lname = p.getProfile().getLname();
            String dobString = p.getProfile().getDob().toString();
            out.appendText("(" + (count++) + ") " + fname + " " + lname + " " + dobString + " [amount due: $" + df.format(charge) + "]\n");
        }
        out.appendText("** end of list **\n");
    }

}