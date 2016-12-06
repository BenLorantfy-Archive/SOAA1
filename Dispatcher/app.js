// [ Dependencies ]
var net = require("net");
var dns = require("dns");
var exec = require('child_process').exec;
var request = require('request');
var services = require("./services.json");

// [ Custom log function ]
var log = function(msg,indent){
    if(!indent) var indent = 0;
    
    var indents = "";
    for(var i = 0; i < indent; i++){
        indents += "    ";
    }
    indents += ">>> ";
    
    console.log(indents + msg);
}
var clients = [];
var registry = {};

// The following ASCII values are taken from the SOA Messaging Protocol Spec
var BOM = 11;
var EOS = 13;
var EOM = 28;

// These ASCII values are whitespace according to https://en.wikipedia.org/wiki/Whitespace_character
var whitespace = [9,10,11,12,13,32 /* <- space */, 133,160,5760,8192,8193,8194,8195,8196,8197,8198,8199,8200,8201,8202,8232,8233,8239,8287,12288 /* following values are not WSpace=Y according to unicode but similar -> */, 6158,8203,8204,8205,8288,65279];


// [ Do all the start up tasks ]
(function startup(){
    log("Starting all the services...");
    for(var i = 0; i < services.services.length; i++){
        var service = services.services[i];
        if(service.start){
            log("Starting " + service.serviceName + " service...",1);
			
			try{
				(function(service){
					exec(service.start, function(error, stdout, stderr){
					  if (error) {
						log("Failed to start " + service.name + " service: " + error);
						return;
					  }
					  //console.log("stdout: " + stdout);
					  //console.log("stderr: " + stderr);
					}); 					
				})(service);
				
			}catch(e){
				log("Failed to start " + service.name + " service: " + e.toString());
			}
       
        }
    }    
    log("Done starting services");
	
	// [ Connect to the registry ]
	log("Connecting to registry...");
	
	// Set up the registry properties
	registry = services.registry;
	registry.registry = true;
	registry.buffer = "";
	registry.messages = [];
	

	// Connect to the registry
	createRegistrySocket(registerTeam);
})();

function createRegistrySocket(callback){
	if(registry.socket){
		try{
			registry.socket.destroy();
		}catch(e){ }		
	}
	
	// Connect to the registry
	registry.socket = net.createConnection({ port:registry.port, host:registry.address },function(){
		registry.buffer = "";
		registry.messages = [];
		
		log("Connected to registry");
		log("Listening for messages from registry");
		listenForMessages(registry);
		
		if(typeof callback === "function"){
			callback();
		}
	});		
	registry.socket.on("close",function(){
		log("Registry socket closed");
	});
	registry.socket.on("error",function(e){
		log("Failed to connect to registry: " + e);
	});	
}

// [ Registers the team with the registry ]
function registerTeam(){
	dns.lookup(require('os').hostname(), function(err, ip, fam){
		var teamName = services.team.name;	
		var message = "";	
		
		// [ Save the IP ]
		services.address = ip;
		
		// [ Create the register-team message ]
		message += String.fromCharCode(BOM) + "DRC|REG-TEAM|||" + String.fromCharCode(EOS);
		message += "INF|" + teamName + "|||" + String.fromCharCode(EOS) + String.fromCharCode(EOM) + "\n";
		
		// [ Write the message to the stream ]
		registry.state = "registeringTeam";
		registry.socket.write(message);		
	});

}

// [ Registers all the services with the registry ]
function registerService(index){
	var team = services.team;
	var service = services.services[index];
	registry.state = "registeringService:" + index;
	
	var message = "";
	
	// [ Create the register service message ]
	message += String.fromCharCode(BOM) + "DRC|PUB-SERVICE|" + team.name + "|" + team.id + "|" + String.fromCharCode(EOS);
	
	message += "SRV|" + 
					service.tagName.replace(/\|/g,"") + "|" + 
					service.serviceName.replace(/\|/g,"") + "|" + 
					service.securityLevel + "|" +
					service.arguments.length + "|" + 
					service.returns.length + "|" + 
					service.description.replace(/\|/g,"") + "|" + 
					String.fromCharCode(EOS);
					
	// [ Add all the arguments ]
	for(var i = 0; i < service.arguments.length; i++){
		var argument = service.arguments[i];
		message += "ARG|" + 
			(i + 1) + "|" + 
			argument.name.replace(/\|/g,"") + "|" + 
			argument.type + "|" + 
			(argument.mandatory ? "mandatory" : "optional") + "||" +
			String.fromCharCode(EOS);
	}
	
	// [ Add all the responses ]
	for(var i = 0; i < service.returns.length; i++){
		var ret = service.returns[i];
		message += "RSP|" + 
			(i + 1) + "|" + 
			ret.name.replace(/\|/g,"") + "|" + 
			ret.type + "||" +
			String.fromCharCode(EOS);
	}
	
	// [ Add the service location ]
	message += "MCH|" + services.address + "|" + services.port + "|" + String.fromCharCode(EOS);
	message += String.fromCharCode(EOM) + "\n";
	
	createRegistrySocket(function(){
		registry.socket.write(message);
	});
}

// [ Consumes a message and performs required actions ]
function consumeMessage(client,message){
	// [ Gets all the message segments ]
	var segments = message.split(String.fromCharCode(EOS));
	
	//console.log("::::Message Segments::::");
	//console.log(segments);
	//console.log("::::End Message Segments::::");
	
	// [ Remove all the empty segments ]
	for(var i = segments.length - 1; i >= 0; i--){
		if(typeof segments[i] != "string"){
			segments.splice(i,1);
			continue;
		}
		
		if(segments[i].trim() == ""){
			segments.splice(i,1);
			continue;
		}
	}
	
	// [ Make sure we didn't recieve an empty message ]
	if(segments.length == 0){
		log("Recieved empty message");
		return;
	}
	
	// [ Check which type of message ]
	for(var i = 0; i < segments.length; i++){
		var parts = segments[i].split("|");
		if(parts[0] == "DRC" && parts[1] == "EXEC-SERVICE"){
            log("Executing service...");
			executeService(client,segments)
		}
		
		// [ Checks if client is registry ]
		if(client.registry){
			if(registry.state == "registeringTeam"){
				if(parts[1] == "OK"){
					log("Successfully registered team");
				}else{
					log("Failed to register team: " + parts[3]);
					break;
				}
				
				services.team.id = parts[2];
				services.team.expiration = parts[3];
				
				// After team is registered, next step is to register the services
				registerService(0);
				
			}else if(registry.state.indexOf("registeringService") == 0){
				var index = registry.state.split(":")[1] * 1;
				if(index < services.services.length - 1){
					registerService(index + 1);
				}	
			}
		}
	}
}

// [ Executes a service given the segments ]
function executeService(client,segments){
	  
    // [ Make sure SRV segment exists ]
    if(!segments[1]){
		log("Missing SRV segment",1);
		return;	
	}
	
	// [ Make sure SRV segment exists ]
	var srv = segments[1].split("|");
	if(srv[0] != "SRV"){
		log("Second segment didn't begin with SRV",1);
		return;
	}
	
	// [ Get service name ]
	var serviceName = null;
	if(typeof srv[2] !== "string" ){
		log("Service name invalid",1);
		return;
	}
	if(srv[2].trim() == ""){
		log("Service name empty",1);
		return;
	}
	serviceName = srv[2].trim();
	
	// [ Look for service info ]
	var service = null;
	for(var i = 0; i < services.services.length; i++){
		if(services.services[i].serviceName == serviceName){
			service = services.services[i];
			break;
		}
	}
	
	// [ Make sure service was found ]
	if(service == null){
		log("Couldn't find service with provided service name",1);
		return;
	}
    
    // [ Add all the arguments ]
    var arguments = {};
    for(var i = 0; i < service.arguments.length; i++){
        var argument = service.arguments[i];
        var name = service.arguments[i].name;
        var value = null;
        
        for(var j = 0; j < segments.length; j++){
            var parts = segments[j].split("|");
            if(parts[0] == "ARG" && parts[2] == name){
                value = parts[5];
            }
        }
        
        // [ Error if value wasn't found and argument is required ]
        if(!value && value !== 0 && value !== "0" && value !== "" && argument.mandatory){
            log("Missing required parameter: " + name,1);
            return;
        }
        
        // [ Set the arguement ]
        arguments[name] = value;
    }
	
	// [ Execute service ]
    var url = 'http://localhost:' + service.port + service.path;
	log("Executing " + service.serviceName + "...");
    log("Making REST HTTP request to POST " + url + "...",1);
    
    // [ Make service request ]
    var options = {
         uri:url
        ,method:"POST"
        ,json:arguments
    }
    
    var response = null;
    request(options, function (error, res, data) {
    	// [ Make sure request didn't fail ]
    	if(error || res.statusCode != 200 || !data){
			log("Error: Request to POST " + url + " failed: " + error,1);
			
			response = String.fromCharCode(BOM) + "PUB|NOT-OK|||42|Service isn't started" + String.fromCharCode(EOS) + String.fromCharCode(EOM) + "\n";
    	}

    	if(!response){
			if (typeof(data) == "string"){
				try{
					data = JSON.parse(data);
				}catch(e){
					console.log("Weird: " + e);
				}
			}
			
			
			
			if(data.error || data.Error || data.ERROR){
				//console.log("DATA:",data);
				for(var key in data){
					data[key.toLowerCase()] = data[key];
				}
				//console.log("DATA2:", data);
				
				if(!data.code){
					data.code = 0;
				}
				response = String.fromCharCode(BOM) + "PUB|NOT-OK|" + data.code + "|" + data.message + "||" + String.fromCharCode(EOS);
			}else{
				// [ Start response ]
				response = String.fromCharCode(BOM) + "PUB|OK|||" + service.returns.length + "|" + String.fromCharCode(EOS);

				// [ Loop through each return value ]
				for(var i = 0; i < service.returns.length; i++){
					var ret = service.returns[i];
					var value = data[service.returns[i].name];
					response += "RSP|" + (i + 1) + "|" + ret.name + "|" + ret.type + "|" + value + "|" + String.fromCharCode(EOS);
				}				
			}



	    	response += String.fromCharCode(EOM);
	    	response += "\n"; // This is only suggested in the spec, but the sample client doesn't work without it
    	}

    	// [ Send the response ]
    	log("Sending response to client...", 1);
		console.log(response);
    	client.socket.write(response,function(){
    		log("Response sent", 1);
    	});
    });
}

// [ Creates the server ]
function createServer(err, ip, fam) {
    log("Determined local IP address:" + ip,1)
	// [ Create the TCP server ]
	log("Creating Server...");
	var server = net.createServer(function (socket){
			
		var client = {
			 socket:socket
			,buffer:""	
			,messages:[]
		}
		
		// [ When a client connects, push them to a list of clients ]
		clients.push(client);
		
		// [ Listen for messages ]
		listenForMessages(client);

	}).on('error', function(err){
		// handle errors here
		throw err;
	});

    log("Created server");
	// [ Listen for Connections ]
	server.listen(services.port, ip, function(){
		var info = server.address();
		log('Server listening on ' + info.address + ":" + info.port + "...");
	});
}

function listenForMessages(client){
	client.socket.setEncoding('utf8');
	client.socket.on("data",function(data){
		
		// Add any recieved data to buffer
		client.buffer += data;
												
		// [ Checks buffer for any new messages ]
		while(client.buffer.indexOf(String.fromCharCode(EOM)) >= 0){
			// [ Adds charachters to message until EOM is reached]
			var message = "";
		
			// [ If buffer doesn't start with BOM then invalid data ]
			if(client.buffer.charCodeAt(0) != BOM && client.buffer.length > 0){
				// First try to remove any beginning whitespace before declaring invalid
				while(
					   whitespace.indexOf(client.buffer.charCodeAt(0)) >= 0 
					&& client.buffer.length > 0 
					&& client.buffer.charCodeAt(0) != BOM 
					&& client.buffer.charCodeAt(0) != EOM
					&& client.buffer.charCodeAt(0) != EOS
				){
					client.buffer = client.buffer.substr(1);
				}
				
				if(client.buffer.charCodeAt(0) != BOM && client.buffer.length > 0){
					client.buffer = "";
					log("Recieved invalid message, clearing buffer. Message must start with <BOM>");
					break;				
				}
			}

			// Assumed buffer will always begin with BOM because it passed the above check
			for(var i = 0; i < client.buffer.length; i++){
				message += client.buffer[i];
				
				if(client.buffer.charCodeAt(i) == EOM){
					// [ Remove message from buffer ]
					// .replace only replaces the first occurance which is good here
					client.buffer = client.buffer.replace(message,"");
					
					// Remove any following whitespace (including newlines) to keep buffer clean
					while(
						   whitespace.indexOf(client.buffer.charCodeAt(0)) >= 0 
						&& client.buffer.length > 0 
						&& client.buffer.charCodeAt(0) != BOM 
						&& client.buffer.charCodeAt(0) != EOM
						&& client.buffer.charCodeAt(0) != EOS
					){
						client.buffer = client.buffer.substr(1);
					}
					break;
				}
			}
			
			// Remove BOM and EOM from message because they are not needed anymore
			message = message.replace(String.fromCharCode(EOM),"");
			message = message.replace(String.fromCharCode(BOM),"");
			
			log("Recieved Message");
			client.messages.push(message);
			
		}
		
		for(var i = client.messages.length - 1; i >= 0; i--){
			consumeMessage(client,client.messages[i]);
			
			// [ Remove message so it's not consumed twice ]
			client.messages.splice(i, 1);
		}


	});			
	
}

// [ Look up the local private IP and then start the server ]
log("Looking up IP address...");
dns.lookup(require('os').hostname(), createServer);


