// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ResponseHolder.cs" company="none">
//    Copyright by Grigory Kozyrev
// </copyright>
// <summary>
//    Project: SOA_Assignment2
//    Author: Grigory Kozyrev
//    Date: 29/09/2016
//    Last Updated: 30/09/2016
// </summary>
// --------------------------------------------------------------------------------------------------------------------

#region Using

using System.Collections.Generic;

#endregion

namespace WebServiceCore.Models
{
    /// <summary>
    /// Purpose of this class is to hold response data after service call.
    /// </summary>
    public class ResponseHolder
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ResponseHolder"/> class.
        /// </summary>
        /// <param name="name">The data entry name.</param>
        /// <param name="parameters">The response parameters.</param>
        public ResponseHolder(string name, IEnumerable<IMethodParameter> parameters)
        {
            Name = name;
            Parameters = parameters;
        }

        /// <summary>
        /// Gets the data entry name.
        /// </summary>
        public string Name { get; }

        /// <summary>
        /// Gets the response parameters.
        /// </summary>
        public IEnumerable<IMethodParameter> Parameters { get; }
    }
}