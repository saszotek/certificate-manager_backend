# Certificate Manager - Backend

**This repository is backend part of a database system supporting the management of expiring certificates.**\
**The description below describes only the backend part.**\
This is a REST API that is integrated with [certificate-manager_frontend](https://github.com/saszotek/certificate-manager_frontend) repository.
System allows you to manage expiring qualified certificates. When validity of the certificate reaches its end, the document status is automatically set to Expired. The application supports notification system that sends e-mail to registered users and people entitled to the certificate when:
- validity period of the certificate ends in 60, 30, 14, 7 days,
- certificate status has been changed,
- certificate validity has been extended,
- certificate has expired.

The system supports import and export of people and its certificates in supported file formats:
- .txt (formatted in a xml schema),
- .csv,
- .eml.

## Live version

**Live demo:** *offline*

## Technologies

- Spring Boot
- Quartz Scheduler
- JSON Web Token
- MySQL
