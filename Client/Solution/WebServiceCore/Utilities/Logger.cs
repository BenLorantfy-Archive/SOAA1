// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Logger.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 26/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System;
using System.IO;

#endregion

namespace WebServiceCore.Utilities
{
    public static class Logger
    {
        private const string DEBUG = "DEBUG";

        private const string DELIMETER = " | ";

        private const string ERROR = "ERROR";

        private const string INFO = "INFO";

        private const string LOG_FILE_PATH = "log.txt";

        public static void Debug(string message, Exception e)
        {
            Log(message, DEBUG, e);
        }

        public static void Debug(Exception e)
        {
            Debug(string.Empty, e);
        }

        public static void Error(string message, Exception e)
        {
            Log(message, ERROR, e);
        }

        public static void Error(Exception e)
        {
            Error(string.Empty, e);
        }

        public static void Info(string message, Exception e)
        {
            Log(message, INFO, e);
        }

        public static void Info(Exception e)
        {
            Info(string.Empty, e);
        }

        private static void Log(string message, string level, Exception e)
        {
            if (string.IsNullOrWhiteSpace(message))
            {
                message = string.Empty;
            }

            File.AppendAllText(
                LOG_FILE_PATH,
                string.Format(
                    "[{0}] {1}{2} : {3}\n",
                    DateTime.Now,
                    level,
                    string.IsNullOrWhiteSpace(message) ? string.Empty : DELIMETER + message,
                    e));
        }
    }
}