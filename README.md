[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/pG3gvzt-)
# PCCCS495 – Term II Project

## Project Title

Hospital Appointment Management System

## Problem Statement (max 150 words)

Hospitals and clinics require a reliable way to manage doctor schedules, patient registration, and appointment bookings. Manual scheduling often causes double-bookings, missed appointments, and inefficient patient flow. This Java desktop application provides a simple appointment management system with booking validation, cancellation, completion, and CSV-based persistence to reduce scheduling errors and improve administrative efficiency.

## Target User

- Hospital receptionists and front desk staff
- Clinic administrators
- Doctors who need to manage their appointment schedules
- Patients who need to book or track appointments

## Core Features

- Doctor registration and patient registration with unique IDs
- Appointment booking with validation against overlapping doctor schedules
- List appointments for a doctor or a patient
- Cancel and complete appointments
- Persistent storage using CSV files for doctors, patients, and appointments
- Swing-based desktop graphical user interface

---

## OOP Concepts Used

- Abstraction: interfaces such as `SchedulingService` and `AppointmentRepository` separate behavior from implementation
- Inheritance: `Doctor` and `Patient` share common behavior through the `User` base type
- Polymorphism: different `User` types provide role-specific behavior and display logic
- Exception Handling: custom exceptions like `HospitalException`, `SlotUnavailableException`, and `ValidationException` manage error conditions
- Collections / Threads: in-memory collections (`Map`, `List`) store registry and appointment data; Swing UI runs on the event dispatch thread

---

## Proposed Architecture Description

The application follows a layered architecture:

- Presentation Layer: `HospitalAppointmentGui` provides the desktop UI and user interaction.
- Service Layer: `DefaultSchedulingService` enforces booking rules, validation, and status changes.
- Registry Layer: `HospitalRegistry` maintains in-memory doctor and patient records.
- Repository Layer: `FileBackedAppointmentRepository` stores appointments persistently.
- Persistence Layer: `CsvDoctorStore` and `CsvPatientStore` read and write CSV files.
- Domain Layer: models such as `Appointment`, `Doctor`, `Patient`, and `AppointmentStatus` represent core entities.
- Utility Layer: `IdGenerator` ensures unique sequential IDs for new entities.

Startup is handled by `HospitalAppointmentApp`, which loads CSV data, seeds demo records when needed, creates service and repository objects, and launches the GUI.

---

## How to Run

1. Open PowerShell in `term-ii-project-submission-owl2103/src`
2. Build the application:
   - `.uild.ps1`
3. Run the application:
   - `.
un.ps1`

If running manually:

- `javac -encoding UTF-8 -d out src\main\java\com\hospital\appointment\**\*.java`
- `java -cp out com.hospital.appointment.HospitalAppointmentApp`

Ensure you have Java installed and the `java`/`javac` commands available in your PATH.

---

## Git Discipline Notes
Minimum 10 meaningful commits required.
