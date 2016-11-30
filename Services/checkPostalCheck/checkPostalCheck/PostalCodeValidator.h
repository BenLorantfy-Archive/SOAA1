#pragma once
#include<string>
#include<list>
#include<map>
#include<set>
#include<regex>

using namespace std;

#define POSTAL_REGEX "[ABCEGHJKLMNPRSTVXY][0-9][ABCEGHJKLMNPRSTVWXYZ][0-9][ABCEGHJKLMNPRSTVWXYZ][0-9]"  

class PostalCodeValidator
{
private:
	string regionCode;
	string regionName;
	string postalCode;
		
	map<string, pair<string, bool>> codeInfos;

public:
	bool isRegionValid;

	PostalCodeValidator(string region, string postalCode);

	bool populatecodeInfos();
	pair<vector<string>, bool> validatePostalCode();

	virtual ~PostalCodeValidator();
};

