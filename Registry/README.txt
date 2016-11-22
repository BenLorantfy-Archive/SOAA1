					       ======================
						 Directory Contents 
					       ======================
   1. Sample-Client sub-directory
      ---------------------------
	a. bin sub-directory 
		- contains the runtime version of a simple SOA-Registry client app
		  - command script "runClient.bat" launches a cheesy interactive Java client app which
		    will exercise the SOA-Registry and allow you to transmit pre-canned messages (found in the
		    bin\msg directory) to the SOA-Registry
		  - please note that 
			- the "PATH" and "JAVA_HOME" directory locations specified in the bin\setEnvironment.cmd 
			  may need to be changed to point to your specific machine's installation of the JRE 
			  (Java Runtime Environment)


    2. SOA-Registry sub-directory
       --------------------------
	a. bin sub-directory 
		- contains the runtime version of the SOA-Registry 
		  - command script "runServer.bat" launches the SOA-Registry server application
		  - please note that 
			- the "PATH" and "JAVA_HOME" directory locations specified in the bin\setEnvironment.cmd 
			  may need to be changed to point to your specific machine's installation of the JRE 
			  (Java Runtime Environment)
			- the run-time argument specified in the runServer.bat command script may need to be
			  changed as well to suit your installation of the SOA-Assign1.zip file
	b. config sub-directory
		- contains the "soa.msgListener.properties" file which specifies some run-time parameters
		  for the SOA-Registry
	c. database sub-directory
		- contains the actual SOA-Registry database files (Microsoft SQL Express 2008)
		  - these are the SOA-Registry.MDF and SOA-Registry_Log.LDF files
		- contains 2 ZIP files
		  - SQLExpress-DBase.zip - contains the SQL Express version of the SOA-Registry databases (already 
		                           in the database directory
		  - SQL2008-DBase.zip - contains the same databases, but in Microsoft SQL 2008 format (ie. not Express)
		- contains SQL scripts (scripts\attachSOARegister.sql and scripts\detachSOARegister.sql)
		  - these script files are used to attach and detach the database from the local SQL2008 instance
		  - please note that the file location hardcoded in the attachSOARegister.sql script may need to
		    be updated - depending on where you unzip the SOA-Assign1.zip file
	d. document sub-directory
		- contains a "ThingsYouNeedToDo" document which you will need to read and follow when trying
              to set-up / configure the SOA-Registry within the 2A314 lab
	e. incoming\Bad and incoming\Good sub-directories
		- when the SOA-Registry receives a message from a client, it permanently stores a copy of the
		  incoming message in one of these 2 directories.  In the processing of the message results in 
		  a "SOA|OK" type of response then the message finds itself in the "Good" sub-directory and if
		  it results in a "SOA|NOT-OK" type of response, it finds itself in the "Bad" sub-directory
	f. logs sub-directory
		- at run-time you will find a file called "SOARegisterListener.log" in this directory
		- please note that you may need to update / change the : "SOAListenerLogDir" property and the
		  "SOAListenerLogLevel" property (in the config\soa.msgListener.properties" file to suit 
		  your machine installation
