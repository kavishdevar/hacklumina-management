from flask import Flask, request, render_template_string, redirect, url_for, jsonify, render_template
import requests
import json
import hashlib
import dotenv
import os

app = Flask(__name__)

dotenv.load_dotenv()
MAILGUN_API_KEY = os.getenv("MAILGUN_API_KEY")
MAILGUN_DOMAIN = os.getenv("MAILGUN_DOMAIN")
LIST_ADDRESS = os.getenv("LIST_ADDRESS")
PASSWORD = os.getenv("PASSWORD")

@app.route("/login", methods=["GET", "POST"])
def login():
    if request.cookies.get("password") == PASSWORD:
        return redirect(url_for("dashboard"))

    if request.method == "POST":
        if hashlib.sha256(request.form.get("password").encode()).hexdigest() == PASSWORD:
            response = redirect(url_for("dashboard"))
            response.set_cookie("password", PASSWORD)
            return response
        else:
            return render_template("login.html", error="Invalid password"), 403
    return render_template("login.html")


@app.route("/email-template-preview")
def email_template_preview():
    return render_template("email-template.html", content="\\{\\{content\\}\\}")

import threading

def send_email_async(from_addr, to_addr, subject, content):
    if to_addr == "announcements-dev@hacklumina.tech":
        response = requests.get(
            f"https://api.eu.mailgun.net/v3/lists/{LIST_ADDRESS}/members",
            auth=("api", MAILGUN_API_KEY)
        )
        members = response.json().get("items", [])
        recipient_variables = {
            member["address"]: {"to": member["address"]}
            for member in members if member.get("subscribed", False)
        }

        send_response = requests.post(
            f"https://api.eu.mailgun.net/v3/{MAILGUN_DOMAIN}/messages",
            auth=("api", MAILGUN_API_KEY),
            data={
                "from": f"Hacklumina {from_addr}",
                "to": to_addr,
                "subject": subject,
                "html": content,
                "recipient-variables": json.dumps(recipient_variables),
            }
        )
        if send_response.status_code == 200:
            return "Email sent successfully!"
        else:
            return f"Failed to send email: {send_response.text}", 500
    else:
        send_response = requests.post(
            f"https://api.eu.mailgun.net/v3/{MAILGUN_DOMAIN}/messages",
            auth=("api", MAILGUN_API_KEY),
            data={
                "from": f"Hacklumina <{LIST_ADDRESS}>",
                "to": to_addr,
                "subject": subject,
                "html": content,
            }
        )
        if send_response.status_code == 200:
            print(send_response.text)
            return "Email sent successfully!"
        else:
            return f"Failed to send email: {send_response.text}", 500


@app.route("/send-email", methods=["GET", "POST"])
def send_email():
    if request.method == "POST":
        if request.cookies.get("password") != PASSWORD:
            return render_template("login.html", error="Invalid password"), 403
        from_addr = request.form.get("from")
        to_addr = request.form.get("to")
        subject = request.form.get("subject")
        content = request.form.get("html-content")
        threading.Thread(target=send_email_async, args=(from_addr, to_addr, subject, content)).start()
        return render_template("send-email.html", message="Email sent successfully!")
    if request.cookies.get("password") != PASSWORD:
        return render_template("login.html", error="Invalid password"), 403
    return render_template("send-email.html", to_addr=request.args.get("to", ""), from_addr=request.args.get("from", "contact@hacklumina.tech"))


@app.route("/subscribe", methods=["POST"])
def subscribe():
    data = request.get_json()
    email = data.get("email")

    if not email or not email.strip() or "@" not in email:
        print(f"Invalid email address: {email}")
        return jsonify({"error": "Invalid email address"}), 400

    try:
        response = requests.post(
            f"https://api.eu.mailgun.net/v3/lists/{LIST_ADDRESS}/members",
            auth=("api", MAILGUN_API_KEY),
            data={"address": email, "subscribed": "true"}
        )
        if response.status_code == 200:
            return jsonify({"message": "Successfully added to the mailing list!"})
        else:
            return jsonify({"error": "Failed to add email to the mailing list."}), response.status_code
    except Exception as e:
        return jsonify({"error": f"Internal server error: {str(e)}"}), 500
    
@app.route("/mailing-lists")
def mailing_lists():
    if request.cookies.get("password") != PASSWORD:
        return redirect(url_for("login"))
    response = requests.get(
        f"https://api.eu.mailgun.net/v3/lists",
        auth=("api", MAILGUN_API_KEY)
    )
    mLists = response.json().get("items", [])
    lists = []
    for mlist in mLists:
        response = requests.get(
            f"https://api.eu.mailgun.net/v3/lists/{mlist['address']}/members",
            auth=("api", MAILGUN_API_KEY)
        )
        members = response.json().get("items", [])
        lists.append({"name": mlist["name"], "address": mlist["address"], "members": members})
    return render_template("mailing-lists.html", lists = lists)

@app.route("/form/<id>")
def form(id):
    return render_template("form.html", id = id)

@app.route("/")
def dashboard():
    if request.cookies.get("password") != PASSWORD:
        return redirect(url_for("login"))
    return render_template("dashboard.html")    

@app.route('/form-submission', methods=['POST'])
def form_submission():
    data = request.get_json()
    print(data)
    if hashlib.sha256(str(data.get("auth")).encode()).hexdigest() != PASSWORD:
        return jsonify({"error": "Invalid auth"}), 403
    email = data.get("email")
    name = data.get("name")
    slackid = data.get("slackid")
    attachements = data.get("attachements")
    
    # Dynamically add other fields
    other_fields = ""
    for key, value in data.items():
        if key not in ["auth", "email", "name", "slackid", "attachements", "form"]:
            other_fields += f"<b>{key.capitalize()}:</b> {value}<br><br>"

    content = f"""
        <b>Form:</b> {data.get("form")}
        <br>
        <br>
        <b>Name:</b> {name}
        <br>
        <br>
        <b>Email:</b> {email}
        <br>
        <br>
        <b>Slack ID:</b> {slackid}
        <br>
        <b>Attachements:</b>
        <br><br>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 10px;">
            {"".join([f'<div><img src="{attachement["url"]}" alt="{attachement["filename"]}" style="width: 150px; object-fit: cover;"></div>' for attachement in attachements])}
        </div>
        <div>
        <b>Other stuff</b>
        </div>
        {other_fields}
    """
    html_content = requests.get("https://admin.hacklumina.tech/email-template-preview").text.replace("\\{\\{content\\}\\}", content)
    response = requests.post(
        f"https://api.eu.mailgun.net/v3/{MAILGUN_DOMAIN}/messages",
        auth=("api", MAILGUN_API_KEY),
        data={
            "from": f"Hacklumina Form Submission <forms@hacklumina.tech>",
            "to": "kavish@hacklumina.tech",
            "subject": "Form Submission from " + name,
            "html": html_content
        }
    )
    if response.status_code == 200:
        return jsonify({"message": "Form submission sent successfully!"})
    else:
        return jsonify({"error": "Failed to send form submission."}), response.status_code

from datetime import datetime
from pyairtable import Api

api = Api(api_key=os.getenv("AIRTABLE_PAT"))

def send_email_from_template_async(from_addr, to_addr, subject, content):
    template_html = requests.get("https://admin.hacklumina.tech/email-template-preview").text
    send_response = requests.post(
        f"https://api.eu.mailgun.net/v3/{MAILGUN_DOMAIN}/messages",
        auth=("api", MAILGUN_API_KEY),
        data={
            "from": from_addr,
            "to": to_addr,
            "subject": subject,
            "html": template_html.replace("\\{\\{content\\}\\}", content)
        }
    )
    if send_response.status_code == 200:
        print("Email sent successfully!")
    else:
        print(f"Failed to send email: {send_response.text}")

@app.route("/register", methods=["GET", "POST"])
def register():
    if request.method == "POST":
        print(request.json)
        participant_name = request.json.get("participant_name")
        email = request.json.get("email")
        contact_number = request.json.get("contact_number")
        allergies = request.json.get("allergies")
        food_preferences = request.json.get("food_preferences")
        tshirt_size = request.json.get("tshirt_size")
        emergency_contact_name = request.json.get("emergency_contact_name")
        emergency_contact_email = request.json.get("emergency_contact_email")
        emergency_contact_number = request.json.get("emergency_contact_number")
        
        table = api.bases()[0].table("Participants")
        
        data = {
            "Participant Name": participant_name,
            "Email": email,
            "Contact Number": contact_number,
            "Allergies": allergies,
            "Food Preferences": food_preferences,
            "T-Shirt Size": tshirt_size,
            "Emergency Contact Name": emergency_contact_name,
            "Emergency Contact Email": emergency_contact_email,
            "Emergency Contact Number": emergency_contact_number,
            "Registration Status": "Pending"
        }

        try:
            table.create(data)
            participant_subject = "Hacklumina - Registration Received!"
            participant_body = f"""
                <p>Hi {participant_name},</p>
                <p>Thank you for registering for Hacklumina '25!</p>
                <p>Your registration is currently pending approval. We will notify you once your registration has been approved. Stay tuned for more updates!</p>
            """
            participant_body=requests.get("https://admin.hacklumina.tech/email-template-preview").text.replace("\\{\\{content\\}\\}", participant_body)
            threading.Thread(target=send_email_async, args=("registrations@hacklumina.tech", email, participant_subject, participant_body)).start()
            
            contact_subject = "Hacklumina - New Registration Received"
            contact_body = f"""
                <p>Hi Kavish,</p>
                <p>A new registration received at {datetime.now().strftime("%d/%m/%Y %H:%M:%S")} and is currently pending approval.</p>
                <p>Participant Name: {data['Participant Name']}
                <br>Email: {data['Participant Name']}
                <br>Contact Number: {data['Contact Number']}</p>
            """
            contact_body=requests.get("https://admin.hacklumina.tech/email-template-preview").text.replace("\\{\\{content\\}\\}", contact_body)
            threading.Thread(target=send_email_async, args=("registrations@hacklumina.tech", "kavish@hacklumina.tech", contact_subject, contact_body)).start()
            
            return "Registration successful!"
        except Exception as e:
            return f"Failed to register: {e}", 500
    return render_template("register.html")

@app.route("/registrations")
def registrations():
    if request.cookies.get("password") != PASSWORD:
        return redirect(url_for("login"))
    table = api.bases()[0].table("Participants")
    registrations = table.all()
    return render_template("registrations.html", registrations=registrations)


@app.route("/update-registration-status/<id>", methods=["POST"])
def update_registration_status(id):
    if request.cookies.get("password") != PASSWORD:
        return jsonify({"success": False, "error": "Unauthorized"}), 403
    print(request.json)
    status = request.json.get("status")
    tag_id = request.json.get("tag-id")

    table = api.bases()[0].table("Participants")
    checkins_table = api.bases()[0].table("Check-ins")
    
    try:
        if status == "Confirmed":
            table.update(id, {"Registration Status": status})
            checkin_record = checkins_table.first(formula=f"{{Tag ID}} = '{tag_id}'")
            if not checkin_record:
                participant_record = table.get(id)
                checkins_table.create({
                    "Tag ID": tag_id,
                    "Participant": [id],
                })
        else:
            table.update(id, {"Registration Status": status})
            checkin_record = checkins_table.first(formula=f"{{Participant}} = '{id}'")
            if checkin_record:
                checkins_table.delete(checkin_record["id"])
        return jsonify({"success": True})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route("/nfc-checkin", methods=["GET", "POST"])
def nfc_checkin():
    if request.cookies.get("password") != PASSWORD:
        return redirect(url_for("login"))
    if request.method == "POST":
        tag_id = request.json.get("tag_id")
        field = request.json.get("field")
        table = api.bases()[0].table("Check-ins")
        try:
            record = table.first(formula=f"{{Tag ID}} = '{tag_id}'")
            if record:
                table.update(record['id'], {field: "Checked In"})
                return jsonify({"success": True})
            else:
                return jsonify({"success": False, "error": "Tag ID not found"}), 404
        except Exception as e:
            return jsonify({"success": False, "error": str(e)}), 500
    return render_template("nfc-checkin.html")

@app.route("/logout")
def logout():
    response = redirect(url_for("login"))
    response.delete_cookie("password")
    return response

if __name__ == "__main__":
    try:
        app.run(debug=True, host="0.0.0.0", port=8080)
    except Exception as e:
        print(f"Failed to start the server: {str(e)}")