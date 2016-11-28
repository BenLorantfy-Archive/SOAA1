// --------------------------------------------------------------------------------------------------------------------
// <copyright file="LoggerTests.cs" company="none">
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

using System;
using System.IO;
using System.Text.RegularExpressions;
using WebServiceCore.Utilities;
using Xunit;

#endregion

namespace Tests.UnitTests
{
    public class LoggerTests : UnitTestBase
    {
        private const string LOG_FILE_PATH = "log.txt";

        private const string REGEX_TEMPLATE =
            @"\[[0-9]+\/[0-9]+\/[0-9]+ [0-9]+:[0-9]+:[0-9]+ (AM|FM)] {0} | {1} : {2}{3}";

        [Fact]
        public void DebugLog_Success()
        {
            string exceptionText = "Test Exception";
            string message = "Testing Logger";
            string level = "DEBUG";

            try
            {
                var exception = new Exception("Test Exception");
                Logger.Debug("Testing Logger", exception);

                Assert.True(File.Exists(LOG_FILE_PATH), "Log file not found");
                var text = File.ReadAllText(LOG_FILE_PATH);

                var regex = new Regex(
                    string.Format(REGEX_TEMPLATE, level, message, "System.Exception: ", exceptionText));
                Assert.True(regex.IsMatch(text), "Log text doesn't match expected format.\nActual text: " + text);
            }
            finally
            {
                if (File.Exists(LOG_FILE_PATH))
                {
                    File.Delete(LOG_FILE_PATH);
                }
            }
        }

        [Fact]
        public void ErrorLog_Success()
        {
            string exceptionText = "Test Exception";
            string message = "Testing Logger";
            string level = "ERROR";

            try
            {
                var exception = new Exception("Test Exception");
                Logger.Error("Testing Logger", exception);

                Assert.True(File.Exists(LOG_FILE_PATH), "Log file not found");
                var text = File.ReadAllText(LOG_FILE_PATH);

                var regex = new Regex(
                    string.Format(REGEX_TEMPLATE, level, message, "System.Exception: ", exceptionText));
                Assert.True(regex.IsMatch(text), "Log text doesn't match expected format.\nActual text: " + text);
            }
            finally
            {
                if (File.Exists(LOG_FILE_PATH))
                {
                    File.Delete(LOG_FILE_PATH);
                }
            }
        }

        [Fact]
        public void InfoLog_Success()
        {
            string exceptionText = "Test Exception";
            string message = "Testing Logger";
            string level = "INFO";

            try
            {
                var exception = new Exception("Test Exception");
                Logger.Info("Testing Logger", exception);

                Assert.True(File.Exists(LOG_FILE_PATH), "Log file not found");
                var text = File.ReadAllText(LOG_FILE_PATH);

                var regex = new Regex(
                    string.Format(REGEX_TEMPLATE, level, message, "System.Exception: ", exceptionText));
                Assert.True(regex.IsMatch(text), "Log text doesn't match expected format.\nActual text: " + text);
            }
            finally
            {
                if (File.Exists(LOG_FILE_PATH))
                {
                    File.Delete(LOG_FILE_PATH);
                }
            }
        }
    }
}