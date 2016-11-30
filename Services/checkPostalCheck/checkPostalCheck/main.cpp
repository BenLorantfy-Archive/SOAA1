
/*
* Example illustrating HTTP service.
*
* Server Usage:
*    sudo ./distribution/example/http_service
*
* Client Usage:
*    curl -w'\n' -v -X POST --data 'Hello, Restbed' 'http://localhost/resource'
*/

#include <memory>
#include <cstdlib>
#include <restbed>
#include <iostream>
#include<stdio.h>
#include "PostalCodeValidator.h"


using namespace std;
using namespace restbed;

//method to handle get request
void get_method_handler(const shared_ptr< Session > session)
{
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
		result.first.push_back("invalid Region");
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

	convert.clear();

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
	session->close(OK, json_response, { {"Content-Length", len} });


}

int main(const int, const char**)
{
	//setup server
	auto resource = make_shared< Resource >();
	resource->set_path("/resource");
	resource->set_method_handler("GET", get_method_handler);

	auto settings = make_shared< Settings >();
	settings->set_port(8080);
	settings->set_default_header("Connection", "close");

	Service service;
	service.publish(resource);
	service.start(settings);

	return EXIT_SUCCESS;
}