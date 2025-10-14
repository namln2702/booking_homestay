Of course. Here is the Markdown document in English, adapted to your project's scope where personal emails are acceptable for creating Admin accounts.

---

# **Strategy & Workflow: Admin Account Creation by Super Admin**

### 🎯 **Mission**
To provide a **secure, controlled, and auditable process** for provisioning administrator access, ensuring that only authorized individuals can access high-level management functions of the system.

---

### **⚙️ Detailed Workflow**
This process involves sequential actions performed by the Super Admin, the System, and the new Admin.

#### **Step 1: Initiation (Offline Process)**
* The decision to create a new Admin account originates from a team or project management decision, not a technical request within the system.
* A designated Super Admin is tasked with creating the account.

#### **Step 2: Super Admin Actions**
1.  **Access:** The Super Admin logs into the highest-level Admin Panel.
2.  **Navigate:** They go to the "Account Management" -> "Admin Accounts" section.
3.  **Initiate Creation:** They click the "Create New Admin" or "Invite Admin" button.
4.  **Input Information:** The Super Admin fills out a simple form with the new Admin's essential details:
    * **Full Name**.
    * **Admin's Email Address**. This is the most critical field. For this project's scope, it can be the team member's personal email, but it must be valid and unique.
    * **NOTE:** The form **must not** have a password field. The Super Admin never sets or knows the new Admin's password.
5.  **Confirm:** The Super Admin submits the form by clicking "Send Invitation" or "Create Account".

#### **Step 3: Automated System Actions**
1.  **Create Account Stub:** The system creates a new user record with the `ADMIN` role but sets its status to **`PENDING`** or **`INACTIVE`**. This account cannot be used for login yet.
2.  **Generate Secure Token:** The system generates a cryptographically secure, **single-use activation token** with a limited lifespan (e.g., expires in 24 hours).
3.  **Send Invitation Email:** An automated email is sent to the address provided by the Super Admin. This email contains a unique activation link embedded with the secure token.
4.  **Create Audit Log:** The system records an entry in the audit log, such as: *"Super Admin [Super Admin's Name] invited [New Admin's Email] as an Admin on [Timestamp]"*.

#### **Step 4: New Admin Activation**
1.  **Receive Email:** The new Admin receives the invitation email.
2.  **Click Activation Link:** They click the unique, time-sensitive link.
3.  **Set Password:** The link directs them to a secure page where they are required to set their own password. The form should include a password confirmation field.
4.  **Complete Activation:** After setting a password, they click "Activate Account".

#### **Step 5: Completion and Status Update**
1.  The system validates the token from the link and saves the new Admin's hashed password.
2.  The account's status is updated in the database from `PENDING` to **`ACTIVE`**.
3.  The system redirects the new Admin to the login page, where they can now sign in with their email and newly created password.

---

### **🔒 Strategic & Security Principles**

* **Never Set Passwords for Others:** This is a fundamental security principle. The user must be the only one who creates and knows their password, minimizing the risk of credential exposure.
* **Email-Based Activation:** This workflow verifies that the invited individual has control over the specified email address, confirming their identity before granting access.
* **Use One-Time, Time-Limited Links:** This prevents the misuse of old or exposed invitation links. Once used or expired, the link becomes invalid.
* **Audit All Privileged Actions:** Every high-privilege action, especially account creation, must be logged. This creates an immutable record for accountability and security analysis.
* **Email as the Unique Identifier:** While corporate domains are a best practice in enterprise environments, for this project, any valid and unique email serves as the primary identifier for login and the secure channel for the activation process.

---

## Implemented API Support

- `POST /admins/invite` — Super Admin triggers the invitation flow by providing the target admin's name, email, and optional level. The system issues a pending admin record, stores a 24-hour activation code, emails the invite, and returns the code in the API payload for non-production testing.
- `POST /admins/activate` — Invited admin submits their email, activation code, and optional profile details to finalize the account. The system validates the token and promotes the admin status to active.
