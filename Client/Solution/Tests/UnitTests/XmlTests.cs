// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ConfigTests.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: Tests
//    Author: Grigory Kozyrev
//    Date: 27/09/2016
//    Last Updated: 29/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System.Collections.Generic;
using System.IO;
using System.Linq;
using WebServiceCore.Models;
using WebServiceCore.Utilities;
using Xunit;

#endregion

namespace Tests.UnitTests
{
    public class XmlTests : UnitTestBase
    {
        #region Constants

        private const string CONFIG_PATH = "ConfigTests";

        private static readonly IWebService[] EXPECTED_SERVICES_1 = new IWebService[]
        {
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
                                null,
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
                                null,
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
                                null,
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
                                null,
                                "[a-zA-Z]{2}",
                                "Country 2 letter ISO Alpha-2 code. For example 'CA', 'US', 'RU'"),
                        }),
                }),
            new WebService(
                "Airport Information Webservice 2",
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
                                null,
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
                                null,
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
                                null,
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
                                null,
                                "[a-zA-Z]{2}",
                                "Country 2 letter ISO Alpha-2 code. For example 'CA', 'US', 'RU'"),
                        }),
                }),
        };

        private static readonly IEnumerable<ResponseHolder> RESPONSE_1 = new List<ResponseHolder>()
        {
            new ResponseHolder("Item 1",
                               new List<IMethodParameter>()
                               {
                                   new MethodParameter(
                                       "AtomicNumber",
                                       "string",
                                       "8",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "ElementName",
                                       "string",
                                       "Oxygen",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "Symbol",
                                       "string",
                                       "O",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "AtomicWeight",
                                       "string",
                                       "15.9994",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "BoilingPoint",
                                       "string",
                                       "90.2",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "IonisationPotential",
                                       "string",
                                       "13.61",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "EletroNegativity",
                                       "string",
                                       "3.5",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "AtomicRadius",
                                       "string",
                                       "0.74",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "MeltingPoint",
                                       "string",
                                       "55",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "Density",
                                       "string",
                                       "1.3318",
                                       null,
                                       null),
                               }),
        };

        private static readonly IEnumerable<ResponseHolder> RESPONSE_2 = new List<ResponseHolder>()
        {
            new ResponseHolder("Item 1",
                               new List<IMethodParameter>()
                               {
                                   new MethodParameter(
                                       "ReturnCode",
                                       "string",
                                       "1",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "IP",
                                       "string",
                                       "69.159.13.21",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "ReturnCodeDetails",
                                       "string",
                                       "Success",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "CountryName",
                                       "string",
                                       "Canada",
                                       null,
                                       null),
                                   new MethodParameter(
                                       "CountryCode",
                                       "string",
                                       "CAN",
                                       null,
                                       null),
                               }),
        };

        #endregion

        [Fact]
        public void ConfigLoad1_Success()
        {
            var services = XmlHelper.ReadConfig(Path.Combine(TEST_DIRECTORY, CONFIG_PATH, "config1.xml"));
            var webServices = services as IWebService[] ?? services.ToArray();

            Assert.Equal(webServices.Count(), 1);

            ValidateWebService(webServices[0], EXPECTED_SERVICES_1[0]);
        }

        [Fact]
        public void ConfigLoad2_Success()
        {
            var services = XmlHelper.ReadConfig(Path.Combine(TEST_DIRECTORY, CONFIG_PATH, "config2.xml"));
            var webServices = services as IWebService[] ?? services.ToArray();

            Assert.Equal(webServices.Count(), 2);

            ValidateWebService(webServices[0], EXPECTED_SERVICES_1[0]);
            ValidateWebService(webServices[1], EXPECTED_SERVICES_1[1]);
        }

        [Fact]
        public void ResponseConvert1_Success()
        {
            var xml = File.ReadAllText(Path.Combine(TEST_DIRECTORY, CONFIG_PATH, "response1.xml"));

            var result = XmlHelper.ConvertSoapXml(xml);

            ValidateResponseHolders(result, RESPONSE_1);
        }

        [Fact]
        public void ResponseConvert2_Success()
        {
            var xml = File.ReadAllText(Path.Combine(TEST_DIRECTORY, CONFIG_PATH, "response2.xml"));

            var result = XmlHelper.ConvertSoapXml(xml);

            ValidateResponseHolders(result, RESPONSE_2);
        }

        #region Private Members

        private void ValidateResponseHolders(IEnumerable<ResponseHolder> response1, IEnumerable<ResponseHolder> response2)
        {
            var r1Array = response1 as ResponseHolder[] ?? response1.ToArray();
            var r2Array = response2 as ResponseHolder[] ?? response2.ToArray();

            Assert.Equal(r1Array.Count(), r2Array.Count());

            for (int i = 0; i < r1Array.Length; ++i)
            {
                var r1 = r1Array[i];
                var r2 = r2Array[i];

                Assert.Equal(r1.Name, r2.Name);

                var r1parameters = r1.Parameters as IMethodParameter[] ?? r1.Parameters.ToArray();
                var r2parameters = r2.Parameters as IMethodParameter[] ?? r2.Parameters.ToArray();

                Assert.Equal(r1parameters.Count(), r2parameters.Count());

                for (int j = 0; j < r1parameters.Length; ++j)
                {
                    ValidateParameter(r1parameters[j], r2parameters[j]);
                }
            }
        }

        private void ValidateParameter(IMethodParameter parameter1, IMethodParameter parameter2)
        {
            Assert.Equal(parameter1.Name, parameter2.Name);
            Assert.Equal(parameter1.Type, parameter2.Type);
            Assert.Equal(parameter1.Value, parameter2.Value);
            Assert.Equal(parameter1.Help, parameter2.Help);
            Assert.Equal(parameter1.Rule, parameter2.Rule);
        }

        private void ValidateWebMethod(IWebMethod method1, IWebMethod method2)
        {
            Assert.Equal(method1.Name, method2.Name);
            Assert.Equal(method1.XMLNS, method2.XMLNS);

            var m1Parameters = method1.Parameters as IMethodParameter[] ?? method1.Parameters.ToArray();
            var m2Parameters = method2.Parameters as IMethodParameter[] ?? method2.Parameters.ToArray();
            Assert.Equal(m1Parameters.Length, m2Parameters.Length);

            for (int i = 0; i < m1Parameters.Length; ++i)
            {
                ValidateParameter(m1Parameters[i], m2Parameters[i]);
            }
        }

        private void ValidateWebService(IWebService service1, IWebService service2)
        {
            Assert.Equal(service1.Name, service2.Name);
            Assert.Equal(service1.Host, service2.Host);
            Assert.Equal(service1.Url, service2.Url);

            var s1Methods = service1.Methods as IWebMethod[] ?? service1.Methods.ToArray();
            var s2Methods = service2.Methods as IWebMethod[] ?? service2.Methods.ToArray();
            Assert.Equal(s1Methods.Length, s2Methods.Length);

            for (int i = 0; i < s1Methods.Length; ++i)
            {
                ValidateWebMethod(s1Methods[i], s2Methods[i]);
            }

            Assert.True(service1.IsValid, "parameter1.IsValid");
            Assert.True(service2.IsValid, "parameter1.IsValid");
        }

        #endregion
    }
}