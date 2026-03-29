package com.hospital.appointment.ui;

import com.hospital.appointment.domain.Appointment;
import com.hospital.appointment.domain.Department;
import com.hospital.appointment.domain.Doctor;
import com.hospital.appointment.domain.Patient;
import com.hospital.appointment.exception.HospitalException;
import com.hospital.appointment.persistence.CsvDoctorStore;
import com.hospital.appointment.persistence.CsvPatientStore;
import com.hospital.appointment.repository.FileBackedAppointmentRepository;
import com.hospital.appointment.service.HospitalRegistry;
import com.hospital.appointment.service.SchedulingService;
import com.hospital.appointment.util.IdGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class HospitalAppointmentGui extends JFrame {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String[] APPOINTMENT_COLUMNS = {"ID", "Patient", "Doctor", "Start", "End", "Status"};

    private final SchedulingService scheduling;
    private final HospitalRegistry registry;
    private final FileBackedAppointmentRepository repository;
    private final IdGenerator doctorIdGenerator;
    private final IdGenerator patientIdGenerator;
    private final Path doctorsFile;
    private final Path patientsFile;

    private final JComboBox<Patient> patientBookingCombo;
    private final JComboBox<Doctor> doctorBookingCombo;
    private final JTextField startDateTimeField;
    private final JComboBox<Doctor> doctorFilterCombo;
    private final JComboBox<Patient> patientFilterCombo;
    private final JComboBox<Department> doctorDepartmentCombo;
    private final JTextField doctorNameField;
    private final JTextField doctorEmailField;
    private final JTextField doctorLicenseField;
    private final JTextField patientNameField;
    private final JTextField patientEmailField;
    private final JTextField patientPhoneField;
    private final DefaultTableModel appointmentTableModel;
    private final JTable appointmentTable;
    private final JLabel statusLabel;

    public HospitalAppointmentGui(
            SchedulingService scheduling,
            HospitalRegistry registry,
            FileBackedAppointmentRepository repository,
            IdGenerator doctorIdGenerator,
            IdGenerator patientIdGenerator,
            Path doctorsFile,
            Path patientsFile) {
        super("Hospital Appointment System");
        this.scheduling = Objects.requireNonNull(scheduling, "scheduling");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.doctorIdGenerator = Objects.requireNonNull(doctorIdGenerator, "doctorIdGenerator");
        this.patientIdGenerator = Objects.requireNonNull(patientIdGenerator, "patientIdGenerator");
        this.doctorsFile = Objects.requireNonNull(doctorsFile, "doctorsFile");
        this.patientsFile = Objects.requireNonNull(patientsFile, "patientsFile");

        patientBookingCombo = new JComboBox<>();
        doctorBookingCombo = new JComboBox<>();
        doctorFilterCombo = new JComboBox<>();
        patientFilterCombo = new JComboBox<>();
        doctorDepartmentCombo = new JComboBox<>(Department.values());
        doctorNameField = new JTextField(22);
        doctorEmailField = new JTextField(22);
        doctorLicenseField = new JTextField(22);
        patientNameField = new JTextField(22);
        patientEmailField = new JTextField(22);
        patientPhoneField = new JTextField(22);
        startDateTimeField = new JTextField(22);
        appointmentTableModel = new DefaultTableModel(APPOINTMENT_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(appointmentTableModel);
        statusLabel = new JLabel("Ready.");

        initComponents();
        refreshSelectors();
        refreshAppointmentTable();
    }

    private void initComponents() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        JTabbedPane tabs = new JTabbedPane();
        JPanel appointmentsTab = new JPanel(new BorderLayout(8, 8));
        appointmentsTab.add(createBookingPanel(), BorderLayout.NORTH);
        appointmentsTab.add(createAppointmentPanel(), BorderLayout.CENTER);
        tabs.addTab("Appointments", appointmentsTab);
        tabs.addTab("Registration", createRegistrationPanel());

        add(tabs, BorderLayout.CENTER);
        add(createStatusPanel(), BorderLayout.SOUTH);

        setSize(980, 700);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindow();
            }
        });
    }

    private JPanel createBookingPanel() {
        JPanel bookingPanel = new JPanel(new BorderLayout(8, 8));
        bookingPanel.setBorder(BorderFactory.createTitledBorder("Book Appointment"));

        JPanel form = new JPanel(new GridLayout(2, 4, 12, 12));
        form.add(new JLabel("Patient:"));
        form.add(patientBookingCombo);
        form.add(new JLabel("Doctor:"));
        form.add(doctorBookingCombo);
        form.add(new JLabel("Start (ISO date-time):"));
        startDateTimeField.setText(LocalDateTime.now().withMinute(0).withSecond(0).withNano(0).plusHours(1).format(DATE_TIME_FORMATTER));
        form.add(startDateTimeField);
        form.add(new JLabel("Slot length:"));
        form.add(new JLabel(scheduling.getSlotMinutes() + " minutes"));

        configureUserCombo(patientBookingCombo, "Select patient");
        configureUserCombo(doctorBookingCombo, "Select doctor");

        JButton bookButton = new JButton("Book Appointment");
        bookButton.addActionListener(e -> handleBook());

        bookingPanel.add(form, BorderLayout.CENTER);
        bookingPanel.add(bookButton, BorderLayout.EAST);
        return bookingPanel;
    }

    private JPanel createAppointmentPanel() {
        JPanel container = new JPanel(new BorderLayout(8, 8));
        container.setBorder(BorderFactory.createTitledBorder("Appointments"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 4));
        filterPanel.add(new JLabel("Filter by doctor:"));
        configureUserCombo(doctorFilterCombo, "All doctors");
        doctorFilterCombo.addActionListener(e -> refreshAppointmentTable());
        filterPanel.add(doctorFilterCombo);

        filterPanel.add(new JLabel("Filter by patient:"));
        configureUserCombo(patientFilterCombo, "All patients");
        patientFilterCombo.addActionListener(e -> refreshAppointmentTable());
        filterPanel.add(patientFilterCombo);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshAppointmentTable());
        filterPanel.add(refreshButton);

        JButton cancelButton = new JButton("Cancel Selected");
        cancelButton.addActionListener(e -> handleChangeStatus(false));
        filterPanel.add(cancelButton);

        JButton completeButton = new JButton("Complete Selected");
        completeButton.addActionListener(e -> handleChangeStatus(true));
        filterPanel.add(completeButton);

        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setAutoCreateRowSorter(true);
        appointmentTable.setFillsViewportHeight(true);

        container.add(filterPanel, BorderLayout.NORTH);
        container.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);
        return container;
    }

    private JPanel createRegistrationPanel() {
        JPanel registrationPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        registrationPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        registrationPanel.add(createDoctorRegistrationPanel());
        registrationPanel.add(createPatientRegistrationPanel());
        return registrationPanel;
    }

    private JPanel createDoctorRegistrationPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Register Doctor"));

        panel.add(new JLabel("Full name:"));
        panel.add(doctorNameField);
        panel.add(new JLabel("Email:"));
        panel.add(doctorEmailField);
        panel.add(new JLabel("Department:"));
        panel.add(doctorDepartmentCombo);
        panel.add(new JLabel("License ID:"));
        panel.add(doctorLicenseField);
        panel.add(new JLabel());
        JButton registerDoctorButton = new JButton("Register Doctor");
        registerDoctorButton.addActionListener(e -> handleRegisterDoctor());
        panel.add(registerDoctorButton);
        panel.add(new JLabel());
        panel.add(new JLabel());
        return panel;
    }

    private JPanel createPatientRegistrationPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Register Patient"));

        panel.add(new JLabel("Full name:"));
        panel.add(patientNameField);
        panel.add(new JLabel("Email:"));
        panel.add(patientEmailField);
        panel.add(new JLabel("Phone:"));
        panel.add(patientPhoneField);
        panel.add(new JLabel());
        JButton registerPatientButton = new JButton("Register Patient");
        registerPatientButton.addActionListener(e -> handleRegisterPatient());
        panel.add(registerPatientButton);
        panel.add(new JLabel());
        panel.add(new JLabel());
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        return statusPanel;
    }

    private void handleRegisterDoctor() {
        String name = doctorNameField.getText().trim();
        String email = doctorEmailField.getText().trim();
        Department department = (Department) doctorDepartmentCombo.getSelectedItem();
        String licenseId = doctorLicenseField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || department == null || licenseId.isEmpty()) {
            showError("Please complete all doctor fields before registering.");
            return;
        }

        try {
            long id = doctorIdGenerator.nextId();
            Doctor doctor = new Doctor(id, name, email, department, licenseId);
            registry.registerDoctor(doctor);
            CsvDoctorStore.writeAll(doctorsFile, registry.listDoctors());
            refreshSelectors();
            showInfo("Doctor registered: " + doctor.getFullName() + " (" + doctor.getId() + ").");
            doctorNameField.setText("");
            doctorEmailField.setText("");
            doctorLicenseField.setText("");
        } catch (Exception ex) {
            showError("Failed to register doctor: " + ex.getMessage());
        }
    }

    private void handleRegisterPatient() {
        String name = patientNameField.getText().trim();
        String email = patientEmailField.getText().trim();
        String phone = patientPhoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showError("Please complete all patient fields before registering.");
            return;
        }

        try {
            long id = patientIdGenerator.nextId();
            Patient patient = new Patient(id, name, email, phone);
            registry.registerPatient(patient);
            CsvPatientStore.writeAll(patientsFile, registry.listPatients());
            refreshSelectors();
            showInfo("Patient registered: " + patient.getFullName() + " (" + patient.getId() + ").");
            patientNameField.setText("");
            patientEmailField.setText("");
            patientPhoneField.setText("");
        } catch (Exception ex) {
            showError("Failed to register patient: " + ex.getMessage());
        }
    }

    private <T> void configureUserCombo(JComboBox<T> comboBox, String emptyLabel) {
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText(emptyLabel);
                } else if (value instanceof Patient patient) {
                    setText(patient.getFullName() + " (" + patient.getId() + ")");
                } else if (value instanceof Doctor doctor) {
                    setText(doctor.getFullName() + " (" + doctor.getId() + ")");
                } else {
                    setText(value.toString());
                }
                return this;
            }
        });
    }

    private void refreshSelectors() {
        patientBookingCombo.removeAllItems();
        doctorBookingCombo.removeAllItems();
        doctorFilterCombo.removeAllItems();
        patientFilterCombo.removeAllItems();

        doctorFilterCombo.addItem(null);
        patientFilterCombo.addItem(null);

        for (Patient patient : registry.listPatients()) {
            patientBookingCombo.addItem(patient);
            patientFilterCombo.addItem(patient);
        }
        for (Doctor doctor : registry.listDoctors()) {
            doctorBookingCombo.addItem(doctor);
            doctorFilterCombo.addItem(doctor);
        }

        if (doctorBookingCombo.getItemCount() > 0) {
            doctorBookingCombo.setSelectedIndex(0);
        }
        if (patientBookingCombo.getItemCount() > 0) {
            patientBookingCombo.setSelectedIndex(0);
        }
    }

    private void refreshAppointmentTable() {
        appointmentTableModel.setRowCount(0);
        List<Appointment> appointments = new ArrayList<>(repository.findAll());
        appointments.sort(Comparator.comparing(Appointment::getStart));

        Doctor selectedDoctor = (Doctor) doctorFilterCombo.getSelectedItem();
        Patient selectedPatient = (Patient) patientFilterCombo.getSelectedItem();

        for (Appointment appointment : appointments) {
            if (selectedDoctor != null && appointment.getDoctorId() != selectedDoctor.getId()) {
                continue;
            }
            if (selectedPatient != null && appointment.getPatientId() != selectedPatient.getId()) {
                continue;
            }
            String patientName = registry.requirePatient(appointment.getPatientId()).getFullName();
            String doctorName = registry.requireDoctor(appointment.getDoctorId()).getFullName();
            appointmentTableModel.addRow(new Object[]{
                    appointment.getId(),
                    patientName,
                    doctorName,
                    appointment.getStart().format(DATE_TIME_FORMATTER),
                    appointment.getEnd().format(DATE_TIME_FORMATTER),
                    appointment.getStatus().name()
            });
        }
        statusLabel.setText("Loaded " + appointmentTableModel.getRowCount() + " appointments.");
    }

    private void handleBook() {
        Patient patient = (Patient) patientBookingCombo.getSelectedItem();
        Doctor doctor = (Doctor) doctorBookingCombo.getSelectedItem();
        if (patient == null || doctor == null) {
            showError("Please select both a patient and a doctor before booking.");
            return;
        }
        LocalDateTime start;
        try {
            start = LocalDateTime.parse(startDateTimeField.getText().trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            showError("Invalid date-time format. Use ISO_LOCAL_DATE_TIME, for example 2026-03-30T10:00.");
            return;
        }

        try {
            Appointment appointment = scheduling.book(patient.getId(), doctor.getId(), start);
            refreshAppointmentTable();
            showInfo("Booked appointment " + appointment.getId() + " for " + patient.getFullName() + " with " + doctor.getFullName() + ".");
        } catch (HospitalException ex) {
            showError("Unable to book appointment: " + ex.getMessage());
        }
    }

    private void handleChangeStatus(boolean complete) {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select an appointment from the table first.");
            return;
        }
        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        long appointmentId = Long.parseLong(appointmentTableModel.getValueAt(modelRow, 0).toString());

        try {
            Appointment updated = complete ? scheduling.complete(appointmentId) : scheduling.cancel(appointmentId);
            refreshAppointmentTable();
            showInfo("Appointment " + updated.getId() + " marked " + updated.getStatus().name().toLowerCase() + ".");
        } catch (HospitalException ex) {
            showError(ex.getMessage());
        }
    }

    private void closeWindow() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to save changes and exit?",
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            repository.shutdown();
        } catch (Exception ex) {
            System.err.println("Failed to save appointments on exit: " + ex.getMessage());
        }
        dispose();
        System.exit(0);
    }

    private void showError(String message) {
        statusLabel.setText("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
