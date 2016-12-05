// Program: SOA
// Assingment 1
// Date:			2016 -12 -04
// Team Members:	Amshar Basheer, Ben Lorantfy, Grigory Kozyrev, Kyle Stevenson 
//

#pragma once
#include<string>
#include<list>
#include<map>
#include<set>
#include<regex>

using namespace std;

// regex string for valid canadian postal code (no spaces)
#define POSTAL_REGEX "[ABCEGHJKLMNPRSTVXY][0-9][ABCEGHJKLMNPRSTVWXYZ][0-9][ABCEGHJKLMNPRSTVWXYZ][0-9]"  

//class to validate a canadian postal code
//requires a postal code (N1N1N1) and region (e.g. ON, NT)
class PostalCodeValidator
{
private:
	string regionCode;		//the province/territory
	string regionName;		//the full name of the province/territory
	string postalCode;		//the postalcode to validate

							//map to hold rules for each region
	map<string, pair<string, bool>> codeInfos;

public:
	bool isRegionValid;

	PostalCodeValidator(string region, string postalCode);

	//method to populate map with rules for a specific region
	bool populatecodeInfos();

	//method to validate postal code
	pair<vector<string>, bool> validatePostalCode();

	virtual ~PostalCodeValidator();
};

