package com.hospital.appointment;

import com.hospital.appointment.domain.Appointment;
import com.hospital.appointment.domain.Department;
import com.hospital.appointment.domain.Doctor;
import com.hospital.appointment.domain.Patient;
import com.hospital.appointment.domain.User;
import com.hospital.appointment.exception.HospitalException;
import com.hospital.appointment.persistence.CsvDoctorStore;
import com.hospital.appointment.persistence.CsvPatientStore;
import com.hospital.appointment.repository.FileBackedAppointmentRepository;
import com.hospital.appointment.service.DefaultSchedulingService;
import com.hospital.appointment.service.HospitalRegistry;
import com.hospital.appointment.service.SchedulingService;
import com.hospital.appointment.ui.HospitalAppointmentGui;
import com.hospital.appointment.util.IdGenerator;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Console front-end for the Hospital Appointment System (OOP coursework demo).
 */
public final class HospitalAppointmentApp {

    private static final int SLOT_MINUTES = 30;

    private HospitalAppointmentApp() {
    }

    public static void main(String[] args) {
        Path appointmentFile = Path.of("data", "appointments.csv");
        Path doctorsFile = Path.of("data", "doctors.csv");
        Path patientsFile = Path.of("data", "patients.csv");
        FileBackedAppointmentRepository repository = new FileBackedAppointmentRepository(appointmentFile);
        boolean started = false;
        try {
            HospitalRegistry registry = new HospitalRegistry();

            boolean hasDoctors = loadDoctors(registry, doctorsFile);
            boolean hasPatients = loadPatients(registry, patientsFile);
            if (!hasDoctors) {
                seedDemoDoctors(registry);
                CsvDoctorStore.writeAll(doctorsFile, registry.listDoctors());
            }
            if (!hasPatients) {
                seedDemoPatients(registry);
                CsvPatientStore.writeAll(patientsFile, registry.listPatients());
            }

            long maxApptId = repository.findAll().stream()
                    .mapToLong(Appointment::getId)
                    .max()
                    .orElse(0L);
            long appointmentIdSeed = Math.max(maxApptId, 1000L);
            IdGenerator appointmentIds = new IdGenerator(appointmentIdSeed);

            long maxDoctorId = registry.listDoctors().stream()
                    .mapToLong(Doctor::getId)
                    .max()
                    .orElse(0L);
            long maxPatientId = registry.listPatients().stream()
                    .mapToLong(Patient::getId)
                    .max()
                    .orElse(0L);
            IdGenerator doctorIds = new IdGenerator(maxDoctorId);
            IdGenerator patientIds = new IdGenerator(maxPatientId);

            SchedulingService scheduling = new DefaultSchedulingService(
                    repository,
                    registry,
                    appointmentIds,
                    SLOT_MINUTES);

            HospitalAppointmentGui gui = new HospitalAppointmentGui(
                    scheduling,
                    registry,
                    repository,
                    doctorIds,
                    patientIds,
                    doctorsFile,
                    patientsFile);
            SwingUtilities.invokeLater(() -> gui.setVisible(true));
            started = true;
        } catch (Exception e) {
            System.err.println("Startup failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!started) {
                repository.shutdown();
            }
        }
    }

    private static boolean loadDoctors(HospitalRegistry registry, Path doctorsFile) {
        try {
            CsvDoctorStore.readAll(doctorsFile).forEach(registry::registerDoctor);
            return registry.listDoctors().size() > 0;
        } catch (Exception e) {
            throw new HospitalException("Failed to load doctors", e);
        }
    }

    private static boolean loadPatients(HospitalRegistry registry, Path patientsFile) {
        try {
            CsvPatientStore.readAll(patientsFile).forEach(registry::registerPatient);
            return registry.listPatients().size() > 0;
        } catch (Exception e) {
            throw new HospitalException("Failed to load patients", e);
        }
    }

    private static void seedDemoDoctors(HospitalRegistry registry) {
        registry.registerDoctor(new Doctor(
                1L,
                "Dr. Ananya Rao",
                "ananya.rao@hospital.demo",
                Department.CARDIOLOGY,
                "MD-IND-1001"));
        registry.registerDoctor(new Doctor(
                2L,
                "Dr. Vikram Singh",
                "vikram.singh@hospital.demo",
                Department.GENERAL,
                "MD-IND-2002"));
    }

    private static void seedDemoPatients(HospitalRegistry registry) {
        registry.registerPatient(new Patient(
                101L,
                "Meera Iyer",
                "meera.iyer@email.demo",
                "+91-9000000001"));
        registry.registerPatient(new Patient(
                102L,
                "Arjun Nair",
                "arjun.nair@email.demo",
                "+91-9000000002"));
    }

    private static void runMenu(SchedulingService scheduling, HospitalRegistry registry, Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("=== Hospital Appointment System ===");
            System.out.println("1) List doctors");
            System.out.println("2) List patients");
            System.out.println("3) Book appointment");
            System.out.println("4) Appointments for a doctor");
            System.out.println("5) Appointments for a patient");
            System.out.println("6) Cancel appointment");
            System.out.println("7) Mark appointment completed");
            System.out.println("8) Demo: show polymorphic user descriptions");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            String line = scanner.nextLine().trim();
            try {
                switch (line) {
                    case "1" -> printDoctors(registry);
                    case "2" -> printPatients(registry);
                    case "3" -> bookFlow(scheduling, registry, scanner);
                    case "4" -> listDoctorFlow(scheduling, registry, scanner);
                    case "5" -> listPatientFlow(scheduling, registry, scanner);
                    case "6" -> cancelFlow(scheduling, scanner);
                    case "7" -> completeFlow(scheduling, scanner);
                    case "8" -> demoPolymorphism(registry);
                    case "0" -> {
                        System.out.println("Goodbye.");
                        return;
                    }
                    default -> System.out.println("Unknown option.");
                }
            } catch (HospitalException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void demoPolymorphism(HospitalRegistry registry) {
        System.out.println("-- Polymorphism: same code, different User behaviour --");
        registry.listDoctors().forEach(HospitalAppointmentApp::printUserLine);
        registry.listPatients().forEach(HospitalAppointmentApp::printUserLine);
    }

    private static void printUserLine(User u) {
        System.out.println(u.getFullName() + " | " + u.getRoleLabel() + " | " + u.getContactSummary());
    }

    private static void printDoctors(HospitalRegistry registry) {
        registry.listDoctors().forEach(d ->
                System.out.println("ID " + d.getId() + " — " + d.getFullName() + " — " + d.getDepartment().getDisplayName()));
    }

    private static void printPatients(HospitalRegistry registry) {
        registry.listPatients().forEach(p ->
                System.out.println("ID " + p.getId() + " — " + p.getFullName() + " — " + p.getPhone()));
    }

    private static void bookFlow(SchedulingService scheduling, HospitalRegistry registry, Scanner scanner) {
        printPatients(registry);
        System.out.print("Patient id: ");
        long patientId = Long.parseLong(scanner.nextLine().trim());
        printDoctors(registry);
        System.out.print("Doctor id: ");
        long doctorId = Long.parseLong(scanner.nextLine().trim());
        System.out.print("Start date-time (ISO, e.g. 2026-03-30T10:00): ");
        LocalDateTime start = LocalDateTime.parse(scanner.nextLine().trim());
        Appointment a = scheduling.book(patientId, doctorId, start);
        Patient p = registry.requirePatient(patientId);
        Doctor d = registry.requireDoctor(doctorId);
        System.out.println("Booked id " + a.getId() + " | " + SLOT_MINUTES + " min slot | " + a.getStart() + " → " + a.getEnd());
        System.out.println("Notify patient: " + p.getContactSummary());
        System.out.println("Notify doctor: " + d.getContactSummary());
    }

    private static void listDoctorFlow(SchedulingService scheduling, HospitalRegistry registry, Scanner scanner) {
        printDoctors(registry);
        System.out.print("Doctor id: ");
        long doctorId = Long.parseLong(scanner.nextLine().trim());
        scheduling.listForDoctor(doctorId).forEach(HospitalAppointmentApp::printAppointment);
    }

    private static void listPatientFlow(SchedulingService scheduling, HospitalRegistry registry, Scanner scanner) {
        printPatients(registry);
        System.out.print("Patient id: ");
        long patientId = Long.parseLong(scanner.nextLine().trim());
        scheduling.listForPatient(patientId).forEach(HospitalAppointmentApp::printAppointment);
    }

    private static void cancelFlow(SchedulingService scheduling, Scanner scanner) {
        System.out.print("Appointment id: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        Appointment a = scheduling.cancel(id);
        System.out.println("Cancelled: " + a.getId() + " status=" + a.getStatus());
    }

    private static void completeFlow(SchedulingService scheduling, Scanner scanner) {
        System.out.print("Appointment id: ");
        long id = Long.parseLong(scanner.nextLine().trim());
        Appointment a = scheduling.complete(id);
        System.out.println("Completed: " + a.getId() + " status=" + a.getStatus());
    }

    private static void printAppointment(Appointment a) {
        System.out.println(
                "id=" + a.getId()
                        + " patient=" + a.getPatientId()
                        + " doctor=" + a.getDoctorId()
                        + " " + a.getStart() + "–" + a.getEnd()
                        + " " + a.getStatus());
    }
}
