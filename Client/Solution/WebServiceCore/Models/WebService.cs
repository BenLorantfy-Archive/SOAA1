// --------------------------------------------------------------------------------------------------------------------
// <copyright file="WebService.cs" company="none">
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
using System.Collections.Generic;

#endregion

namespace WebServiceCore.Models
{
    /// <summary>
    /// Web Service implementation.
    /// </summary>
    public class WebService : IWebService
    {
        /// <summary>
        /// Constructs a new <see cref="WebService"/> object.
        /// </summary>
        /// <param name="name">Name of the web service.</param>
        /// <param name="host">Address of the web service host.</param>
        /// <param name="url">Url of the web service.</param>
        /// <param name="methods"><see cref="IEnumerable{T}"/> of <see cref="IWebMethod"/> used in web service.</param>
        public WebService(string ip, int port, string name, IEnumerable<IMethodParameter> parameters)
        {
            if (parameters == null)
            {
                throw new ArgumentNullException("parameters != null");
            }

            IP = ip;
            Port = port;
            Name = name;
            Parameters = parameters;
        }

        /// <inheritdoc />
        public string IP { get; }

        /// <inheritdoc />
        public int Port { get; }

        /// <inheritdoc />
        public string Name { get; }

        /// <inheritdoc />
        public bool IsValid
        {
            get
            {
                if (string.IsNullOrWhiteSpace(Name)
                    || string.IsNullOrWhiteSpace(IP)
                    || Port <= 0)
                {
                    return false;
                }

                foreach (var parameter in Parameters)
                {
                    if (!parameter.IsValid)
                    {
                        return false;
                    }
                }

                return true;
            }
        }

        /// <inheritdoc />
        public IEnumerable<IMethodParameter> Parameters { get; }
    }
}