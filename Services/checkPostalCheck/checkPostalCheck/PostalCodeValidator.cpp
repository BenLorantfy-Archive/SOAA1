// Program: SOA
// Assingment 1
// Date:			2016 -12 -04
// Team Members:	Amshar Basheer, Ben Lorantfy, Grigory Kozyrev, Kyle Stevenson 
//

#include "PostalCodeValidator.h"

PostalCodeValidator::PostalCodeValidator(string _region, string _postalCode)
{
	//convert to all upper case
	transform(_region.begin(), _region.end(), _region.begin(), toupper);
	transform(_postalCode.begin(), _postalCode.end(), _postalCode.begin(), toupper);

	//get rid of whitespace
	_region.erase(remove_if(_region.begin(), _region.end(), isspace));
	_postalCode.erase(remove_if(_postalCode.begin(), _postalCode.end(), isspace));

	//set class properties
	regionCode = _region;
	postalCode = _postalCode;

	//
	isRegionValid = populatecodeInfos();
}

//function to validate postal code
//returns pair with boolean to say if valid or not
//	and a vector of strings with any special notes
pair<vector<string>, bool> PostalCodeValidator::validatePostalCode()
{

	pair<vector<string>, bool> ret;
	regex validpostal(POSTAL_REGEX);

	try
	{

		if (!regex_match(postalCode, validpostal))
		{
			//regex failed, bad format
			ret.second = false;
			ret.first.push_back("Bad postal code format");
		}
		else
		{
			//first two digits not valid
			map<string, pair<string, bool>>::iterator it;
			int count = 0;

			//loop though postal code chars
			for (int i = 1; i < postalCode.length(); i++) {

				//test first ith chars of postal code
				string str = postalCode.substr(0, i);

				it = codeInfos.find(str);

				if (it != codeInfos.end()) {
					count++;
					ret.second = codeInfos.at(str).second;

					if (codeInfos.at(str).first != "") {
						ret.first.push_back(codeInfos.at(str).first);
					}
				}
			}

			if (count == 0) {
				//could not find postal code for this province
				ret.second = false;
				ret.first.push_back("Invalid Postal Code");
			}
		}
	}
	catch (exception ex)
	{
		ret.second = false;
		ret.first.push_back(ex.what());
	}

	return ret;
}

//function to populate rules based on province/territory
bool PostalCodeValidator::populatecodeInfos() {

	bool ret = true;

	try {
		if (regionCode == "NL") {
			regionName = "Newfoundland";

			codeInfos["A0"] = make_pair("Rural Newfoundland", true);
			codeInfos["A1"] = make_pair("", true);
			codeInfos["A2"] = make_pair("", true);
			codeInfos["A3"] = make_pair("Wanna Be Newfoundland", false);
			codeInfos["A4"] = make_pair("Wanna Be Newfoundland", false);
			codeInfos["A5"] = make_pair("", true);
			codeInfos["A6"] = make_pair("Wanna Be Newfoundland", false);
			codeInfos["A7"] = make_pair("Wanna Be Newfoundland", false);
			codeInfos["A8"] = make_pair("", true);
			codeInfos["A9"] = make_pair("Wanna Be Newfoundland", false);
		}
		else if (regionCode == "NS")
		{
			regionName = "Nova Scotia";

			codeInfos["B0"] = make_pair("Rural Nova Scotia", true);
			codeInfos["B1"] = make_pair("", true);
			codeInfos["B2"] = make_pair("", true);
			codeInfos["B3"] = make_pair("Wanna Be Nova Scotia", false);
			codeInfos["B4"] = make_pair("Wanna Be Nova Scotia", false);
			codeInfos["B5"] = make_pair("", true);
			codeInfos["B6"] = make_pair("Wanna Be Nova Scotia", false);
			codeInfos["B7"] = make_pair("Wanna Be Nova Scotia", false);
			codeInfos["B8"] = make_pair("", true);
			codeInfos["B9"] = make_pair("Wanna Be Nova Scotia", false);
		}
		else if (regionCode == "NB")
		{
			regionName = "New Brunswick";

			codeInfos["E0"] = make_pair("Wanna be Rural New Brunswick", false);
			codeInfos["E1"] = make_pair("", true);
			codeInfos["E2"] = make_pair("", true);
			codeInfos["E3"] = make_pair("", true);
			codeInfos["E4"] = make_pair("", true);
			codeInfos["E5"] = make_pair("", true);
			codeInfos["E6"] = make_pair("", true);
			codeInfos["E7"] = make_pair("", true);
			codeInfos["E8"] = make_pair("", true);
			codeInfos["E9"] = make_pair("", true);
		}
		else if (regionCode == "PE")
		{
			regionName = "Prnice Edward Island";

			codeInfos["C0"] = make_pair("Rural PEI", true);
			codeInfos["C1"] = make_pair("", true);
			codeInfos["C2"] = make_pair("Wanna be PEI", false);
			codeInfos["C3"] = make_pair("Wanna be PEI", false);
			codeInfos["C4"] = make_pair("Wanna be PEI", false);
			codeInfos["C5"] = make_pair("Wanna be PEI", false);
			codeInfos["C6"] = make_pair("Wanna be PEI", false);
			codeInfos["C7"] = make_pair("Wanna be PEI", false);
			codeInfos["C8"] = make_pair("Wanna be PEI", false);
			codeInfos["C9"] = make_pair("Wanna be PEI", false);
		}
		else if (regionCode == "QC")
		{
			regionName = "Quebec";

			codeInfos["G"] = make_pair("Eastern Quebec", false);
			codeInfos["G0"] = make_pair("Rural New Brunswick", true);
			codeInfos["G1"] = make_pair("", true);
			codeInfos["G1A"] = make_pair("Provincial Government", true);
			codeInfos["G2"] = make_pair("", true);
			codeInfos["G3"] = make_pair("", true);
			codeInfos["G4"] = make_pair("", true);
			codeInfos["G5"] = make_pair("", true);
			codeInfos["G6"] = make_pair("", true);
			codeInfos["G7"] = make_pair("", true);
			codeInfos["G8"] = make_pair("", true);
			codeInfos["G9"] = make_pair("", true);

			codeInfos["H"] = make_pair("Montreal Area", false);
			codeInfos["H0"] = make_pair("Rural New Brunswick", true);
			codeInfos["H0H 0H0"] = make_pair("Santa Claus", true);
			codeInfos["H1"] = make_pair("", true);
			codeInfos["H2"] = make_pair("", true);
			codeInfos["H3"] = make_pair("", true);
			codeInfos["H4"] = make_pair("", true);
			codeInfos["H5"] = make_pair("", true);
			codeInfos["H6"] = make_pair("Wanna be Quebec", false);
			codeInfos["H7"] = make_pair("", true);
			codeInfos["H8"] = make_pair("", true);
			codeInfos["H9"] = make_pair("", true);

			codeInfos["J"] = make_pair("Western / Northern Quebec", false);
			codeInfos["J0"] = make_pair("Rural Quebec", true);
			codeInfos["J1"] = make_pair("", true);
			codeInfos["J2"] = make_pair("", true);
			codeInfos["J3"] = make_pair("", true);
			codeInfos["J4"] = make_pair("", true);
			codeInfos["J5"] = make_pair("", true);
			codeInfos["J6"] = make_pair("", true);
			codeInfos["J7"] = make_pair("", true);
			codeInfos["J8"] = make_pair("", true);
			codeInfos["J9"] = make_pair("", true);
		}
		else if (regionCode == "ON")
		{
			regionName = "Ontario";

			codeInfos["K"] = make_pair("Eastern Ontario", false);
			codeInfos["K0"] = make_pair("Rural Ontario", true);
			codeInfos["K1"] = make_pair("", true);
			codeInfos["K2"] = make_pair("", true);
			codeInfos["K3"] = make_pair("Wanna Be Ontario", false);
			codeInfos["K4"] = make_pair("", true);
			codeInfos["K5"] = make_pair("Wanna Be Ontario", false);
			codeInfos["K6"] = make_pair("", true);
			codeInfos["K7"] = make_pair("", true);
			codeInfos["K8"] = make_pair("", true);
			codeInfos["K9"] = make_pair("", true);

			codeInfos["L"] = make_pair("Central Ontario", false);
			codeInfos["L0"] = make_pair("Rural Ontario", true);
			codeInfos["L1"] = make_pair("", true);
			codeInfos["L2"] = make_pair("", true);
			codeInfos["L3"] = make_pair("", true);
			codeInfos["L4"] = make_pair("", true);
			codeInfos["L5"] = make_pair("", true);
			codeInfos["L6"] = make_pair("", true);
			codeInfos["L7"] = make_pair("", true);
			codeInfos["L8"] = make_pair("", true);
			codeInfos["L9"] = make_pair("", true);

			codeInfos["M"] = make_pair("Toronto Area", false);
			codeInfos["M0"] = make_pair("Wanna be Ontario", false);
			codeInfos["M1"] = make_pair("", true);
			codeInfos["M2"] = make_pair("", true);
			codeInfos["M3"] = make_pair("", true);
			codeInfos["M4"] = make_pair("Wanna be Ontario", false);
			codeInfos["M5"] = make_pair("", true);
			codeInfos["M6"] = make_pair("Wanna be Ontario", false);
			codeInfos["M7"] = make_pair("", true);
			codeInfos["M7A"] = make_pair("Queen's Park", true);
			codeInfos["M8"] = make_pair("", true);
			codeInfos["M9"] = make_pair("", true);

			codeInfos["N"] = make_pair("Western Ontario", true);
			codeInfos["N0"] = make_pair("Rural Ontario", true);
			codeInfos["N1"] = make_pair("", true);
			codeInfos["N2"] = make_pair("", true);
			codeInfos["N3"] = make_pair("", true);
			codeInfos["N4"] = make_pair("", true);
			codeInfos["N5"] = make_pair("", true);
			codeInfos["N6"] = make_pair("", true);
			codeInfos["N7"] = make_pair("", true);
			codeInfos["N8"] = make_pair("", true);
			codeInfos["N9"] = make_pair("", true);

			codeInfos["P"] = make_pair("Northen Ontario", true);
			codeInfos["P0"] = make_pair("Rural Ontario", true);
			codeInfos["P1"] = make_pair("", true);
			codeInfos["P2"] = make_pair("", true);
			codeInfos["P3"] = make_pair("", true);
			codeInfos["P4"] = make_pair("", true);
			codeInfos["P5"] = make_pair("", true);
			codeInfos["P6"] = make_pair("", true);
			codeInfos["P7"] = make_pair("", true);
			codeInfos["P8"] = make_pair("", true);
			codeInfos["P9"] = make_pair("", true);
		}
		else if (regionCode == "MB")
		{
			regionName = "New Brunswick";

			codeInfos["R0"] = make_pair("Rural Manitoba", true);
			codeInfos["C1"] = make_pair("", true);
			codeInfos["C2"] = make_pair("Winnipeg Area", true);
			codeInfos["C3"] = make_pair("Winnipeg Area", true);
			codeInfos["C4"] = make_pair("", true);
			codeInfos["C5"] = make_pair("", true);
			codeInfos["C6"] = make_pair("", true);
			codeInfos["C7"] = make_pair("", true);
			codeInfos["C8"] = make_pair("", true);
			codeInfos["C9"] = make_pair("", true);
		}
		else if (regionCode == "SK")
		{
			regionName = "Saskatchewan";

			codeInfos["S0"] = make_pair("Rural Saskatchewan", true);
			codeInfos["S1"] = make_pair("Wanna be Saskatchewan", false);
			codeInfos["S2"] = make_pair("", true);
			codeInfos["S3"] = make_pair("", true);
			codeInfos["S4"] = make_pair("", true);
			codeInfos["S5"] = make_pair("Wanna be Saskatchewan", false);
			codeInfos["S6"] = make_pair("", true);
			codeInfos["S7"] = make_pair("Saskatoon Area", true);
			codeInfos["S8"] = make_pair("Wanna be Saskatchewan", false);
			codeInfos["S9"] = make_pair("", true);
		}
		else if (regionCode == "AB")
		{
			regionName = "Alberta";

			codeInfos["T0"] = make_pair("Rural Alberta", true);
			codeInfos["T1"] = make_pair("", true);
			codeInfos["T2"] = make_pair("Calgary Area", true);
			codeInfos["T3"] = make_pair("Calgary Area", true);
			codeInfos["T4"] = make_pair("", true);
			codeInfos["T5"] = make_pair("Edmonton Area", true);
			codeInfos["T6"] = make_pair("Edmonton Area", true);
			codeInfos["T7"] = make_pair("", true);
			codeInfos["T8"] = make_pair("", true);
			codeInfos["T9"] = make_pair("", true);
		}
		else if (regionCode == "BC")
		{
			regionName = "British Columbia";

			codeInfos["V0"] = make_pair("Rural British Columbia", true);
			codeInfos["V1"] = make_pair("", true);
			codeInfos["V2"] = make_pair("", true);
			codeInfos["V3"] = make_pair("", true);
			codeInfos["V4"] = make_pair("", true);
			codeInfos["V5"] = make_pair("", true);
			codeInfos["V6"] = make_pair("", true);
			codeInfos["V7"] = make_pair("", true);
			codeInfos["V8"] = make_pair("", true);
			codeInfos["V9"] = make_pair("", true);
		}
		else if (regionCode == "YT")
		{
			regionName = "Yukon Territories";

			codeInfos["Y0"] = make_pair("Remote Yukon Areas", false);
			codeInfos["Y0A"] = make_pair("", true);
			codeInfos["Y0B"] = make_pair("", true);
			codeInfos["Y1A"] = make_pair("Whitehorse", true);
			codeInfos["Y2"] = make_pair("Wanna Be Yukon", false);
			codeInfos["Y3"] = make_pair("Wanna Be Yukon", false);
			codeInfos["Y4"] = make_pair("Wanna Be Yukon", false);
			codeInfos["Y5"] = make_pair("Wanna Be Yukon", false);
			codeInfos["Y6"] = make_pair("Wanna Be Yukon", false);
			codeInfos["Y7"] = make_pair("Wanna Be Yukon", false);
			codeInfos["Y8"] = make_pair("Wanna Be Yukon", false);
			codeInfos["Y9"] = make_pair("Wanna Be Yukon", false);
		}
		else if (regionCode == "NT")
		{
			regionName = "Northwest Territories";

			codeInfos["X0"] = make_pair("Remote NWT Areas", false);
			codeInfos["X0E"] = make_pair("", true);
			codeInfos["X0F"] = make_pair("", true);
			codeInfos["X1A"] = make_pair("Yellowknife", true);
			codeInfos["X2"] = make_pair("Wanna Be Yukon", false);
			codeInfos["X3"] = make_pair("Wanna Be Yukon", false);
			codeInfos["X4"] = make_pair("Wanna Be Yukon", false);
			codeInfos["X5"] = make_pair("Wanna Be Yukon", false);
			codeInfos["X6"] = make_pair("Wanna Be Yukon", false);
			codeInfos["X7"] = make_pair("Wanna Be Yukon", false);
			codeInfos["X8"] = make_pair("Wanna Be Yukon", false);
			codeInfos["X9"] = make_pair("Wanna Be Yukon", false);
		}
		else if (regionCode == "NU")
		{
			regionName = "Nunavut";

			codeInfos["X0A"] = make_pair("", true);
			codeInfos["X0B"] = make_pair("", true);
			codeInfos["X0C"] = make_pair("", true);
			codeInfos["X2"] = make_pair("Wanna Be Nunavut", false);
			codeInfos["X3"] = make_pair("Wanna Be Nunavut", false);
			codeInfos["X4"] = make_pair("Wanna Be Nunavut", false);
			codeInfos["X5"] = make_pair("Wanna Be Nunavut", false);
			codeInfos["X6"] = make_pair("Wanna Be Nunavut", false);
			codeInfos["X7"] = make_pair("Wanna Be Nunavut", false);
			codeInfos["X8"] = make_pair("Wanna Be Nunavut", false);
			codeInfos["X9"] = make_pair("Wanna Be Nunavut", false);
		}
		else {
			ret = false;
		}
	}
	catch (exception ex) {
		throw "Error validating region";
	}

	return ret;
}


PostalCodeValidator::~PostalCodeValidator()
{
}
