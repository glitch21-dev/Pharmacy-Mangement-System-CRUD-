💊 Pharmacy Management System

A GUI-based Pharmacy Management System built with Java Swing. This application allows you to manage medicines, customers, and sales efficiently with an intuitive graphical interface. All data is securely stored in an SQLite database.

✨ Features
🧾 Medicine Management

➕ Add new medicines to the database

✏️ Update existing medicine details (name, price, quantity)

❌ Delete medicines that are no longer available

🔍 Search for medicines by name or ID

👥 Customer Management

➕ Add new customer details

✏️ Update existing customer information

❌ Delete customer records

🔍 Search for customers by name or ID

💰 Sales Management

🛒 Record sales transactions

💵 Calculate total cost and generate receipts

📉 Update inventory automatically after a sale

📊 Track daily sales

📦 Inventory Tracking

📦 Monitor stock levels of all medicines

⚠️ Alert when medicine quantity is low

📋 View all available medicines and their details

🗄️ Database Integration

💾 Uses SQLite for persistent storage

🔧 Easy to modify and extend database tables

🔒 Securely stores all data locally in a single database file

🚀 How to Run the Pharmacy Management System
Step 1: Add SQLite JDBC Driver to Project

Method A: Add JAR to Project (Recommended)

Right-click on your project in the Project Explorer

Select "Open Module Settings" (or press F4)

Go to "Libraries" in the left panel

Click the "+" button → "Java"

Navigate to your SQLite JAR file (sqlite-jdbc-3.50.3.0.jar)

Select it and click "OK"

Click "Apply" and "OK"

Step 2: Compile and Run (Command Line Recommended)

Compile:

javac -cp ".;sqlite-jdbc-3.50.3.0.jar" src/PharmacyManagementSystem.java


Run:

java -cp ".;sqlite-jdbc-3.50.3.0.jar" src/PharmacyManagementSystem

🛠️ Requirements

Java JDK 8 or above

SQLite JDBC Driver (sqlite-jdbc-3.50.3.0.jar)

IntelliJ IDEA or any Java IDE (optional, for easier project management)

⚠️ Notes

Make sure the SQLite JDBC JAR file is correctly added to your project or referenced in the classpath

This system is designed for small-scale pharmacy management and educational purposes

<img width="1920" height="1080" alt="Screenshot (152)" src="https://github.com/user-attachments/assets/77d56e0a-6e6f-4514-9545-78bffda70134" />

<img width="1920" height="1080" alt="Screenshot (154)" src="https://github.com/user-attachments/assets/82768bd8-4e32-4bb7-a330-b3bb6b477dc1" />
<img width="1920" height="1080" alt="Screenshot (153)" src="https://github.com/user-attachments/assets/b6a9355f-bdf0-49b1-be54-dc9269d5cb03" />
<img width="1293" height="794" alt="Screenshot 2025-08-29 234007" src="https://github.com/user-attachments/assets/cb976fd4-d2a3-4ae3-afcd-9053e04617c9" />
<img width="1273" height="792" alt="Screenshot 2025-08-29 233948" src="https://github.com/user-attachments/assets/390f4a1c-1127-4323-a1dc-a38c1c43f65d" />
<img width="1319" height="863" alt="Screenshot 2025-08-29 233918" src="https://github.com/user-attachments/assets/896c992a-7f96-4093-abb6-e3a99c594ebe" />
