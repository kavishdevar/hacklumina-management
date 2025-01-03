# Hacklumina Management System

## Description
Hacklumina Management System is a web-based application designed to manage various aspects of the Hacklumina event, including registrations, mailing lists, and NFC check-ins.

## Installation
1. Clone the repository:
    ```sh
    git clone https://github.com/kavishdevar/hacklumina-management.git
    ```
2. Navigate to the project directory:
    ```sh
    cd hacklumina-management
    ```
3. Create a virtual environment:
    ```sh
    python -m venv venv
    ```
4. Activate the virtual environment:
    - On Windows:
        ```sh
        venv\scripts\activate
        ```
    - On macOS/Linux:
        ```sh
        source venv/bin/activate
        ```
5. Install the required dependencies:
    ```sh
    pip install -r requirements.txt
    ```
6. Create a [.env](http://_vscodecontentref_/1) file and add the necessary environment variables:
    ```env
    MAILGUN_API_KEY=your-mailgun-api-key
    MAILGUN_DOMAIN=your-mailgun-domain
    LIST_ADDRESS=your-mailing-list-address
    PASSWORD=your-password-hash
    AIRTABLE_PAT=your-airtable-pat
    ```

## Usage
1. Run the application:
    ```sh
    python main.py
    ```
2. Open your web browser and navigate to `http://localhost:8080`.

## Features

### Dashboard
- **Route**: `/`
- **Method**: `GET`
- **Description**: Displays the admin dashboard. Redirects to the login page if the user is not authenticated.
- **HTML File**: [dashboard.html](http://_vscodecontentref_/2)
- **Screenshot**:
    !Dashboard

### Login
- **Route**: `/login`
- **Method**: `GET`, `POST`
- **Description**: Handles user login. Sets a cookie if the password is correct.
- **HTML File**: [login.html](http://_vscodecontentref_/3)
- **Screenshot**:
    !Login

### Email Template Preview
- **Route**: `/email-template-preview`
- **Method**: `GET`
- **Description**: Returns a preview of the email template with placeholder content.
- **HTML File**: [email-template.html](http://_vscodecontentref_/4)
- **Screenshot**:
    ![Email Template Preview](screenshots/email-template-preview.png)

### Send Email
- **Route**: `/send-email`
- **Method**: `GET`, `POST`
- **Description**: Handles sending emails. Uses Mailgun API to send emails.
- **HTML File**: [send-email.html](http://_vscodecontentref_/5)
- **Screenshot**:
    ![Send Email](screenshots/send-email.png)

### Subscribe to Mailing List
- **Route**: `/subscribe`
- **Method**: `POST`
- **Description**: Adds an email to the mailing list using Mailgun API.

### Mailing Lists
- **Route**: `/mailing-lists`
- **Method**: `GET`
- **Description**: Displays mailing lists and their members. Uses Mailgun API to fetch data.
- **HTML File**: [mailing-lists.html](http://_vscodecontentref_/6)
- **Screenshot**:
    ![Mailing Lists](screenshots/mailing-lists.png)

### Form
- **Route**: `/form/<id>`
- **Method**: `GET`
- **Description**: Displays a form based on the provided ID.
- **HTML File**: [form.html](http://_vscodecontentref_/7)
- **Screenshot**:
    !Form

### Form Submission
- **Route**: `/form-submission`
- **Method**: `POST`
- **Description**: Handles form submissions and sends an email with the form data.

### Register
- **Route**: `/register`
- **Method**: `GET`, `POST`
- **Description**: Handles participant registration. Stores data in Airtable and sends confirmation emails.
- **HTML File**: [register.html](http://_vscodecontentref_/8)
- **Screenshot**:
    !Register

### Registrations
- **Route**: `/registrations`
- **Method**: `GET`
- **Description**: Displays a list of registrations. Uses Airtable API to fetch data.
- **HTML File**: [registrations.html](http://_vscodecontentref_/9)
- **Screenshot**:
    !Registrations

### Update Registration Status
- **Route**: `/update-registration-status/<id>`
- **Method**: `POST`
- **Description**: Updates the registration status of a participant. Uses Airtable API to update data.

### NFC Check-in
- **Route**: `/nfc-checkin`
- **Method**: `GET`, `POST`
- **Description**: Handles NFC check-ins. Uses Airtable API to update check-in status.
- **HTML File**: [nfc-checkin.html](http://_vscodecontentref_/10)
- **Screenshot**:
    ![NFC Check-in](screenshots/nfc-checkin.png)

### Logout
- **Route**: `/logout`
- **Method**: `GET`
- **Description**: Logs out the user by deleting the authentication cookie.

## HTML Files
- **login.html**: Login page.
- **send-email.html**: Send email page.
- **email-template.html**: Email template used for sending emails.
- **dashboard.html**: Admin dashboard.
- **mailing-lists.html**: Mailing lists page.
- **form.html**: Form page.
- **register.html**: Registration page.
- **registrations.html**: Registrations page.
- **nfc-checkin.html**: NFC check-in page.

## Static Files
- **login.css**: Styles for the login page.
- **send-email.css**: Styles for the send email page.
- **dashboard.css**: Styles for the dashboard page.
- **mailing-lists.css**: Styles for the mailing lists page.
- **index.css**: Common styles for the application.
- **form.css**: Styles for the registration form.
- **registrations.css**: Styles for the registrations page.
- **nfc-checkin.css**: Styles for the NFC check-in page.
- **registrations.js**: JavaScript for the registrations page.
- **nfc-checkin.js**: JavaScript for the NFC check-in page.

## Android App
The Hacklumina Management System also includes an Android app for managing NFC check-ins.

### Features
- **NFC Check-in**: Allows users to check-in participants using NFC tags.
- **Registrations**: View and manage participant registrations.

### Installation
1. Open the project in Android Studio.
2. Build and run the app on an Android device.

### Screenshots
- **NFC Check-in**:
    ![NFC Check-in](screenshots/android-nfc-checkin.png)
- **Registrations**:
    ![Registrations](screenshots/android-registrations.png)

## Contact
For any inquiries, please contact the project maintainer at [mail@kavishdevar.me].

## License
Hacklumina's management system
Copyright (C) 2024 Kavish Devar

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.