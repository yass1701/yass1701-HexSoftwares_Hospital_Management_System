# Hospital Management System 🏥

### Internship Project for Hex Softwares

This is a full-stack Desktop Application built to manage hospital patient records. It features a graphical user interface (GUI) built with **Java Swing** and connects to a **PostgreSQL** database for permanent data storage.

## 🚀 Features
* **Add Patients:** Input details (Name, Age, Gender) with duplicate checking.
* **View Records:** See all patients in a clean data table.
* **Update & Delete:** Modify or remove patient records by ID.
* **Search:** Instantly find patients by name.
* **Reset Database:** A "Nuclear" option to wipe all data and reset IDs to 1.
* **Data Validation:** Prevents invalid inputs (e.g., text in the Age field).

## 🛠️ Tech Stack
* **Language:** Java (JDK 21)
* **Interface:** Java Swing (GUI)
* **Database:** PostgreSQL 15
* **Connectivity:** JDBC (Java Database Connectivity)

## 📂 Project Structure
* `HospitalSwingApp.java` - The main source code file containing GUI and Logic.
* `postgresql-42.7.2-all.jar` - The JDBC Driver to connect Java to PostgreSQL.

## ⚙️ Setup & Installation

### 1. Database Setup (PostgreSQL)
Open your SQL Shell or pgAdmin and run these commands:

```sql
-- Create the database
CREATE DATABASE hospital_db;

-- Connect to the database
\c hospital_db

-- Create the table
CREATE TABLE patients (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    gender VARCHAR(20)
);






2. Run the Application
Open your terminal in the project folder and run these commands:
javac -cp ".;postgresql-42.7.2-all.jar" HospitalSwingApp.java
then run
java -cp ".;postgresql-42.7.2-all.jar" HospitalSwingApp
