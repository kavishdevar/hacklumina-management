# Hacklumina '25 Websites

## Demo Video

[![Demo video thumbnail](http://img.youtube.com/vi/_g0AEOZ1Crc/0.jpg)](http://www.youtube.com/watch?v=_g0AEOZ1Crc "Hacklumina website demo")

# Visitor's Website

The visitor's website for Hacklumina '25 provides information about the event, including schedules, highlights, and contact details. Visit it at [https://hacklumina.tech](https://hacklumina.tech)

## Features
- **Event Details**: Provides detailed information about the event, including the venue, time, and schedule.
- **Highlights**: Lists the key highlights of the event, such as workshops, rewards, etc.
- **Run of Show**: Displays the schedule for each day of the event.
- **Sponsors**: Showcases the event sponsors.
- **Contact Us**: Provides contact information for the event organizers.
- **Sign Up for Updates**: Allows visitors to sign up for email updates about the event. This sends a POST request to the [Hacklumina Management System at `https://admin.hacklumina.tech/subscribe`](#subscribe-to-mailing-list)

# Hacklumina Management System

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
6. Create a `.env` file and add the necessary environment variables:
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

### Login
- **Route**: `/login`
- **Method**: `GET`, `POST`
- **Description**: Handles user login. Sets a cookie if the password is correct.

### Email Template Preview
- **Route**: `/email-template-preview`
- **Method**: `GET`
- **Description**: Returns a preview of the email template with placeholder content. Used in `/send-email`.

### Send Email
- **Route**: `/send-email`
- **Method**: `GET`, `POST`
- **Description**: Handles sending emails. Uses Mailgun API to send emails.

### Subscribe to Mailing List
- **Route**: `/subscribe`
- **Method**: `POST`
- **Description**: Adds an email to the mailing list using Mailgun API. Used by the [main website](https://hacklumina.tech).

### Mailing Lists
- **Route**: `/mailing-lists`
- **Method**: `GET`
- **Description**: Displays mailing lists and their members. Uses Mailgun API to fetch data.

### Form
- **Route**: `/form/<fillout-id-or-custom-name-as-defined-in-main.py>`
- **Method**: `GET`
- **Description**: Displays a form based on the provided ID or a custom name.

### Form Submission
- **Route**: `/form-submission`
- **Method**: `POST`
- **Description**: Handles form submissions and sends an email with the form data.

### Register
- **Route**: `/register`
- **Method**: `GET`, `POST`
- **Description**: Handles participant registration. Stores data in Airtable and sends confirmation emails.

### Registrations
- **Route**: `/registrations`
- **Method**: `GET`
- **Description**: Displays a list of registrations, with filters. Allows updating registration status.

### Update Registration Status
- **Route**: `/update-registration-status/<id>`
- **Method**: `POST`
- **Description**: Updates the registration status of a participant.
- 
### NFC Check-in
- **Route**: `/nfc-checkin`
- **Method**: `GET`, `POST`
- **Description**: Handles NFC check-ins.

### Logout
- **Route**: `/logout`
- **Method**: `GET`
- **Description**: Logs out the user by deleting the authentication cookie.

## Android App
The Hacklumina Management System also includes an Android app for managing NFC check-ins.

### Features
- **NFC Check-in**: Allows users to check-in participants using NFC tags.
- **Registrations**: View and manage participant registrations.

### Installation
1. Open the project in Android Studio.
2. Build and run the app on an Android device.

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
