Sample usage of gmail api

# Task
There was simple email subscription service, which sent emails with email address of subscribers in body

It is known, that service sent mails from gmail.com and there is only one email in body included

Prepare simple app, which will:
1) Let login with special google oath2 popup
2) Download all mails from inbox, specified by query
3) Extract emails and date from message body to console output

# Steps
1) Follow [how to auth](https://medium.com/@pablo127/google-api-authentication-with-oauth-2-on-the-example-of-gmail-a103c897fd98)
2) After step 1 fill in client_secrets.json from resources folder
3) Prepare executable jar and ship it to client
4) After launch from console with 'java -jar', client will be redirected to page for login.
5) After successful login emails with dates will be listed.
6) Also folder .store will be created near .jar - for repeating start process without login if needed. 
After - it can be deleted.