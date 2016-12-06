// Program: SOA
// Assingment 1
// Date:			2016 -12 -04
// Team Members:	Amshar Basheer, Ben Lorantfy, Grigory Kozyrev, Kyle Stevenson 
//
#include <memory>
#include <cstdlib>
#include <restbed>
#include <iostream>
#include<stdio.h>
#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "PostalCodeValidator.h"

using namespace std;
using namespace restbed;
using namespace rapidjson;

#define PORT 1337

//method to handle get request
void post_method_handler(const shared_ptr< Session > session)
{
	try {
		const auto request = session->get_request();
		size_t content_length = request->get_header("Content-Length", 0);
	
		// Get Body Data
		session->fetch(content_length, [](const shared_ptr< Session > session, const restbed::Bytes & body) {
			//cout << "Body Size: " << body.size() << endl;
			auto strBody = restbed::String::to_string(body);
			pair<vector<string>, bool> result;
			string json = strBody;
			string postalCode = "";
			string regionCode = "";
			
			Document d;
			d.Parse(json.c_str());
			if (!d.HasParseError()) {

				//get values from body
				if (d.HasMember("postalcode") && d.HasMember("province")) {
					Value& postal = d["postalcode"];
					Value& region = d["province"];
					string postalCode = postal.GetString();
					string regionCode = region.GetString();

					//valid body, continue with validation
					//validate the postalcode
					PostalCodeValidator postalCodeValidator = PostalCodeValidator(regionCode, postalCode);

					if (postalCodeValidator.isRegionValid) {

						result = postalCodeValidator.validatePostalCode();

						//print out results
						cout << result.second << "\n";

						for (int i = 0; i < result.first.size(); i++) {
							cout << result.first.at(i) << "\n";
						}
					}
					else
					{
						result.second = false;
						result.first.push_back("Region not supported");
					}

					ostringstream convert;
					ostringstream convert_length;
					convert << "{\"isValid\": \"" << result.second << "\",";
					convert << "\"specialNotes\":[";

					for (int i = 0; i < result.first.size(); i++) {
						convert << "\"" << result.first.at(i) << "\"";
						if (i < result.first.size() - 1) {
							convert << ",";
						}
					}
					convert << "]}";
					string json_response = convert.str();

					convert_length << json_response.length();
					string len = convert_length.str();

					//send response back to client
					session->close(OK, json_response, { { "Content-Length", len } });
					return;
				}

				if (postalCode.size() == 0 || regionCode.size() == 0) {
					//error missing body
					ostringstream convert;
					ostringstream convert_length;

					convert << "{\"error\": \"true\", \"code\": \"10\", \"message\" : \"must provide postalcode and province in body\"}";
					string json_response = convert.str();
					convert_length << json_response.length();
					string len = convert_length.str();

					//response
					session->close(OK, json_response, { { "Content-Length", len } });
					return;
					
				}			
			}
			else
			{
				ostringstream convert;
				ostringstream convert_length;

				convert << "{\"error\": \"true\", \"code\": \"7\", \"message\" : \"error parsing body\"}";
				string json_response = convert.str();
				convert_length << json_response.length();
				string len = convert_length.str();

				//response
				session->close(OK, json_response, { { "Content-Length", len } });
				return;
			}
		});

		//destroy class
		//postalCodeValidator.~PostalCodeValidator();

	}
	catch (exception e) {
		ostringstream convert;
		ostringstream convert_length;

		convert << "{\"error\": \"true\", \"code\": \"5\", \"message\" : \"service exception\"}";
		string json_response = convert.str();
		convert_length << json_response.length();
		string len = convert_length.str();

		//response
		session->close(OK, json_response, { { "Content-Length", len } });
	}
}

////method to handle exceptions from resource
void resource_error_handler(const int, const exception&, const shared_ptr< Session > session)
{
	if (session->is_open())
	{
		session->close(6000, "Custom Resource Internal Server Error", { { "Content-Length", "37" } });
	}
	else
	{
		fprintf(stderr, "Custom Resource Internal Server Error\n");
	}
}

//method to handle exceptions from service
void service_error_handler(const int, const exception&, const shared_ptr< Session > session)
{
	if (session->is_open())
	{
		session->close(5000, "Custom Service Internal Server Error", { { "Content-Length", "36" } });
	}
	else
	{
		fprintf(stderr, "Custom Service Internal Server Error\n");
	}
}


int main(const int, const char**)
{
	//setup server
	//cout << "Starting Service [Postal Code Validator]...\n";

	auto checkPostalCode = make_shared< Resource >();
	checkPostalCode->set_path("/checkPostalCode");
	checkPostalCode->set_method_handler("POST", post_method_handler);
	checkPostalCode->set_error_handler(&resource_error_handler);

	auto settings = make_shared< Settings >();
	settings->set_port(PORT);
	settings->set_default_header("Connection", "close");

	cout << "Service Started\n";
	cout << "Listening on port:" << PORT;

	Service service;
	service.publish(checkPostalCode);
	service.set_error_handler(service_error_handler);
	service.start(settings);

	return EXIT_SUCCESS;
}