// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ConfigFunctionalTests.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: Tests
//    Author: Grigory Kozyrev
//    Date: 28/09/2016
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

namespace Tests.FunctionalTests
{
    /// <summary>
    /// Test file for config file functional tests.
    /// </summary>
    public class ConfigFunctionalTests : UnitTestBase
    {
        private const string CONFIG_PATH = "ConfigFunctionalTests";

        private void TestServices(IEnumerable<IWebService> services)
        {
            /*foreach (var service in services)
            {
                foreach (var method in service.Methods)
                {
                    var result = HttpHelper.SendHttpRequest(service, method);

                    Assert.True(result.StartsWith("HTTP/1.1 200 OK"),
                        string.Format("Method {0} of Service {1} failed.\nResult: {2}", method.Name, service.Name,
                            result));
                }
            }*/
        }

        [Fact]
        public void ConfigRun1_Success()
        {
            var services = XmlHelper.ReadConfig(Path.Combine(TEST_DIRECTORY, CONFIG_PATH, "config1.xml"));

            TestServices(services);
        }

        [Fact]
        public void ConfigRun2_Success()
        {
            var services = XmlHelper.ReadConfig(Path.Combine(TEST_DIRECTORY, CONFIG_PATH, "config2.xml"));

            TestServices(services);
        }

        [Fact]
        public void ConfigRun3_Success()
        {
            var services = XmlHelper.ReadConfig(Path.Combine(TEST_DIRECTORY, CONFIG_PATH, "config3.xml"));

            TestServices(services);
        }
    }
}