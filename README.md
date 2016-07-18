# acronis-email-retrieval

### Requirements
- Java 1.7 or greater
- Gradle 2.3 or greater
- Access to the internet and a web browser
- A Google account with Gmail enabled

###Setup
Step 1: Turn on the Gmail API

1. Use [this wizard](https://console.developers.google.com/start/api?id=gmail) to create or select a project in the Google Developers Console and automatically turn on the API. Click **Continue**, then **Go to credentials**.
2. At the top of the page, select the **OAuth consent screen** tab. Select an **Email address**, enter a **Product name** if not already set, and click the **Save** button.
3. Select the **Credentials** tab, click the **Create credentials** button and select **OAuth client ID**.
4. Select the application type **Other**, enter the name "Email Backup", and click the **Create** button.
5. Click **OK** to dismiss the resulting dialog.
6. Click the **file_download** (Download JSON) button to the right of the client ID.
7. Move this file to `src/main/resources/` and rename it `client_secret.json`.

###Running Application
At project folder, run with `gradle -q run`
To login to the application, it will prompt you to authorize access:

1. The project will attempt to open to open a new window or tab in your default browser. If this fails, copy the URL from the console and manually open it in your browser.
2. If you are not already logged into your Google account, you will be prompted to log in. If you are logged into multiple Google accounts, you will be asked to select one account to use for the authorization.
Click the Accept button.
3. The project will proceed automatically, and you may close the window/tab.
4. Running of application in the future will skip this authorization step unless you `logout`.

###Commands
* `get <start date> <end date>` - Downloads and encrypts all emails between `<start date>` and `<end date>` inclusive. Date format must be in yyyy/mm/dd.
* `decrypt` - Decrypts all downloaded and encrypted emails and stores them as files in the same folder
* `decrypt <id>` - Decrypts downloaded and encrypted email with `<id>` and stores it as a file in the same folder
* `logout` - Revokes and deletes refresh token for Google OAuth 2.0 and stops application
* `exit` - Stops application



