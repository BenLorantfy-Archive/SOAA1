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
#include "PostalCodeValidator.h"


using namespace std;
using namespace restbed;

#define PORT 1337

//method to handle get request
void get_method_handler(const shared_ptr< Session > session)
{
	try {

		const auto request = session->get_request();

		int content_length = 0;
		request->get_header("Content-Length", content_length);

		//get query params for postal code and province if they exist
		pair<vector<string>, bool> result;
		string postalCode = request->get_query_parameter("postalcode", "");
		string province = request->get_query_parameter("province", "");

		//validate the postalcode
		PostalCodeValidator postalCodeValidator = PostalCodeValidator(province, postalCode);

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

		//destroy class
		//postalCodeValidator.~PostalCodeValidator();

		//send response back to client
		/*session->fetch(content_length, [](const shared_ptr< Session > session, const Bytes & body)
		{
		fprintf(stdout, "%.*s\n", (int)body.size(), body.data());
		session->close(OK, "Hello, World!", { { "Content-Length", len } });
		}); */

		//send response back to client
		session->close(OK, json_response, { { "Content-Length", len } });
	}
	catch (exception e) {
		ostringstream convert;
		ostringstream convert_length;
		convert << "{\"isValid\": \"" << 0 << "\",";
		convert << "\"specialNotes\":[ \"Error validating postal code\" ]}";
		string json_response = convert.str();

		convert_length << json_response.length();
		string len = convert_length.str();

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
	cout << "Starting Service [Postal Code Validator]...\n";
	auto checkPostalCode = make_shared< Resource >();
	checkPostalCode->set_path("/checkPostalCode");
	checkPostalCode->set_method_handler("GET", get_method_handler);
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