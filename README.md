# 🌟 EZCredit
### Customer & Credit Management System  
*Android • Kotlin • Firebase • WorkManager • Stripe • OCR*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-blueviolet?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Backend-orange?logo=firebase&logoColor=white)](https://firebase.google.com/)
[![WorkManager](https://img.shields.io/badge/WorkManager-Background-green?logo=android&logoColor=white)](https://developer.android.com/topic/libraries/architecture/workmanager)
[![Stripe](https://img.shields.io/badge/Stripe-Payments-0A2540?logo=stripe&logoColor=white)](https://stripe.com/)
[![OCR](https://img.shields.io/badge/OCR-Gemini-FFBB33?logo=google&logoColor=white)](https://developers.google.com/)

---

## 🚀 Overview
EZCredit is a modern *Android business management app* designed to help small businesses **manage their financial operations efficiently**. Its core features include:  
•⁠ Customer credit tracking  
•⁠ OCR-powered invoice data extraction  
•⁠ Stripe payments & receipt matching  
•⁠ Automated email reminders & overdue notifications  
•⁠ Invoice creation and management
•⁠ Filter Customers and Invoices with ease

Built using *MVVM architecture, **Jetpack Compose, **WorkManager, and **Firebase*, EZCredit provides a complete financial workflow from Sales to daily credit updates.

---

## 🔮 OCR
•⁠  ⁠Instant invoice data extraction with *OCR*  
•⁠  ⁠Autofill invoice fields from images or camera  

## 🤖 Automated
•⁠  ⁠5 background workers handle:
  - AI powered Email reminders  
  - Credit score updates  
  - Overdue invoices  
  - Paid/Late payment tracking  
  - Daily summaries  

## 📊 Insightful
•⁠  ⁠Dashboards & charts  
•⁠  ⁠Calendar view for invoices & payments  
•⁠  ⁠Customer credit history & analytics  

---

# ✨ Key Features

## 📄 Smart Invoice Management
•⁠  ⁠Auto status updates: *Unpaid → PastDue → Paid → LatePayment*  
•⁠  ⁠PDF invoice generation  
•⁠  ⁠Stripe checkout & receipt matching  
•⁠  ⁠Customer credit updates  
•⁠  ⁠Group invoices by customer  

## 🧾 OCR Invoice Extraction
•⁠  ⁠Detect invoice *Amount, date, Customer*  
•⁠  ⁠Autofill invoice form instantly  
•⁠  ⁠Supports camera & image upload  

## 👤 Customer Management
•⁠  ⁠Add, edit, delete customers  
•⁠  ⁠Track daily credit score changes  
•⁠  ⁠Full payment history  

## 📅 Calendar View
•⁠  ⁠Visual invoice/payment timelines  
•⁠  ⁠Daily summaries & color-coded statuses  

## 📊 Analytics Dashboard
•⁠  ⁠Total receivables  
•⁠  ⁠Past-due trends  
•⁠  ⁠Daily collection summaries  
•⁠  ⁠Customer credit performance  

---

# ⚙️ Background Automation (5 Workers)
| Worker | Function |
|--------|---------|
| *Auto Email Reminder* | Sends daily payment reminders via Mailgun |
| *Credit Score Update* | Recalculates customer credit scores |
| *Overdue Invoice* | Marks invoices as PastDue |
| *Paid / Late Payment* | Matches receipts & updates invoice status |
| *Daily Summary* | Sends notifications with summary of invoices & credit changes |

---

# 🧰 Tech Stack

## Frontend
| Category | Technology |
|---------|------------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| State Management | StateFlow + Coroutines |
| Networking | OkHttp |
| Background Tasks | WorkManager |
| PDF | iText / File Provider |
| OCR | Gemini Messages |

## Backend & Cloud
| Category | Technology |
|---------|------------|
| Authentication | Firebase Auth |
| Database | Firestore (NoSQL) |
| Storage | Firebase Storage And Room Database |
| Email | Mailgun API |
| Payments | Stripe API |
| AI | Google Gemini 1.5 Flash |

---

 🏗 Architecture & Project Structure

# EZCredit App Architecture

```text
┌─────────────────────────────────────┐
│ UI Layer (Jetpack Compose)          │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ StateFlow (State Management)        │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ ViewModel Layer                     │
└──────────────┬──────────────────────┘
               │ suspend functions
               ▼
┌─────────────────────────────────────┐
│ Repository Layer                     │
└──────────────┬──────────────────────┘
               │ IO Dispatcher
               ▼
┌─────────────────────────────────────┐
│ External Services                    │
│ Firebase • Stripe • Mailgun          │
└─────────────────────────────────────┘
```

## 📁 MVVM Architecture
![MVVM Architecture](./docs/MVVM.png)

## 📁 Threaded Architecture
![Threads Diagram](./docs/Threads.png)


## 📁 Project Structure

```
EZCredit/
├── data/
│   ├── dao/           # 5 classes (data access objects)
│   ├── entity/        # Entity classes
│   ├── repository/    # 5 classes (repositories)
│   └── api/           # External API integrations (Mailgun, Stripe)
├── ui/
│   ├── screens/       # Compose screens
│   ├── viewmodel/     # 12 ViewModel classes
│   ├── components/    # Reusable UI components
│   └── theme/         # 3 theme classes (Material 3)
├── workers/           # 5 WorkManager workers
└── utils/             # Helpers (OCR, date utils, etc.)
```

# 👥 Team
| Developer | Role |
|-----------|------|
| Ayush Arora | UI, WorkManager logic, credit system |
| Daksh Arora | Database architecture, Firebase sync |
| Gurshan Singh Aulakh | Invoice & customer UI Screens, Automatic Email Background Worker, PDF Invoice Generation |
| Hetmay Vora | Calendar & analytics |
| Henry Nguyen | OCR engine & invoice formatting |

---

# 📜 License
MIT License © 2025 EZCredit Development Team

---

# ⭐ Support
If you find this project helpful, *please give it a star!*
