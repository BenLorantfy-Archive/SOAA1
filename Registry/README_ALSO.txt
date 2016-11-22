=====================================
Extra Steps Not In Other Instructions
=====================================

After you do all the steps in SOA-Registry/document/ThingsYouNeedToDo.doc, at the 
bottom of SOA-Registry/config/soa.msgListener.properties, there's this line:

	SOAListenerDBaseURL=jdbc:sqlserver://<machineName>\\SQLExpress:1433;SelectMethod=Cursor;DatabaseName=SOARegistry

You have to replace <machineName> with your actual machine name, Sean doesn't mention this anywhere. Here's an example: 

	SOAListenerDBaseURL=jdbc:sqlserver://2A314-D07\\SQLExpress:1433;SelectMethod=Cursor;DatabaseName=SOARegistry


- Ben
