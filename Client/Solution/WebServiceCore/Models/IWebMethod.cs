// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IWebMethod.cs" company="none">
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
    /// Standard interface for web method.
    /// </summary>
    public interface IWebMethod
    {
        /// <summary>
        /// Returns true if web method is valid.
        /// </summary>
        /// <value>
        ///   <c>true</c> if this instance is valid; otherwise, <c>false</c>.
        /// </value>
        bool IsValid { get; }

        /// <summary>
        /// Gets or sets the method name.
        /// </summary>
        string Name { get; }

        /// <summary>
        /// Gets or sets the method parameters.
        /// </summary>
        IEnumerable<IMethodParameter> Parameters { get; }

        /// <summary>
        /// Gets the xmlns of the method.
        /// </summary>
        string XMLNS { get; }
    }
}