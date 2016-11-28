// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SoapTests.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: Tests
//    Author: Grigory Kozyrev
//    Date: 27/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System.Collections.Generic;
using System.IO;
using WebServiceCore.Models;
using WebServiceCore.Utilities;
using Xunit;

#endregion

namespace Tests.UnitTests
{
    /// <summary>
    /// Tests class for SOAP message helper
    /// </summary>
    public class SoapTests : UnitTestBase
    {
        private const string SOAP_DERECTORY = "SoapTests";

        private static readonly IWebService EXPECTED_SERVICE =
            new WebService(
                "Airport Information Webservice",
                "www.webservicex.net",
                "/airport.asmx",
                new List<IWebMethod>()
                {
                    new WebMethod(
                        "GetAirportInformationByCountry",
                        "http://www.webserviceX.NET",
                        new List<IMethodParameter>()
                        {
                            new MethodParameter(
                                "country",
                                "string",
                                "Canada",
                                "[a-zA-Z]+",
                                "Full country name"),
                        }),
                    new WebMethod(
                        "getAirportInformationByAirportCode",
                        "http://www.webserviceX.NET",
                        new List<IMethodParameter>()
                        {
                            new MethodParameter(
                                "airportCode",
                                "string",
                                "YYZ",
                                "[a-zA-Z]{3}",
                                "Code of the airport"),
                        }),
                    new WebMethod(
                        "getAirportInformationByCityOrAirportName",
                        "http://www.webserviceX.NET",
                        new List<IMethodParameter>()
                        {
                            new MethodParameter(
                                "cityOrAirportName",
                                "string",
                                "Moscow Domodedovo",
                                "[a-zA-Z]+",
                                "Name of the city and/or airport. Most of the time contains both like 'Toronto Pearson'"),
                        }),
                    new WebMethod(
                        "getAirportInformationByISOCountryCode",
                        "http://www.webserviceX.NET",
                        new List<IMethodParameter>()
                        {
                            new MethodParameter(
                                "CountryAbbrviation",
                                "string",
                                "US",
                                "[a-zA-Z]{2}",
                                "Country 2 letter ISO Alpha-2 code. For example 'CA', 'US', 'RU'"),
                        }),
                });

        private static readonly string[] EXPECTED_SOAP = new string[]
        {
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message1.expected")).Replace("\r\n", "\n"),
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message2.expected")).Replace("\r\n", "\n"),
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message3.expected")).Replace("\r\n", "\n"),
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message4.expected")).Replace("\r\n", "\n"),
        };

        [Fact]
        public void MessageGeneration_Success()
        {
            int i = 0;
            foreach (var method in EXPECTED_SERVICE.Methods)
            {
                var soap = SoapHelper.GenerateSOAP(EXPECTED_SERVICE, method);

                Assert.Equal(EXPECTED_SOAP[i++], soap.Item2);
            }
        }
    }
}