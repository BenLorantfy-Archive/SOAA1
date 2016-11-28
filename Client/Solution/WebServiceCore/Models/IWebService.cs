// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IWebService.cs" company="none">
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

using System.Collections.Generic;

#endregion

namespace WebServiceCore.Models
{
    /// <summary>
    /// Standard interface for web service.
    /// </summary>
    public interface IWebService
    {
        /// <summary>
        /// Gets the service host.
        /// </summary>
        string Host { get; }

        /// <summary>
        /// Returns true if web service is valid.
        /// </summary>
        /// <value>
        ///   <c>true</c> if this instance is valid; otherwise, <c>false</c>.
        /// </value>
        bool IsValid { get; }

        /// <summary>
        /// Gets or sets the service methods.
        /// </summary>
        IEnumerable<IWebMethod> Methods { get; }

        /// <summary>
        /// Gets or sets the service name.
        /// </summary>
        string Name { get; }

        /// <summary>
        /// Gets the service URL.
        /// </summary>
        string Url { get; }
    }
}