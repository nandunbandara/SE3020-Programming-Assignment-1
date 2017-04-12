# SE3020-Programming-Assignment-1
## SE3020 - Distributed Systems - 3rd Year 1st Semester of SE specialization

### Problem Definition
Imagine that you have been asked to develop a weather monitoring system for mobile base-stations around the country. You will be using IOT sensors that can measure the average rainfall (mm), temperature (Celsius), humidity (%) and air pressure (psi) to monitor the weather of base-stations located around the country from different monitoring stations. The weather sensors do these measurements at five minute intervals. The weather sensors do have TCP/IP network ports but they can only perform basic socket communication to send data. The monitoring stations however do have PCs and laptops that have standard Windows or Linux based operating systems installed. A server machine is available with higher memory and processing speed to deploy a server.   
The weather sensors should send periodic updates (every 1 hour) of the above weather readings to a remote server. If the rainfall exceeds 20 mm or the temperature is above 35 degrees or below 20 degrees of Celsius, alerts should be sent to all monitoring stations by the server. Each monitoring station should be able to view the locations and the number of sensors connected, along with their latest weather details. If required, the monitoring stations should be able to query for the current weather at each station. The system should be scalable so that additional weather sensors and/or monitoring stations can be added as required. If a particular sensor doesn’t send the 1 hour update, all monitoring stations should be alerted.   
Each monitoring station should be able to view the number of sensors and monitoring stations (clients) connected to the server at any given time. These two numbers should be thread safe, meaning that when one server thread is updating these values, these valued should be locked and other threads may access these number as read-only. 
Basic security/authentication mechanisms should be there to authenticate each sensor and monitor (client) connection and that data that is sent by the sensors.     
  
1. Based on the given requirements, implement a client-server system to gather and monitor weather data at the remote base-stations. You may use regular Java programs to emulate the behavior of the IOT sensors.   
2. You may use Java Socket programming to communicate between the server and the sensors and Java RMI to manage the communication between the server and the monitoring stations (clients).    
3. Write a report explaining the system. The report should include:  
- The High level system architecture diagram along with the software components - Sequence diagrams for each operations - Assumptions made in the implementation 
The report should be within 2-3 pages of length. 
4. Write a readme.txt file explaining how to test the given system.   
Note: You may use the Netbeans IDE to do the development and upload the source files in a Netbeans project. Do not use a database and simply use text file/files and/or XML file/files to store/retrieve any data that you want to run and demonstrate the application.   
Note: All submissions will be checked for plagiarism using the Turnitin software and the submissions that have 20% or higher similarity ratio will have to be resubmitted.   
Note: The Netbeans project with the source files, readme.txt file and the report should be uploaded in a single zip archive. The zip file name should be your SLIIT registration number. 
