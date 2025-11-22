Android_EZcredit
---

## MVVM Architecture

This project follows the **MVVM (Model-View-ViewModel)** architecture for a clean and maintainable code structure.  

![MVVM Architecture](./docs/MVVM.png)  


---

## Download APK

You can download the latest APK of the project here:  
[Download APK](https://1drv.ms/u/c/a2e50b6392b4123e/ESVSOct-CntGnYsJraKw_H0BttE0P8dyy7iV7ycy_cjO4w?e=ZJbyXA)  
---

## [Source Code](https://github.com/DakshArora07/Android_EZcredit)


## Demonstration of some of the project components
1. **Customers**  
   - Displays a list of customers along with their **credit scores**.  
   - Provides quick access to customer details and their transaction history.

2. **Invoices**  
   - Shows all invoices generated for customers.  
   - Includes invoice details, status (paid/unpaid), and due dates.

3. **Calendar**  
   - Displays a calendar view of scheduled tasks, events, or payments.  
   - Helps track deadlines and appointments efficiently.

4. **Analytics**  
   - Provides insights and analytics on customer activity, payments, and trends.  
   - Includes graphs and charts for easy visualization.

---

## Milestones
1. **OCR (Optical Character Recognition) Setup**
   - Implemented OCR functionality to extract text from images.
   - Configured text extraction logic and ensured accurate parsing of recognized text for further use within the app.

2. **Gemini Integration**
   - Implemented logic for Gemini to determine whether an invoice is past its due date.
   - If an invoice is overdue, Gemini generates an appropriate alert or message to notify the user.

3. **Room Database setup**
   - Established a Room database for data storage and offline accessibility.
   - Created the necessary Entity, DAO, and Repository classes to manage invoice data efficiently in the later stages.
   - Implemented a clean architecture to maintain clear separation between data, domain, and presentation layers.

4. **UI Development**
   - Built UI screens for OCR scanning and viewing results.
   - Integrated the Calendar view in the UI to display invoices by date.

5. **Project Structure**
   - Organized code following MVVM architecture with ViewModel and utility classes for data handling.
   - Added utility and helper classes to support OCR, gemini integration, and date handling.
   - Maintained clear package organization for easier navigation and scalability.


## Wokrload Distribution

Show and Tell 1 Contributions

- **Ayush Arora** – Contributed to UI development and app navigation, designing layouts and implementing smooth transitions between key sections of the application.

- **Daksh Arora** – Set up the Room Database, including entities, DAO, and repository classes, to enable efficient storage and retrieval of invoice/cusomer data.
  
- **Gurshan Singh Aulakh** – Focussed on Gemini integration in the app, enabling the app to send prompts to Gemini and handle the returned results.

- **Hetmay Ketan Vora** – Worked on UI design, navigation flow, and Calendar functionality, implementing the calendar feature and displaying the selected date when a day is clicked.

- **Henry Nguyen** – Developed the OCR functionality, enabling the extraction of text from invoice images captured through the camera.
  
Future Workload Management

- **Ayush Arora** - Will focus on developing the UI for customer and settings screens, including the login functionality, ensuring a smooth and intuitive user experience.
  
- **Daksh Arora** - Will finalize the Room Database setup and integrate Firebase for cloud storage and synchronization, ensuring reliable data persistence.
  
- **Gurshan Singh Aulakh** - Will implement the credit score algorithm and set up automated reminders using Gemini to notify users about overdue or important invoices.
  
- **Hetmay Ketan Vora** - Will work on the UI for analytics and Calendar features, integrating the calendar with app data to allow users to view and manage invoices by date.
  
- **Henry Nguyen** - Will complete the OCR functionality for hand-written invoices and complete the ui for invoice screen to add invoices through OCR and manually.




