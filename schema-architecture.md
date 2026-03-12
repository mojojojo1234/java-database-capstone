Architecture summary: The architecture of "Smart Clinic Management System" is robust, multi layered and balances structured and flexible data. At the UI level, Thymeleaf which generates dynamic HTML is used for Admin and Doctor dashboards, whereas REST API is used for all the other clients. The application's data layer is divided into MySQL (for patient, doctor, appointment, and admin data) and MongoDB (for prescriptions). Service layer is sort of like the brain, which applies business rules & handles entity coordination. This interaction of the application and the repositories is done by controllers which rout through the service layer. MySQL is used for structured data such as various role entities and it uses JPA entities while MongoDB is used for its flexible data capabilities, which is used for document based information like medical prescription and it uses document models

Numbered flow of data and control: 
1. User interacts with either dynamic Admin/Doctor dashboard or REST Modules to send a request
2. Controllers recieve the request and routed to either Thymeleaf or REST controller
3. Service layer when called, processes the request by applying business logic
4. Correct data layer is chosen based on wheter the request requires handling(fetching or storing) structured or flexible dat
5. Information is either fetched or saved in structured(MySQL) or flexible(MongoDB) data management layer
6. Relational Model is mapped to real world Java based objects that the application can understand, using annotations
7. Data is organized into respective entitites and output is delivered to the UI
