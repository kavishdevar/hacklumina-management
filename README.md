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

### Features
- **Dashboard**: View and manage different sections of the event.
- **Registrations**: View, manage, and update participant registrations.
- **Mailing Lists**: View and manage mailing lists, send emails to participants.
- **NFC Check-in**: Check-in participants using NFC tags.

## Contributing
1. Fork the repository.
2. Create a new branch:
    ```sh
    git checkout -b feature-branch
    ```
3. Make your changes and commit them:
    ```sh
    git commit -m "Description of changes"
    ```
4. Push to the branch:
    ```sh
    git push origin feature-branch
    ```
5. Open a pull request.

## Contact
For any inquiries, please contact the project maintainer at [mail@kavishdevar.me].

# LICENSE

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