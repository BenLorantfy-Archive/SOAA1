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

        private static readonly string[] EXPECTED_SOAP = new string[]
        {
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message1.expected")).Replace("\r\n", "\n"),
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message2.expected")).Replace("\r\n", "\n"),
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message3.expected")).Replace("\r\n", "\n"),
            File.ReadAllText(Path.Combine(TEST_DIRECTORY, SOAP_DERECTORY, "message4.expected")).Replace("\r\n", "\n"),
        };
    }
}