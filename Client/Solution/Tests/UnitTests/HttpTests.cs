// --------------------------------------------------------------------------------------------------------------------
// <copyright file="HttpTests.cs" company="none">
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

using System.IO;
using WebServiceCore.Utilities;
using Xunit;

#endregion

namespace Tests.UnitTests
{
    /// <summary>
    /// Class for http connection and messaging tests.
    /// </summary>
    /// <seealso cref="UnitTestBase" />
    public class HttpTests : UnitTestBase
    {
        private const string HTTP_TEST_DIRECTORY = "HttpTests";

        [Fact]
        public void TestHttp_1_Success()
        {
            var request =
                File.ReadAllText(Path.Combine(TEST_DIRECTORY, HTTP_TEST_DIRECTORY, "request1.example"))
                    .Replace("\r\n", "\n");

            var result = HttpHelper.SendHttpRequest("www.webservicex.net", request);

            Assert.True(result.StartsWith("HTTP/1.1 200 OK"));
        }

        [Fact]
        public void TestHttp_2_Success()
        {
            var request =
                File.ReadAllText(Path.Combine(TEST_DIRECTORY, HTTP_TEST_DIRECTORY, "request2.example"))
                    .Replace("\r\n", "\n");

            var result = HttpHelper.SendHttpRequest("www.webservicex.net", request);

            Assert.True(result.StartsWith("HTTP/1.1 200 OK"));
        }
    }
}