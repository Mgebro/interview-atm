# ATM Simulation CLI

## Overview
This project is a simple ATM simulation implemented in Kotlin with Spring Boot and a command-line interface (CLI) using Spring Shell. It allows users to perform basic banking operations such as depositing, withdrawing, transferring money, and managing debts.

## Setup & Run
### Prerequisites
- Java 21
- Gradle
- Bash

### Build & Run
```sh
chmod +x start.sh
./start.sh
```

## Commands
- **Login:** `login <username>`
- **Logout:** `logout`
- **Deposit:** `deposit <amount>`
- **Withdraw:** `withdraw <amount>`
- **Transfer:** `transfer <recipient_username> <amount>`
- **View Debts:** `debts`
